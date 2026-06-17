/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.contentreview.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * UC-33 §6.4 — Body cho {@code POST /api/manager/reviews/request-changes}.
 *
 * <p>{@code targetStatus} ∈ {draft, rejected}, mặc định {@code draft} (FR-33-13/15).
 * {@code feedback} bắt buộc (FR-33-14).
 */
@Data
public class RequestChangesRequest {

    @NotBlank(message = "contentType là bắt buộc")
    private String contentType;

    @NotNull(message = "contentId là bắt buộc") private Long contentId;

    private String targetStatus;

    @NotBlank(message = "feedback là bắt buộc khi yêu cầu chỉnh sửa")
    private String feedback;
}
