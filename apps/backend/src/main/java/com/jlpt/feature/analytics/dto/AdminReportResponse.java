/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.analytics.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDate;
import java.util.List;
import lombok.Builder;
import lombok.Data;

/**
 * Response cho GET /api/analytics/admin/reports — UC-38 FR-ANALYTICS-21/22.
 * Thống kê tổng quát theo khoảng thời gian — tính server-side (không để client tính).
 */
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AdminReportResponse {

    private ReportPeriod period;
    private Long newRegistrations;
    private Long totalExamAttempts;
    private Double avgExamScore;
    private List<LevelCompletionRate> courseCompletionRates;

    /** Khoảng thời gian báo cáo. */
    @Data
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ReportPeriod {
        private LocalDate startDate;
        private LocalDate endDate;
    }

    /** Tỉ lệ hoàn thành theo JLPT level. */
    @Data
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class LevelCompletionRate {
        private String jlptLevel;
        private Long completedStudentsCount;
        private Long totalStudentsCount;

        /** completedStudentsCount / totalStudentsCount × 100. Chia-0 trả 0. */
        private Double completionRate;
    }
}
