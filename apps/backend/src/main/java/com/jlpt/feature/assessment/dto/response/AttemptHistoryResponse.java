/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.assessment.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AttemptHistoryResponse {
    private Long attemptId;
    private String assessmentTitle;
    private BigDecimal score;
    private BigDecimal maxScore;
    private Boolean isPassed;
    private LocalDateTime submittedAt;
    private Integer durationSeconds;
}
