/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.student;

import com.jlpt.feature.learning.Kanji;
import com.jlpt.feature.learning.Lesson;
import com.jlpt.feature.learning.LessonRepository;
import com.jlpt.feature.learning.VocabularyRepository;
import com.jlpt.feature.student.StudentContentProgress.ContentType;
import com.jlpt.feature.student.StudentContentProgress.ProgressStatus;
import com.jlpt.feature.student.StudentUser.JlptLevel;
import com.jlpt.feature.student.dto.response.DashboardResponse;
import com.jlpt.feature.student.dto.response.NextLessonResponse;
import com.jlpt.feature.student.dto.response.StudentStatsResponse;
import com.jlpt.feature.student.kanji.StudentKanjiRepository;
import com.jlpt.shared.exception.ResourceNotFoundException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Tổng hợp dữ liệu học tập cho học viên: Dashboard (UC-09), thống kê (trang Tiến độ),
 * và bài học kế tiếp (trang Học Từ Mới). Tái dùng các query đếm sẵn có ở
 * {@link StudentContentProgressRepository} — không phát sinh migration.
 */
@Service
@RequiredArgsConstructor
public class StudentDashboardService {

    private static final int WEEK_DAYS = 7;
    private static final int DEFAULT_LESSON_MINUTES = 10;
    private static final int MAX_SUGGESTED = 4;

    private final StudentUserRepository studentUserRepository;
    private final StudentContentProgressRepository progressRepository;
    private final StudentKanjiRepository kanjiRepository;
    private final VocabularyRepository vocabularyRepository;
    private final LessonRepository lessonRepository;

    // ── Mục 1: Dashboard ────────────────────────────────────────────────────────
    @Transactional(readOnly = true)
    public DashboardResponse getDashboard(Long studentId) {
        StudentUser student = loadStudent(studentId);
        int streak = nz(student.getCurrentStreak());
        LocalDate now = LocalDate.now();

        return DashboardResponse.builder()
                .streak(streak)
                .weekDays(computeWeekDays(student.getLastActivityDate(), streak))
                .selectedLevel(currentLevel(student).name())
                .wordCount(progressRepository.countCompletedVocab(studentId))
                .daysThisMonth(
                        progressRepository.countDistinctStudyDaysInMonth(studentId, now.getYear(), now.getMonthValue()))
                .lessons(buildLearningPath(studentId))
                .build();
    }

    // ── Mục 5: Thống kê ─────────────────────────────────────────────────────────
    @Transactional(readOnly = true)
    public StudentStatsResponse getStats(Long studentId) {
        StudentUser student = loadStudent(studentId);
        LocalDate now = LocalDate.now();
        return StudentStatsResponse.builder()
                .currentStreak(nz(student.getCurrentStreak()))
                .longestStreak(nz(student.getLongestStreak()))
                .wordCount(progressRepository.countCompletedVocab(studentId))
                .lessonsCompleted(
                        progressRepository.countCompleted(studentId, ContentType.LESSON, ProgressStatus.COMPLETED))
                .daysThisMonth(
                        progressRepository.countDistinctStudyDaysInMonth(studentId, now.getYear(), now.getMonthValue()))
                .build();
    }

    // ── Mục 4: Bài học kế tiếp ───────────────────────────────────────────────────
    @Transactional(readOnly = true)
    public NextLessonResponse getNextLesson(Long studentId) {
        StudentUser student = loadStudent(studentId);
        JlptLevel level = currentLevel(student);

        List<Lesson> lessons = lessonRepository.findByJlptLevelAndStatusOrderByDisplayOrderAscIdAsc(
                level, Lesson.LessonStatus.PUBLISHED);

        Map<Long, Integer> progressById = lessonProgressById(
                studentId, lessons.stream().map(Lesson::getId).toList());

        // Bài kế tiếp = bài chưa hoàn thành đầu tiên; gợi ý = các bài chưa hoàn thành còn lại.
        List<Lesson> pending = lessons.stream()
                .filter(l -> progressById.getOrDefault(l.getId(), 0) < 100)
                .toList();

        NextLessonResponse.LessonItem next = pending.isEmpty() ? null : toItem(pending.get(0), progressById);
        List<NextLessonResponse.LessonItem> suggested = pending.stream()
                .skip(1)
                .limit(MAX_SUGGESTED)
                .map(l -> toItem(l, progressById))
                .collect(Collectors.toList());

        return NextLessonResponse.builder()
                .nextLesson(next)
                .suggestedLessons(suggested)
                .build();
    }

    // ── Helpers ──────────────────────────────────────────────────────────────────

    private StudentUser loadStudent(Long studentId) {
        return studentUserRepository
                .findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy học viên"));
    }

    private JlptLevel currentLevel(StudentUser student) {
        return student.getCurrentJlptLevel() != null ? student.getCurrentJlptLevel() : JlptLevel.N5;
    }

    private static int nz(Integer v) {
        return v != null ? v : 0;
    }

    private Map<Long, Integer> lessonProgressById(Long studentId, List<Long> lessonIds) {
        if (lessonIds.isEmpty()) {
            return Map.of();
        }
        return progressRepository
                .findByStudentIdAndContentTypeAndContentIdIn(studentId, ContentType.LESSON, lessonIds)
                .stream()
                .collect(Collectors.toMap(
                        StudentContentProgress::getContentId,
                        p -> p.getProgressPercent() != null
                                ? p.getProgressPercent().intValue()
                                : 0,
                        (a, b) -> a));
    }

    private NextLessonResponse.LessonItem toItem(Lesson l, Map<Long, Integer> progressById) {
        return NextLessonResponse.LessonItem.builder()
                .lessonId(l.getId())
                .title(l.getTitle())
                .description(l.getExplanation())
                .jlptLevel(l.getJlptLevel() != null ? l.getJlptLevel().name() : null)
                .lessonType(l.getLessonType() != null ? l.getLessonType().name() : null)
                .estimatedMinutes(DEFAULT_LESSON_MINUTES)
                .progressPercent(progressById.getOrDefault(l.getId(), 0))
                .build();
    }

    /**
     * Tiến độ tuần (Thứ 2 → CN). Chưa có log học theo ngày → suy từ streak
     * (đồng nhất với {@link VocabHomeService#computeWeekDays}).
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
            days.set(i, !d.isAfter(today) && !d.isAfter(lastActivity) && !d.isBefore(streakStart));
        }
        return days;
    }

    /** Một mục mẫu trên lộ trình kỹ năng. progress thực tính cho KANJI/VOCAB, còn lại tạm 0. */
    private record Category(String type, String title, String thumbnail) {}

    /**
     * Lộ trình kỹ năng (skill-category cards) cho cả 5 cấp độ để FE lọc theo selectedLevel.
     * Tiến độ KANJI/VOCAB lấy từ dữ liệu thật; KANA/GRAMMAR/LISTENING tạm 0 (sẽ bổ sung sau).
     */
    private List<DashboardResponse.LessonCard> buildLearningPath(Long studentId) {
        List<DashboardResponse.LessonCard> cards = new ArrayList<>();
        long idSeq = 1;
        for (JlptLevel level : JlptLevel.values()) {
            String lv = level.name();
            List<Category> categories = new ArrayList<>();
            if (level == JlptLevel.N5) {
                categories.add(new Category("KANA", "Hiragana", "あ"));
                categories.add(new Category("KANA", "Katakana", "ア"));
            }
            categories.add(new Category("VOCAB", "Từ vựng " + lv, "語"));
            categories.add(new Category("GRAMMAR", "Ngữ pháp " + lv, "文"));
            categories.add(new Category("KANJI", "Kanji " + lv, "漢"));
            categories.add(new Category("LISTENING", "Nghe hiểu " + lv, "聴"));

            boolean activeAssigned = false;
            for (Category c : categories) {
                double progress = categoryProgress(studentId, level, c.type());
                String status;
                if (progress < 1.0 && !activeAssigned) {
                    status = "active";
                    activeAssigned = true;
                } else {
                    status = "available";
                }
                cards.add(DashboardResponse.LessonCard.builder()
                        .id(idSeq++)
                        .title(c.title())
                        .description(c.title() + " — " + lv)
                        .jlptLevel(lv)
                        .lessonType(c.type())
                        .status(status)
                        .progress(progress)
                        .thumbnail(c.thumbnail())
                        .build());
            }
        }
        return cards;
    }

    private double categoryProgress(Long studentId, JlptLevel level, String type) {
        long completed;
        long total;
        switch (type) {
            case "KANJI" -> {
                completed = progressRepository.countCompletedKanjiByLevel(
                        studentId, level, ContentType.KANJI, ProgressStatus.COMPLETED);
                total = kanjiRepository.countByLevelAndStatus(level, Kanji.ContentStatus.PUBLISHED);
            }
            case "VOCAB" -> {
                completed = progressRepository.countCompletedVocabularyByLevel(
                        studentId, level, ContentType.VOCABULARY, ProgressStatus.COMPLETED);
                total = vocabularyRepository.countPublished(Kanji.ContentStatus.PUBLISHED, level);
            }
            default -> {
                return 0.0;
            }
        }
        if (total <= 0) {
            return 0.0;
        }
        return Math.min(1.0, (double) completed / total);
    }
}
