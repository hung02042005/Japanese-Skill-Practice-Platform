/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.assessment.dto.response;

import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LegacyQuizResultResponse {
    private Long attemptId;
    private int score;
    private int totalQuestions;
    private int scorePct;
    private List<LegacyQuestionResultResponse> results;
}
