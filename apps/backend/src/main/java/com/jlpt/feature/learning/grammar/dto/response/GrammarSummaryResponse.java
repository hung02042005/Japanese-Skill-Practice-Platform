/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.learning.grammar.dto.response;

import lombok.Builder;
import lombok.Data;

/** UC-06 — Item DTO for GET /api/grammar-points (paginated list). Không expose Entity (ADR-005). */
@Data
@Builder
public class GrammarSummaryResponse {

    private Long grammarId;
    private String structure;
    private String formula;
    private String meaning;
    private String jlptLevel;
    private Boolean isCompleted;
}
