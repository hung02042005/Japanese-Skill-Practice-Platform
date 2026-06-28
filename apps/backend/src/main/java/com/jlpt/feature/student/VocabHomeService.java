/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.student;

import com.jlpt.feature.flashcard.repository.FlashcardRepository;
import com.jlpt.feature.learning.Kanji;
import com.jlpt.feature.learning.VocabularyRepository;
import com.jlpt.feature.learning.VocabularyTopic;
import com.jlpt.feature.learning.VocabularyTopicRepository;
import com.jlpt.feature.student.dto.response.VocabHomeResponse;
import com.jlpt.shared.common.JlptLevels;
import com.jlpt.shared.exception.ResourceNotFoundException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Dựng dữ liệu trang chủ Từ vựng (lesson-path gamified) cho Student — SPEC-vocab-home UC-09/UC-19.
 *
 * <p>Lưu ý: hệ thống chưa có model subscription (FREE/VIP) → mặc định "FREE" và không VIP-gate
 * (vipOnly=false). Trạng thái khoá VIP sẽ được bổ sung khi có model subscription.
 */
@Service
@RequiredArgsConstructor
public class VocabHomeService {

    private static final int WEEK_DAYS = 7;

    /** Ngưỡng "thành thạo" một từ: số lần lặp SM-2 thành công liên tiếp (#5, quyết định đã chốt). */
    private static final int MASTERY_THRESHOLD = 3;

    private final StudentUserRepository studentUserRepository;
    private final VocabularyTopicRepository topicRepository;
    private final VocabularyRepository vocabularyRepository;
    private final StudentContentProgressRepository progressRepository;
    private final FlashcardRepository flashcardRepository;

    @Transactional(readOnly = true)
    public VocabHomeResponse getVocabHome(Long studentId) {
        return getVocabHome(studentId, null);
    }

    /**
     * @param levelOverride cấp độ JLPT do người dùng chọn (vd từ màn Course List). Null/blank →
     *     dùng cấp độ hiện tại của học viên. Sai định dạng → {@link BadRequestException}.
     */
    @Transactional(readOnly = true)
    public VocabHomeResponse getVocabHome(Long studentId, String levelOverride) {
        StudentUser student = studentUserRepository
                .findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy học viên"));

        StudentUser.JlptLevel level = JlptLevels.parseOptional(levelOverride);
        if (level == null) {
            level = student.getCurrentJlptLevel() != null ? student.getCurrentJlptLevel() : StudentUser.JlptLevel.N5;
        }

        int streak = student.getCurrentStreak() != null ? student.getCurrentStreak() : 0;

        List<VocabularyTopic> topics = topicRepository.findPublishedByLevel(level, Kanji.ContentStatus.PUBLISHED);

        // Đếm "đã học"/"thành thạo" theo chủ đề cho thanh tiến độ (#5) — 2 query batched, tránh N+1.
        List<Long> topicIds = topics.stream().map(VocabularyTopic::getId).toList();
        Map<Long, Long> learnedByTopic = topicIds.isEmpty()
                ? Map.of()
                : toCountMap(flashcardRepository.countLearnedVocabByTopics(
                        studentId, Kanji.ContentStatus.PUBLISHED, topicIds));
        Map<Long, Long> masteredByTopic = topicIds.isEmpty()
                ? Map.of()
                : toCountMap(flashcardRepository.countMasteredVocabByTopics(
                        studentId, Kanji.ContentStatus.PUBLISHED, topicIds, MASTERY_THRESHOLD));

        List<VocabHomeResponse.LessonItem> lessons = new ArrayList<>(topics.size());
        boolean activeAssigned = false;
        for (VocabularyTopic topic : topics) {
            long total = vocabularyRepository.countPublishedByTopic(Kanji.ContentStatus.PUBLISHED, topic.getId());
            long completed = progressRepository.countCompletedVocabularyInTopic(
                    studentId,
                    StudentContentProgress.ContentType.VOCABULARY,
                    StudentContentProgress.ProgressStatus.COMPLETED,
                    topic.getId(),
                    Kanji.ContentStatus.PUBLISHED);

            // Đúng MỘT bài "active" (FR-VH-08): chủ đề đầu tiên chưa hoàn thành.
            // Chủ đề đã hoàn thành hoặc các chủ đề còn lại → "available".
            String status;
            boolean done = total > 0 && completed >= total;
            if (!done && !activeAssigned) {
                status = "active";
                activeAssigned = true;
            } else {
                status = "available";
            }

            lessons.add(VocabHomeResponse.LessonItem.builder()
                    .topicId(topic.getId())
                    .slug(topic.getSlug())
                    .titleJp(topic.getTitleJa())
                    .subtitleEn(topic.getTitleVi())
                    .status(status)
                    .thumbnail(null)
                    .vipOnly(false)
                    .totalWords(total)
                    .learnedCount(learnedByTopic.getOrDefault(topic.getId(), 0L))
                    .masteredCount(masteredByTopic.getOrDefault(topic.getId(), 0L))
                    .build());
        }

        return VocabHomeResponse.builder()
                .streak(streak)
                .weekDays(computeWeekDays(student.getLastActivityDate(), streak))
                .courseTitle(level.name() + " Kanji & Vocab")
                .level(level.name())
                .subscription("FREE")
                .lessons(lessons)
                .build();
    }

    /** Gom kết quả {@code [topicId, count]} của query GROUP BY thành Map topicId → count. */
    private static Map<Long, Long> toCountMap(List<Object[]> rows) {
        Map<Long, Long> map = new HashMap<>(rows.size());
        for (Object[] r : rows) {
            map.put(((Number) r[0]).longValue(), ((Number) r[1]).longValue());
        }
        return map;
    }

    /**
     * Tiến độ tuần hiện tại (Thứ 2 → Chủ nhật), 7 phần tử boolean.
     * Chưa có bảng log học theo ngày → suy ra từ streak: đánh dấu các ngày nằm trong
     * khoảng streak [lastActivity - (streak-1) .. lastActivity] và không vượt quá hôm nay.
     */
    private List<Boolean> computeWeekDays(LocalDate lastActivity, int streak) {
        List<Boolean> days = new ArrayList<>(Collections.nCopies(WEEK_DAYS, Boolean.FALSE));
        if (lastActivity == null || streak <= 0) {
            return days;
        }
        LocalDate today = LocalDate.now();
        LocalDate monday = today.with(DayOfWeek.MONDAY);
        LocalDate streakStart = lastActivity.minusDays(streak - 1L);
        for (int i = 0; i < WEEK_DAYS; i++) {
            LocalDate d = monday.plusDays(i);
            boolean studied = !d.isAfter(today) && !d.isAfter(lastActivity) && !d.isBefore(streakStart);
            days.set(i, studied);
        }
        return days;
    }
}
