/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.contentreview.exception;

import com.jlpt.shared.exception.BusinessException;

/** FR-33-17 — Nguyên tắc bốn mắt: không được tự duyệt nội dung của chính mình (HTTP 403, SELF_REVIEW_DENIED). */
public class SelfReviewNotAllowedException extends BusinessException {
    public SelfReviewNotAllowedException() {
        super(403, "SELF_REVIEW_DENIED", "Nguyên tắc chéo: Không thể tự phê duyệt nội dung của chính mình");
    }
}
