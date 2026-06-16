/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.learning.kanji.dto.response;

import lombok.Builder;
import lombok.Data;

/** UC-07 — Tổng số Kanji đã hoàn thành / tổng số Kanji của một level (toàn bộ, không theo trang). */
@Data
@Builder
public class KanjiProgressSummaryResponse {

    private String jlptLevel;
    private long completed;
    private long total;
}
