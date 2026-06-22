package com.jlpt.feature.student.kanji.dto;

import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class KanjiListResponse {
    private List<KanjiItemResponse> content;
    private int totalPages;
    private long totalElements;
    private int page;
    private int size;
    private long completedCount;
}
