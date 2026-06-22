/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.assessment.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LegacyQuestionResultResponse {
    private Long questionId;

    @JsonProperty("isCorrect")
    private boolean isCorrect;

    private Integer selectedOptionId;
    private Integer correctOptionId;
    private String explanation;
}
