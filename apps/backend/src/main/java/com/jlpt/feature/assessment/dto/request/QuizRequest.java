/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.assessment.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class QuizRequest {
    @NotBlank(message = "Tiêu đề là bắt buộc")
    private String title;

    private Long lessonId;

    private String topic;

    private String jlptLevel;

    @NotNull(message = "Thời lượng là bắt buộc") private Integer durationMin;

    @NotNull(message = "Điểm đạt là bắt buộc") private Integer passScore;
}
