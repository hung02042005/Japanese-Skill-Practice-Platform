/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.analytics.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;
import lombok.Builder;
import lombok.Data;

/**
 * Response cho GET /api/analytics/quizzes/{assessmentId}/stats — UC-32 FR-ANALYTICS-10/11.
 * Per-question accuracy tính server-side (KHÔNG để client tính).
 */
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class QuizStatsResponse {

    private Long assessmentId;
    private String title;
    private Integer totalAttempts;
    private Double averageScore;
    private Double maxScore;

    /** Tỉ lệ pass (%) — dựa trên isPassed = true. */
    private Double passRate;

    /** Độ chính xác từng câu hỏi — tính server-side. */
    private List<QuestionAccuracyItem> questionAccuracy;

    /** Per-question accuracy item — inner class. */
    @Data
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class QuestionAccuracyItem {
        private Long questionId;
        private String questionText;
        private Integer correctCount;
        private Integer incorrectCount;

        /** correctCount / totalAttempts × 100. Chia-0 trả 0. */
        private Double accuracyPercent;
    }
}
