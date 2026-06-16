/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.publishedcontent.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/** UC-34 — Yêu cầu khôi phục nội dung archived (POST /contents/{id}/restore). */
@Data
public class RestoreContentRequest {

    @NotBlank(message = "Thiếu trường bắt buộc: contentType")
    private String contentType;
}
