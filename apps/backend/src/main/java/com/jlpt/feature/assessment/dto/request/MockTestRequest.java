/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.assessment.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MockTestRequest {
    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "JLPT Level is required")
    private String jlptLevel;

    @NotNull(message = "Duration is required") private Integer durationMin;

    @NotNull(message = "Pass score is required") private Integer passScore;

    private String audioUrl;
}
