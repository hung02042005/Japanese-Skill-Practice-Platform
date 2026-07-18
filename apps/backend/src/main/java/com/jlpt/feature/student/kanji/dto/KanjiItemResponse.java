/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.student.kanji.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KanjiItemResponse {
    private Long kanjiId;
    private String characterValue;
    private String meaning;
    private String onyomi;
    private String kunyomi;
    private Integer strokeCount;
    private String jlptLevel;

    @com.fasterxml.jackson.annotation.JsonProperty("isCompleted")
    private boolean isCompleted;
}
