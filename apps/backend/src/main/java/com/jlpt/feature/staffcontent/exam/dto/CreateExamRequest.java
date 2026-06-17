/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.staffcontent.exam.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * UC-28 — Create exam (FR-28-01..08). Business rules (jlptLevel set, score ranges)
 * are enforced in the service to yield the §7 error codes.
 */
@Data
public class CreateExamRequest {

    /** Ignored — the endpoint always forces assessment_type = 'exam' (FR-28-05). */
    private String assessmentType;

    @NotBlank(message = "Thiếu trường bắt buộc: title")
    @Size(max = 255, message = "title không vượt quá 255 ký tự")
    private String title;

    @NotBlank(message = "Thiếu trường bắt buộc: jlptLevel")
    private String jlptLevel;

    @NotNull(message = "Thiếu trường bắt buộc: durationMin") @Min(value = 1, message = "durationMin phải > 0")
    private Integer durationMin;

    @NotNull(message = "Thiếu trường bắt buộc: passScore") @Min(value = 0, message = "passScore phải >= 0")
    private Integer passScore;

    @NotNull(message = "Thiếu trường bắt buộc: totalScore") @Min(value = 1, message = "totalScore phải > 0")
    private Integer totalScore;

    @Size(max = 500, message = "description không vượt quá 500 ký tự")
    private String description;

    /** Optional; rejected if set to published/archived (FR-28-33). */
    private String status;
}
