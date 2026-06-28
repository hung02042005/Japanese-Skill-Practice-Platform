/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.student.kanji.dto;

import lombok.Builder;
import lombok.Data;

/** Tóm tắt tiến độ học Kanji theo cấp độ cho badge ở danh sách Kanji (FE: { completed, total }). */
@Data
@Builder
public class KanjiProgressSummaryResponse {
    private long completed;
    private long total;
}
