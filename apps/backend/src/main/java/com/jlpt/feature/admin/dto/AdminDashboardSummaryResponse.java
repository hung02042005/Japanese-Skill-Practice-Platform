/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.admin.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AdminDashboardSummaryResponse {
    private Long totalUsers;
    private Long activeToday;
    private Long quizAttemptsToday;
    private String systemStatus;
}
