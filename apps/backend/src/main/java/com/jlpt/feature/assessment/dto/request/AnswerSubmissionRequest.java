/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.assessment.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AnswerSubmissionRequest {
    @NotNull(message = "Question ID is required") private Long questionId;

    private String selectedOption;
    private String answerText;
}
