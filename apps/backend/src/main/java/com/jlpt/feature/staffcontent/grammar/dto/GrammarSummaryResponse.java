/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.staffcontent.grammar.dto;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

/**
 * UC-25 — Lightweight summary DTO for grammar list (GET /api/staff/grammar).
 * Used in paginated list responses (FR-09).
 */
@Data
@Builder
public class GrammarSummaryResponse {

    private Long grammarId;
    private String title;
    private String structure;
    private String meaning;
    private String jlptLevel;
    private String status;
    private Long createdBy;
    private LocalDateTime updatedAt;
}
