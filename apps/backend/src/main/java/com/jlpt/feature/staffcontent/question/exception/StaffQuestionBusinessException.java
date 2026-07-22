/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.staffcontent.question.exception;

import com.jlpt.shared.exception.BusinessException;
import lombok.Getter;

/**
 * UC-24 — Single business exception class for all question-bank error codes.
 * Factory methods provide consistent error codes and messages per the spec (Section 7).
 */
@Getter
public class StaffQuestionBusinessException extends BusinessException {

    private StaffQuestionBusinessException(int status, String errorCode, String message) {
        super(status, errorCode, message);
    }

    // -- 404 --

    public static StaffQuestionBusinessException questionNotFound(Long id) {
        return new StaffQuestionBusinessException(404, "QUESTION_NOT_FOUND", "Không tìm thấy câu hỏi với id: " + id);
    }

    // -- 400 --

    public static StaffQuestionBusinessException missingOptions() {
        return new StaffQuestionBusinessException(
                400, "MISSING_OPTIONS", "Câu trắc nghiệm phải có đủ 4 đáp án A/B/C/D và correct_option");
    }

    public static StaffQuestionBusinessException invalidCorrectOption() {
        return new StaffQuestionBusinessException(400, "VALIDATION_FAILED", "correctOption chỉ được A, B, C hoặc D");
    }

    // -- 403 --

    public static StaffQuestionBusinessException forbidden(String message) {
        return new StaffQuestionBusinessException(403, "FORBIDDEN", message);
    }

    public static StaffQuestionBusinessException publishNotAllowed() {
        return new StaffQuestionBusinessException(403, "PUBLISH_NOT_ALLOWED", "Staff không được tự xuất bản câu hỏi");
    }

    // -- 409 --

    public static StaffQuestionBusinessException questionLocked() {
        return new StaffQuestionBusinessException(
                409, "QUESTION_LOCKED", "Câu hỏi đã được học viên làm, không thể sửa trực tiếp");
    }

    public static StaffQuestionBusinessException invalidStatusTransition() {
        return new StaffQuestionBusinessException(
                409, "INVALID_STATUS_TRANSITION", "Không thể thực hiện thao tác ở trạng thái hiện tại");
    }
}
