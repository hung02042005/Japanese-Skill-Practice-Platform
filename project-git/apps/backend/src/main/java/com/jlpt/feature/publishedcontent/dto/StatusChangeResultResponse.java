/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.publishedcontent.dto;

import lombok.Builder;
import lombok.Data;

/** UC-34 — Kết quả đổi trạng thái / khôi phục (FR-34-13, FR-34-18). */
@Data
@Builder
public class StatusChangeResultResponse {
    private Long contentId;
    private String contentType;
    private String status;
}
