/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.publishedcontent.dto;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

/** UC-34 — Một dòng trong danh sách nội dung đã xuất bản (FR-34-03/04). */
@Data
@Builder
public class PublishedContentItemResponse {
    private Long contentId;
    private String contentType;
    private String titleOrText;
    private String jlptLevel;
    private String status;
    private LocalDateTime publishedAt;
}
