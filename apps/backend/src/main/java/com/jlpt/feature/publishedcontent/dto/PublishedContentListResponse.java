/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.publishedcontent.dto;

import java.util.List;
import lombok.Builder;
import lombok.Data;

/** UC-34 — Kết quả phân trang danh sách nội dung đã xuất bản (FR-34-04). */
@Data
@Builder
public class PublishedContentListResponse {
    private List<PublishedContentItemResponse> content;
    private long totalElements;
    private int totalPages;
}
