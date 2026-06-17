/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.contentreview.dto;

import java.util.List;
import lombok.Builder;
import lombok.Getter;

/** UC-33 §6.1 — Bao kết quả phân trang cho hàng đợi duyệt. */
@Getter
@Builder
public class ReviewQueueResponse {
    private final List<ReviewQueueItemResponse> content;
    private final long totalElements;
    private final int totalPages;
}
