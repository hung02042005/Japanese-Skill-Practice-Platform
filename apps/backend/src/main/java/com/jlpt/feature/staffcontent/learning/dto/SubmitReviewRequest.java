/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.staffcontent.learning.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * UC-27 — Request DTO for submitting content for review (POST /api/staff/contents/submit-review).
 * FR-27-25: contentType ∈ {lesson, vocabulary, kanji} with a valid contentId.
 */
@Data
public class SubmitReviewRequest {

    @NotBlank(message = "Thiếu trường bắt buộc: contentType")
    @Pattern(regexp = "^(lesson|vocabulary|kanji)$", message = "contentType phải là lesson, vocabulary hoặc kanji")
    private String contentType;

    @NotNull(message = "Thiếu trường bắt buộc: contentId") private Long contentId;
}
