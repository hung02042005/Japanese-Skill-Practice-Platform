/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.staffcontent.grammar.exception;

import com.jlpt.shared.exception.BusinessException;

/**
 * UC-25 — Centralised business exception factory for grammar management.
 * HTTP status codes match the error table in UC-25 §7.
 */
public class GrammarBusinessException extends BusinessException {

    private GrammarBusinessException(int status, String errorCode, String message) {
        super(status, errorCode, message);
    }

    /* ── 404 ─────────────────────────────────────────────────────── */

    public static GrammarBusinessException grammarNotFound(Long id) {
        return new GrammarBusinessException(404, "ERR-NF-404", "Không tìm thấy ngữ pháp với id: " + id);
    }

    public static GrammarBusinessException lessonNotFound(Long id) {
        return new GrammarBusinessException(404, "ERR-LESSON-404", "Không tìm thấy bài học với id: " + id);
    }

    /* ── 400 ─────────────────────────────────────────────────────── */

    public static GrammarBusinessException invalidJlptLevel() {
        return new GrammarBusinessException(400, "ERR-LEVEL-400", "Cấp độ JLPT phải là N5 đến N1");
    }

    public static GrammarBusinessException missingRequiredField(String field) {
        return new GrammarBusinessException(400, "ERR-VAL-400", "Thiếu trường bắt buộc: " + field);
    }

    /* ── 403 ─────────────────────────────────────────────────────── */

    public static GrammarBusinessException ownershipDenied() {
        return new GrammarBusinessException(403, "ERR-AUTH-403", "Không có quyền thao tác ngữ pháp này");
    }

    public static GrammarBusinessException publishNotAllowed() {
        return new GrammarBusinessException(403, "ERR-FORBIDDEN-PUBLISH-403", "Staff không được tự xuất bản ngữ pháp");
    }

    /* ── 422 ─────────────────────────────────────────────────────── */

    public static GrammarBusinessException levelMismatch() {
        return new GrammarBusinessException(
                422, "ERR-LEVEL-MISMATCH-422", "Cấp độ JLPT của bài học không khớp với cấp độ ngữ pháp");
    }

    public static GrammarBusinessException editNotAllowedPublished() {
        return new GrammarBusinessException(
                422, "ERR-STATE-EDIT-422", "Không thể sửa ngữ pháp đã xuất bản. Vui lòng tạo phiên bản mới.");
    }

    public static GrammarBusinessException editNotAllowedCurrentStatus(String status) {
        return new GrammarBusinessException(
                422, "ERR-STATE-EDIT2-422", "Không thể sửa ngữ pháp ở trạng thái: " + status);
    }

    public static GrammarBusinessException submitInvalidStatus(String status) {
        return new GrammarBusinessException(
                422, "ERR-STATE-SUBMIT-422", "Không thể gửi duyệt ngữ pháp ở trạng thái: " + status);
    }

    public static GrammarBusinessException submitIncomplete(String field) {
        return new GrammarBusinessException(
                422, "ERR-SUBMIT-INCOMPLETE-422", "Không thể gửi duyệt: thiếu trường bắt buộc: " + field);
    }
}
