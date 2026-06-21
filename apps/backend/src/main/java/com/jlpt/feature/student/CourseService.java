/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.student;

import com.jlpt.feature.learning.Lesson;
import com.jlpt.feature.learning.LessonRepository;
import com.jlpt.feature.student.StudentUser.JlptLevel;
import com.jlpt.feature.student.dto.response.CourseListResponse;
import com.jlpt.shared.exception.ResourceNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Dựng danh sách khoá học theo cấp độ JLPT cho Student — SPEC-course-list UC-08/UC-09.
 *
 * <p>Mỗi cấp độ (N5→N1) là một "khoá học". Nội dung tĩnh (title/description) được sinh tại đây vì
 * hệ thống chưa có entity Course riêng; tiến độ ({@code completed/total}) lấy từ bảng lessons +
 * student_content_progress. VIP/subscription đã được bỏ khỏi phạm vi → mọi khoá đều mở.
 */
@Service
@RequiredArgsConstructor
public class CourseService {

    /** Thứ tự hiển thị N5 → N1 (FR-CL-03). */
    private static final JlptLevel[] LEVEL_ORDER = {JlptLevel.N5, JlptLevel.N4, JlptLevel.N3, JlptLevel.N2, JlptLevel.N1
    };

    private static final Map<JlptLevel, String> DESCRIPTIONS = Map.of(
            JlptLevel.N5, "Hiragana, Katakana, từ vựng & ngữ pháp cơ bản",
            JlptLevel.N4, "Giao tiếp hàng ngày, mở rộng từ vựng & Kanji",
            JlptLevel.N3, "Trung cấp: cấu trúc phức hợp, đọc hiểu thực tế",
            JlptLevel.N2, "Cao cấp & học thuật",
            JlptLevel.N1, "Trình độ bản ngữ");

    private final StudentUserRepository studentUserRepository;
    private final LessonRepository lessonRepository;
    private final StudentContentProgressRepository progressRepository;

    @Transactional(readOnly = true)
    public CourseListResponse getCourses(Long studentId) {
        StudentUser student = studentUserRepository
                .findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy học viên"));

        JlptLevel currentLevel = student.getCurrentJlptLevel() != null ? student.getCurrentJlptLevel() : JlptLevel.N5;

        List<CourseListResponse.CourseItem> courses = new ArrayList<>(LEVEL_ORDER.length);
        for (JlptLevel level : LEVEL_ORDER) {
            long total = lessonRepository.countByJlptLevelAndStatus(level, Lesson.LessonStatus.PUBLISHED);
            long completed = progressRepository.countCompletedLessonsInLevel(
                    studentId,
                    StudentContentProgress.ContentType.LESSON,
                    StudentContentProgress.ProgressStatus.COMPLETED,
                    level,
                    Lesson.LessonStatus.PUBLISHED);

            courses.add(CourseListResponse.CourseItem.builder()
                    .jlptLevel(level.name())
                    .title("Tiếng Nhật " + level.name())
                    .description(DESCRIPTIONS.getOrDefault(level, ""))
                    .completedLessons(completed)
                    .totalLessons(total)
                    .build());
        }

        return CourseListResponse.builder()
                .currentLevel(currentLevel.name())
                .courses(courses)
                .build();
    }
}
