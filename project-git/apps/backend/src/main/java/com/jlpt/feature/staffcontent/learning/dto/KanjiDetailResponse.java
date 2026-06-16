/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.staffcontent.learning.dto;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

/**
 * UC-27 — Response DTO for a kanji entry (NFR-27-04: never expose the JPA entity).
 */
@Data
@Builder
public class KanjiDetailResponse {

    private Long kanjiId;
    private String characterValue;
    private String meaning;
    private String onyomi;
    private String kunyomi;
    private Integer strokeCount;
    private String jlptLevel;
    private String strokeOrderUrl;
    private String exampleWord;
    private String exampleReading;
    private String exampleMeaning;
    private String status;
    private Long createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
