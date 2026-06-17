/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.staffcontent.learning.dto;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

/**
 * UC-27 — Response DTO for a vocabulary entry (NFR-27-04: never expose the JPA entity).
 */
@Data
@Builder
public class VocabularyDetailResponse {

    private Long vocabularyId;
    private String word;
    private String furigana;
    private String meaning;
    private String wordType;
    private String jlptLevel;
    private String topic;
    private String audioUrl;
    private String exampleSentenceJp;
    private String exampleSentenceVi;
    private Long lessonId;
    private String status;
    private Long createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
