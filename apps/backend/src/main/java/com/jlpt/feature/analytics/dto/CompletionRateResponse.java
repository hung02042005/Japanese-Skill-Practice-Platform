/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.analytics.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

/**
 * Response cho GET /api/analytics/completion?contentType=... — UC-19 FR-ANALYTICS-03/04.
 * completionRate = completedCount / totalCount × 100; xử lý chia-0 trả 0.
 */
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CompletionRateResponse {

    /** lesson | vocabulary | kanji | kana | grammar | null = tổng hợp tất cả */
    private String contentType;

    private Long totalCount;
    private Long completedCount;

    /** Tỉ lệ hoàn thành 0-100. Luôn >= 0, division-by-zero trả 0. */
    private Double completionRate;
}
