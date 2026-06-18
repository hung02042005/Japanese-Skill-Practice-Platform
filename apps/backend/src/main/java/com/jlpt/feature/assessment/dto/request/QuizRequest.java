/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.assessment.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class QuizRequest {
    @NotBlank(message = "Title is required")
    private String title;

    private Long lessonId;

    private String topic;

    private String jlptLevel;

    @NotNull(message = "Duration is required") private Integer durationMin;

    @NotNull(message = "Pass score is required") private Integer passScore;
}
