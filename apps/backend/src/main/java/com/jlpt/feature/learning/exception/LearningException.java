/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.learning.exception;

import com.jlpt.shared.exception.BusinessException;

/**
 * UC-06/UC-07 — Centralised business exception factory for student-facing learning content
 * (grammar/kanji list/detail, learning-progress marking, flashcards). Error codes match
 * UC-06 §3, §5 and UC-07 §3.2.
 */
public class LearningException extends BusinessException {

    private LearningException(int status, String errorCode, String message) {
        super(status, errorCode, message);
    }

    public static LearningException levelMismatch() {
        return new LearningException(422, "LEVEL_MISMATCH", "Cấp độ JLPT không hợp lệ");
    }

    public static LearningException contentNotFound() {
        return new LearningException(404, "CONTENT_NOT_FOUND", "Nội dung không tồn tại");
    }

    public static LearningException validationFailed(String field) {
        return new LearningException(400, "VALIDATION_FAILED", "Dữ liệu không hợp lệ: " + field);
    }

    public static LearningException progressRegression() {
        return new LearningException(422, "PROGRESS_REGRESSION", "Không thể hạ tiến độ đã đạt");
    }

    public static LearningException flashcardExists() {
        return new LearningException(409, "FLASHCARD_EXISTS", "Nội dung đã có trong Flashcard");
    }
}
