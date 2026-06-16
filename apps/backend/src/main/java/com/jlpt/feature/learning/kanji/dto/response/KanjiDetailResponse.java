/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.learning.kanji.dto.response;

import lombok.Builder;
import lombok.Data;

/** UC-07 — Detail DTO for GET /api/kanji/{kanjiId}. Không expose Entity (ADR-005). */
@Data
@Builder
public class KanjiDetailResponse {

    private Long kanjiId;
    private String characterValue;
    private Integer strokeCount;

    /** BR-07-02 — luôn là ảnh tĩnh, không phải animation. */
    private String strokeOrderUrl;

    private String onyomi;
    private String kunyomi;
    private String meaning;
    private String exampleWord;
    private String exampleReading;
    private String exampleMeaning;
    private String jlptLevel;

    /** null khi Student chưa có tiến độ cho Kanji này. */
    private String progressStatus;

    /** null khi đây là Kanji đầu/cuối trong level — dùng cho điều hướng Tiếp theo/Trước. */
    private Long prevKanjiId;

    private Long nextKanjiId;
}
