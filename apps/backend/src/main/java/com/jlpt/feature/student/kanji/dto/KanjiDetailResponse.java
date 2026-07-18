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
public class KanjiDetailResponse {
    private Long kanjiId;
    private String characterValue;
    private Integer strokeCount;
    private String strokeOrderUrl;
    private String radical;
    private String onyomi;
    private String kunyomi;
    private String meaning;
    private String jlptLevel;
    private String exampleWord;
    private String exampleReading;
    private String exampleMeaning;

    @com.fasterxml.jackson.annotation.JsonProperty("isCompleted")
    private boolean isCompleted;

    private String progressStatus;
    private Long prevKanjiId;
    private Long nextKanjiId;
}
