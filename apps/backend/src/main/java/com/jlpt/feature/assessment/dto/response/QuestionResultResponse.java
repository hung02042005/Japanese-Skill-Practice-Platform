/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.assessment.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class QuestionResultResponse {
    private Long questionId;
    private Boolean isCorrect;
    private String selectedOption;
    private String correctOption;
    private String explanation;
}
