/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.student;

import com.jlpt.feature.learning.Kanji;
import com.jlpt.feature.learning.VocabularyRepository;
import com.jlpt.feature.learning.VocabularyTopic;
import com.jlpt.feature.learning.VocabularyTopicRepository;
import com.jlpt.feature.student.dto.response.VocabHomeResponse;
import com.jlpt.shared.exception.BadRequestException;
import com.jlpt.shared.exception.ResourceNotFoundException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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

    private final StudentUserRepository studentUserRepository;
    private final VocabularyTopicRepository topicRepository;
    private final VocabularyRepository vocabularyRepository;
    private final StudentContentProgressRepository progressRepository;

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

        StudentUser.JlptLevel level = parseLevel(levelOverride);
        if (level == null) {
            level = student.getCurrentJlptLevel() != null ? student.getCurrentJlptLevel() : StudentUser.JlptLevel.N5;
        }

        int streak = student.getCurrentStreak() != null ? student.getCurrentStreak() : 0;

        List<VocabularyTopic> topics = topicRepository.findPublishedByLevel(level, Kanji.ContentStatus.PUBLISHED);

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

    /** Null/blank → null (dùng cấp độ học viên). Sai định dạng → BadRequestException. */
    private StudentUser.JlptLevel parseLevel(String level) {
        if (level == null || level.isBlank()) {
            return null;
        }
        try {
            return StudentUser.JlptLevel.valueOf(level.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Cấp độ JLPT không hợp lệ: " + level);
        }
    }
}
