/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.assessment.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

/** Adapter DTO khớp contract cũ {questionId, selectedOptionId} của studentService.js. */
@Data
public class LegacyAnswerRequest {

    @NotNull(message = "questionId không hợp lệ") @Positive(message = "questionId không hợp lệ") private Long questionId;

    @Min(value = 1, message = "selectedOptionId phải từ 1 đến 4")
    @Max(value = 4, message = "selectedOptionId phải từ 1 đến 4")
    private Integer selectedOptionId;
}
