/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.learning.kanji.dto.response;

import lombok.Builder;
import lombok.Data;

/** UC-07 — Item DTO for GET /api/kanji (paginated list). Không expose Entity (ADR-005). */
@Data
@Builder
public class KanjiSummaryResponse {

    private Long kanjiId;
    private String characterValue;
    private String meaning;
    private Integer strokeCount;
    private String jlptLevel;
    private Boolean isCompleted;
}
