/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.learning.kana.dto.response;

import java.util.List;
import lombok.Builder;
import lombok.Data;

/** UC-08 — Wrapper response cho GET /api/kana. Chứa danh sách ký tự + thống kê tiến độ. */
@Data
@Builder
public class KanaChartResponse {

    private List<KanaResponse> characters;
    private long completedCount;
    private long totalCount;
}
