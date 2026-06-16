/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.staffcontent.learning.dto;

import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * UC-27 — Request DTO for updating a kanji entry.
 * Only non-null fields will be updated (partial update).
 */
@Data
public class UpdateKanjiRequest {

    private String characterValue;

    private String meaning;

    private String onyomi;

    private String kunyomi;

    private Integer strokeCount;

    @Pattern(regexp = "N[1-5]", message = "JLPT level must be N1–N5")
    private String jlptLevel;

    private String strokeOrderUrl;

    private String exampleWord;

    private String exampleReading;

    private String exampleMeaning;
}
