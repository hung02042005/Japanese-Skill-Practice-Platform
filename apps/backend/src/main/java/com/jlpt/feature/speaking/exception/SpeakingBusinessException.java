/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.speaking.exception;

import com.jlpt.shared.exception.BusinessException;

public final class SpeakingBusinessException extends BusinessException {

    private SpeakingBusinessException(int status, String errorCode, String message) {
        super(status, errorCode, message);
    }

    public static SpeakingBusinessException contentNotFound() {
        return new SpeakingBusinessException(404, "CONTENT_NOT_FOUND", "Không tìm thấy bài nói");
    }

    public static SpeakingBusinessException invalidStateTransition() {
        return new SpeakingBusinessException(
                409, "INVALID_STATE_TRANSITION", "Không thể sửa/gửi duyệt ở trạng thái hiện tại");
    }

    public static SpeakingBusinessException invalidLevel() {
        return new SpeakingBusinessException(400, "INVALID_LEVEL", "Cấp độ JLPT không hợp lệ");
    }

    public static SpeakingBusinessException validationFailed() {
        return new SpeakingBusinessException(400, "VALIDATION_FAILED", "Dữ liệu bài nói không hợp lệ");
    }
}
