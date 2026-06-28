/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.student.dto.response;

import lombok.Builder;
import lombok.Data;

/** Thống kê học tập tổng quan của học viên (trang Tiến độ). */
@Data
@Builder
public class StudentStatsResponse {
    private int currentStreak;
    private int longestStreak;
    private long wordCount;
    private long lessonsCompleted;
    private long daysThisMonth;
}
