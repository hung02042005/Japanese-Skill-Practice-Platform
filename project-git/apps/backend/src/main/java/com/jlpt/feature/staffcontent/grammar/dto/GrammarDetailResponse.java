/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.staffcontent.grammar.dto;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

/**
 * UC-25 — Full detail DTO for grammar point (GET /api/staff/grammar/{id} and PUT response).
 * Includes lesson summary when linked. Does NOT expose Entity fields (ADR-005).
 */
@Data
@Builder
public class GrammarDetailResponse {

    private Long grammarId;
    private String title;
    private String structure;
    private String formula;
    private String meaning;
    private String usageExplanation;
    private String jlptLevel;
    private String exampleSentenceJp;
    private String exampleSentenceVi;
    private String status;
    private Long createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /** Abbreviated lesson info, null when grammar is not linked to a lesson. */
    private LessonRef lesson;

    @Data
    @Builder
    public static class LessonRef {
        private Long lessonId;
        private String title;
        private String jlptLevel;
    }
}
