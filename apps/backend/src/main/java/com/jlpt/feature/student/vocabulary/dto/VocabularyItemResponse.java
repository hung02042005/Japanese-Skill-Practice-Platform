package com.jlpt.feature.student.vocabulary.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class VocabularyItemResponse {
    private Long id;
    private String word;
    private String furigana;
    private String meaning;
    private String wordType;
    private String jlptLevel;
    private String topic;
    @JsonProperty("isCompleted")
    private boolean isCompleted;
    @JsonProperty("isInFlashcard")
    private boolean isInFlashcard;
}
