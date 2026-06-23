/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.staffcontent.student.dto;

import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;
import lombok.Data;

/** Tiến độ học của một học viên (Staff xem). */
@Data
@Builder
public class StaffStudentProgressResponse {
    private Long studentId;
    private String fullName;
    private String jlptLevel;
    private int currentStreak;
    private long lessonsCompleted;
    private int averageQuizScore; // % trung bình các lần thi đã nộp
    private List<AttemptItem> recentAttempts;

    @Data
    @Builder
    public static class AttemptItem {
        private Long attemptId;
        private String title;
        private Integer score;
        private Integer maxScore;
        private int scorePct;
        private LocalDateTime takenAt;
    }
}
