/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.learning.vocabulary.dto.response;

import lombok.Builder;
import lombok.Data;

/** UC-09 — Progress summary DTO for GET /api/vocabulary/progress-summary. */
@Data
@Builder
public class VocabularyProgressSummaryResponse {

    private String jlptLevel;
    private long completed;
    private long total;
}
