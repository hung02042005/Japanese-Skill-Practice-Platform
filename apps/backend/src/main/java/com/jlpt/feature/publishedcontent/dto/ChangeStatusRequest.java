/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.publishedcontent.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * UC-34 — Yêu cầu Unpublish / Archive / Delete (PUT /contents/{id}/status).
 *
 * <p>{@code reason} cố ý KHÔNG gắn {@code @NotBlank}: việc bắt buộc lý do được kiểm ở Service Layer
 * để trả đúng mã lỗi {@code REASON_REQUIRED} (FR-34-11) thay vì lỗi validation chung.
 */
@Data
public class ChangeStatusRequest {

    @NotBlank(message = "Thiếu trường bắt buộc: contentType")
    private String contentType;

    @NotBlank(message = "Thiếu trường bắt buộc: status")
    private String status;

    @Size(max = 500, message = "Lý do tối đa 500 ký tự")
    private String reason;
}
