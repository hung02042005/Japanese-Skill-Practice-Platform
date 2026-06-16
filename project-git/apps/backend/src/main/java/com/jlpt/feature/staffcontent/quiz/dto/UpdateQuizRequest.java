/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.staffcontent.quiz.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/** UC-26 — Update quiz metadata (FR-26-15..18); assessment_type cannot be changed (FR-26-17). */
@Data
public class UpdateQuizRequest {

    @NotBlank(message = "Thiếu trường bắt buộc: title")
    @Size(max = 255, message = "title không vượt quá 255 ký tự")
    private String title;

    private Long lessonId;

    @Size(max = 100, message = "topic không vượt quá 100 ký tự")
    private String topic;

    @NotBlank(message = "Thiếu trường bắt buộc: jlptLevel")
    private String jlptLevel;

    @NotNull(message = "Thiếu trường bắt buộc: durationMin") @Min(value = 1, message = "durationMin phải > 0")
    private Integer durationMin;

    @NotNull(message = "Thiếu trường bắt buộc: passScore") @Min(value = 0, message = "passScore phải >= 0")
    private Integer passScore;

    @NotNull(message = "Thiếu trường bắt buộc: totalScore") @Min(value = 1, message = "totalScore phải > 0")
    private Integer totalScore;

    /** Optional; rejected if set to published/archived (FR-26-30). */
    private String status;
}
