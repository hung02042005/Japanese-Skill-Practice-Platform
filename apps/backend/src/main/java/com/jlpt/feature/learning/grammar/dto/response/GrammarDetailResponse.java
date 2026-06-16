/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.learning.grammar.dto.response;

import lombok.Builder;
import lombok.Data;

/** UC-06 — Detail DTO for GET /api/grammar-points/{grammarId}. Không expose Entity (ADR-005). */
@Data
@Builder
public class GrammarDetailResponse {

    private Long grammarId;
    private String structure;
    private String formula;
    private String meaning;
    private String usageExplanation;
    private String jlptLevel;
    private String exampleSentenceJp;
    private String exampleSentenceVi;

    /** null khi Student chưa có tiến độ cho điểm ngữ pháp này. */
    private String progressStatus;
}
