/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.student.dto.response;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Payload cho trang Danh sách khoá học theo cấp độ JLPT (SPEC-course-list §13, FR-CL-01).
 *
 * <p>Shape: {@code { currentLevel, courses[] }}. Mỗi "khoá học" tương ứng một cấp độ JLPT
 * (N5→N1). VIP/subscription đã được bỏ khỏi phạm vi → không có trường {@code vipOnly}.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseListResponse {

    private String currentLevel; // cấp độ JLPT hiện tại của student (đánh dấu "Đang học")
    private List<CourseItem> courses;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CourseItem {
        private String jlptLevel; // "N5".."N1"
        private String title;
        private String description;
        private long completedLessons;
        private long totalLessons;
    }
}
