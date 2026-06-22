/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.learning.dto;

import java.util.List;
import lombok.Builder;
import lombok.Data;

/** UC-09 — Trang danh sách từ vựng cho Student. */
@Data
@Builder
public class VocabularyListResponse {
    private List<VocabularyListItemResponse> content;
    private long totalElements;
    private int totalPages;
    private int page;
    private int size;
    private long completedCount;
}
