package com.jlpt.feature.student.reading.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReadingAnswerRequest {

    @NotNull(message = "questionId is required")
    private Long questionId;

    @NotNull(message = "selectedOption is required")
    @Pattern(regexp = "^[A-D]$", message = "selectedOption must be A, B, C, or D")
    private String selectedOption;
    
    private String answerText;
}
