/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.student.dto.response;

import java.util.List;
import lombok.Builder;
import lombok.Data;

/**
 * Bài học kế tiếp + gợi ý cùng cấp độ cho trang "Học Từ Mới" (UC-09).
 * {@code nextLesson} null nếu học viên đã hoàn thành hết bài ở cấp hiện tại.
 */
@Data
@Builder
public class NextLessonResponse {

    private LessonItem nextLesson;
    private List<LessonItem> suggestedLessons;

    @Data
    @Builder
    public static class LessonItem {
        private Long lessonId;
        private String title;
        private String description;
        private String jlptLevel;
        private String lessonType;
        private int estimatedMinutes;
        private int progressPercent;
    }
}
