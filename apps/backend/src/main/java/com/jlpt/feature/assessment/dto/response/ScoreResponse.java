/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.assessment.dto.response;

import java.math.BigDecimal;
import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ScoreResponse {
    private Long attemptId;
    private BigDecimal score;
    private BigDecimal maxScore;
    private Boolean isPassed;
    private List<QuestionResultResponse> results;
}
