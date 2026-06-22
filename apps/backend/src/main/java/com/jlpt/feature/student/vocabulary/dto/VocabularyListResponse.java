package com.jlpt.feature.student.vocabulary.dto;

import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class VocabularyListResponse {
    private List<VocabularyItemResponse> content;
    private int totalPages;
    private long totalElements;
    private int page;
    private int size;
    private long completedCount;
}
