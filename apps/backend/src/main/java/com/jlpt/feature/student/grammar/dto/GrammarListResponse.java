/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.student.grammar.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GrammarListResponse {
    private List<GrammarSummaryResponse> content;
    private long totalElements;
    private int totalPages;
    private int page;
    private int size;
}
