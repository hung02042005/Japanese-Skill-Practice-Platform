/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.staffcontent.quiz.dto;

import java.math.BigDecimal;
import lombok.Builder;
import lombok.Data;

/** UC-26 — Result of assign-questions (API §6.5). */
@Data
@Builder
public class AssignResultResponse {

    private Long assessmentId;
    private int assignedCount;
    private BigDecimal assignedScoreSum;
    private Integer totalScore;
    private boolean scoreMatched;
}
