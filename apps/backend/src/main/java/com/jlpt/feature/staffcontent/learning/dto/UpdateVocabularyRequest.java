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

    @Pattern(regexp = "^(N5|N4|N3|N2|N1)$", message = "Cấp độ JLPT phải là N5 đến N1")
    private String jlptLevel;

    /** Chủ đề mới (topic_id thuộc catalog, đúng cấp độ). Null = giữ nguyên. */
    private Long topicId;

    private String audioUrl;

    private String exampleSentenceJp;

    private String exampleSentenceVi;

    private Long lessonId;

    /** Set true to disassociate from current lesson. */
    private boolean clearLesson;
}
