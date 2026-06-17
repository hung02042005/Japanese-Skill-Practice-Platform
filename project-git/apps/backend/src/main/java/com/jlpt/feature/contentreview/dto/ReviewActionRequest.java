/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.contentreview.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * UC-33 §6.3 — Body cho {@code POST /api/manager/reviews} (Approve / Reject).
 *
 * <p>{@code feedback} bắt buộc khi {@code action=REJECT} — kiểm tra ở Service (FR-33-14),
 * vì với {@code APPROVE} feedback là tùy chọn.
 */
@Data
public class ReviewActionRequest {

    @NotBlank(message = "contentType là bắt buộc")
    private String contentType;

    @NotNull(message = "contentId là bắt buộc") private Long contentId;

    @NotBlank(message = "action là bắt buộc (APPROVE | REJECT)")
    private String action;

    private String feedback;
}
