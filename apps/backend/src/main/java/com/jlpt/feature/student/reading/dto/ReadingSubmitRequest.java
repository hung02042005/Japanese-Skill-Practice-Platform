/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.student.reading.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReadingSubmitRequest {

    @NotBlank(message = "attemptType is required")
    @Pattern(regexp = "^reading$", message = "attemptType must be 'reading'")
    private String attemptType;

    @NotEmpty(message = "answers list cannot be empty")
    @Valid
    private List<ReadingAnswerRequest> answers;
}
