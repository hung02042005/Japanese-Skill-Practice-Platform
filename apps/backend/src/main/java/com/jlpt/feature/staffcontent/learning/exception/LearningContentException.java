/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.staffcontent.learning.exception;

import com.jlpt.shared.exception.BusinessException;

/**
 * UC-27 — Centralised business exception factory for learning-content management
 * (lesson / vocabulary / kanji). HTTP status codes match the error table in UC-27 §7.
 */
public class LearningContentException extends BusinessException {

    private LearningContentException(int status, String errorCode, String message) {
        super(status, errorCode, message);
    }

    /* ── 400 ─────────────────────────────────────────────────────── */

    public static LearningContentException missingField(String field) {
        return new LearningContentException(400, "VALIDATION_FAILED", "Thiếu trường bắt buộc: " + field);
    }

    public static LearningContentException invalidJlptLevel() {
        return new LearningContentException(400, "INVALID_JLPT_LEVEL", "Cấp độ JLPT phải là N5 đến N1");
    }

    public static LearningContentException invalidLessonType() {
        return new LearningContentException(400, "INVALID_LESSON_TYPE", "lesson_type không hợp lệ");
    }

    public static LearningContentException lessonContentRequired() {
        return new LearningContentException(
                400, "LESSON_CONTENT_REQUIRED", "Học liệu phải có nội dung văn bản hoặc media URL");
    }

    public static LearningContentException invalidContentType() {
        return new LearningContentException(
                400, "VALIDATION_FAILED", "contentType phải là lesson, vocabulary hoặc kanji");
    }

    /* ── 403 ─────────────────────────────────────────────────────── */

    public static LearningContentException ownershipDenied() {
        return new LearningContentException(403, "FORBIDDEN", "Không có quyền thao tác học liệu này");
    }

    public static LearningContentException publishNotAllowed() {
        return new LearningContentException(403, "PUBLISH_NOT_ALLOWED", "Staff không được tự xuất bản nội dung");
    }

    /* ── 404 ─────────────────────────────────────────────────────── */

    public static LearningContentException lessonNotFound() {
        return new LearningContentException(404, "LESSON_NOT_FOUND", "Không tìm thấy học liệu");
    }

    public static LearningContentException contentNotFound() {
        return new LearningContentException(404, "CONTENT_NOT_FOUND", "Không tìm thấy nội dung yêu cầu");
    }

    /* ── 409 ─────────────────────────────────────────────────────── */

    public static LearningContentException kanjiDuplicate() {
        return new LearningContentException(409, "KANJI_DUPLICATE", "Chữ Kanji đã tồn tại");
    }

    public static LearningContentException invalidStatusTransition() {
        return new LearningContentException(
                409, "INVALID_STATUS_TRANSITION", "Không thể thực hiện thao tác ở trạng thái hiện tại");
    }
}
