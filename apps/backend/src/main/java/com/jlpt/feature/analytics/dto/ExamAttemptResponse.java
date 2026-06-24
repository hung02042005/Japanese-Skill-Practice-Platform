/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.analytics.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

/**
 * Response cho GET /api/analytics/students/{studentId}/exam-history — UC-32 FR-ANALYTICS-12.
 * Paginated list of exam/quiz attempts for a student (Staff/Admin view).
 */
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ExamAttemptResponse {

    private Long attemptId;

    /** exam | quiz | practice | reading | listening */
    private String attemptType;

    /** Tên assessment nếu parentType = ASSESSMENT; null nếu random_practice. */
    private String assessmentTitle;

    private BigDecimal totalScore;
    private BigDecimal maxScore;
    private Boolean isPassed;
    private LocalDateTime startedAt;
    private LocalDateTime submittedAt;
}
