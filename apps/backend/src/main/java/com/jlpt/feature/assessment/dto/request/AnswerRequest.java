/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.assessment.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AnswerRequest {

    @NotNull(message = "questionId không hợp lệ") @Positive(message = "questionId không hợp lệ") private Long questionId;

    @Pattern(regexp = "[ABCD]", message = "selectedOption phải là A, B, C hoặc D")
    private String selectedOption;

    @Size(max = 1000, message = "Câu trả lời quá dài")
    private String answerText;
}
