/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.contentreview.exception;

import com.jlpt.shared.exception.BusinessException;

/**
 * FR-33-10 / FR-33-19 — Nội dung không còn ở {@code pending_review} khi commit
 * (đã được StaffManager khác xử lý) → HTTP 409, CONCURRENT_REVIEW.
 */
public class ConcurrentReviewException extends BusinessException {
    public ConcurrentReviewException() {
        super(409, "CONCURRENT_REVIEW", "Nội dung này đã được xử lý bởi một StaffManager khác");
    }
}
