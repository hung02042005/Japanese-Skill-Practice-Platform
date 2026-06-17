/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.contentreview.exception;

import com.jlpt.shared.exception.BusinessException;

/** FR-33-14 — Bắt buộc nhập feedback khi REJECT / Request Changes (HTTP 400, FEEDBACK_REQUIRED). */
public class FeedbackRequiredException extends BusinessException {
    public FeedbackRequiredException() {
        super(400, "FEEDBACK_REQUIRED", "Phải nhập lý do khi từ chối hoặc yêu cầu chỉnh sửa");
    }
}
