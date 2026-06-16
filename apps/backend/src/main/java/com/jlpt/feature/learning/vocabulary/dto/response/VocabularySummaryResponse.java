/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.learning.vocabulary.dto.response;

import lombok.Builder;
import lombok.Data;

/** UC-09 — Item DTO for GET /api/vocabulary (paginated list). Không expose Entity (ADR-005). */
@Data
@Builder
public class VocabularySummaryResponse {

    private Long vocabularyId;
    private String word;
    private String furigana;
    private String meaning;
    private String wordType;
    private String jlptLevel;
    private String topic;
    private Boolean isCompleted;
}
