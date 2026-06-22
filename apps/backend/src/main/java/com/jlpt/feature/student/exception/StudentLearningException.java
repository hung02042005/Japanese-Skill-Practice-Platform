/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.student.exception;

import com.jlpt.shared.exception.BusinessException;

/**
 * UC-06 — Centralised exceptions for student learning features.
 */
public class StudentLearningException extends BusinessException {

    private StudentLearningException(int status, String errorCode, String message) {
        super(status, errorCode, message);
    }

    public static StudentLearningException levelMismatch() {
        return new StudentLearningException(422, "LEVEL_MISMATCH", "Cấp độ JLPT không hợp lệ");
    }

    public static StudentLearningException contentNotFound() {
        return new StudentLearningException(404, "CONTENT_NOT_FOUND", "Nội dung không tồn tại");
    }

    public static StudentLearningException validationFailed(String field) {
        return new StudentLearningException(400, "VALIDATION_FAILED", "Dữ liệu không hợp lệ: " + field);
    }

    public static StudentLearningException progressRegression() {
        return new StudentLearningException(422, "PROGRESS_REGRESSION", "Không thể hạ tiến độ đã đạt");
    }

    public static StudentLearningException vipRequired() {
        return new StudentLearningException(403, "VIP_REQUIRED", "Nội dung này yêu cầu tài khoản VIP");
    }
}
