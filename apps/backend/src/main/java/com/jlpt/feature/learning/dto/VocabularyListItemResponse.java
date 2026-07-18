/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.learning.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

/** UC-09 — Một mục từ vựng trong danh sách học (Student). Không lộ entity (ADR-005). */
@Data
@Builder
public class VocabularyListItemResponse {
    private Long id;
    private String word;
    private String furigana;
    private String meaning;
    private String wordType;
    private String jlptLevel;
    private Long topicId;
    private String audioUrl;
    private String exampleSentenceJp;
    private String exampleSentenceVi;

    @JsonProperty("isCompleted")
    private boolean isCompleted;
}
