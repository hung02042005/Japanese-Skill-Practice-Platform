/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.staffcontent.learning.dto;

import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * UC-27 — Request DTO for updating a vocabulary entry.
 * Only non-null fields will be updated (partial update).
 */
@Data
public class UpdateVocabularyRequest {

    private String word;

    private String furigana;

    private String meaning;

    private String wordType;

    @Pattern(regexp = "N[1-5]", message = "JLPT level must be N1–N5")
    private String jlptLevel;

    private String topic;

    private String audioUrl;

    private String exampleSentenceJp;

    private String exampleSentenceVi;

    private Long lessonId;

    /** Set true to disassociate from current lesson. */
    private boolean clearLesson;
}
