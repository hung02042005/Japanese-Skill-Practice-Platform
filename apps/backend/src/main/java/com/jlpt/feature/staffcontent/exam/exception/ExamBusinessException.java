/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.staffcontent.exam.exception;

import com.jlpt.shared.exception.BusinessException;

/**
 * UC-28 — Centralised business exception factory for exam management.
 * Extends the shared {@link BusinessException} so it is handled by the existing
 * GlobalExceptionHandler. HTTP codes match UC-28 §7.
 */
public class ExamBusinessException extends BusinessException {

    private ExamBusinessException(int status, String errorCode, String message) {
        super(status, errorCode, message);
    }

    /* ── 400 ──────────────────────────────────────────────── */

    public static ExamBusinessException validationFailed(String detail) {
        return new ExamBusinessException(400, "VALIDATION_FAILED", detail);
    }

    public static ExamBusinessException invalidJlptLevel() {
        return new ExamBusinessException(400, "INVALID_JLPT_LEVEL", "Cấp độ JLPT phải là N5 đến N1");
    }

    public static ExamBusinessException invalidSection(String section) {
        return new ExamBusinessException(
                400,
                "INVALID_SECTION",
                "Tên section không hợp lệ: " + section
                        + ". Chỉ chấp nhận: vocabulary, grammar, kanji, reading, listening");
    }

    /* ── 403 ──────────────────────────────────────────────── */

    public static ExamBusinessException ownershipDenied() {
        return new ExamBusinessException(403, "FORBIDDEN", "Không có quyền thao tác đề thi này");
    }

    public static ExamBusinessException publishNotAllowed() {
        return new ExamBusinessException(403, "PUBLISH_NOT_ALLOWED", "Staff không được tự xuất bản đề thi");
    }

    /* ── 404 ──────────────────────────────────────────────── */

    public static ExamBusinessException examNotFound(Long id) {
        return new ExamBusinessException(404, "EXAM_NOT_FOUND", "Không tìm thấy đề thi với id: " + id);
    }

    public static ExamBusinessException questionNotFound(Long id) {
        return new ExamBusinessException(404, "QUESTION_NOT_FOUND", "Không tìm thấy câu hỏi cần gán với id: " + id);
    }

    /* ── 409 ──────────────────────────────────────────────── */

    public static ExamBusinessException duplicateAssignment(Long questionId) {
        return new ExamBusinessException(
                409, "DUPLICATE_ASSIGNMENT", "Câu hỏi bị gán trùng trong đề thi: " + questionId);
    }

    public static ExamBusinessException examPublished() {
        return new ExamBusinessException(
                409, "ASSESSMENT_PUBLISHED", "Đề thi đã xuất bản, không thể thay đổi danh sách câu hỏi");
    }

    public static ExamBusinessException invalidStatusTransition(String status) {
        return new ExamBusinessException(
                409, "INVALID_STATUS_TRANSITION", "Không thể thực hiện thao tác ở trạng thái hiện tại: " + status);
    }

    /* ── 422 ──────────────────────────────────────────────── */

    public static ExamBusinessException scoreMismatch() {
        return new ExamBusinessException(
                422, "SCORE_MISMATCH", "Tổng điểm câu hỏi không khớp với total_score của đề thi");
    }

    public static ExamBusinessException levelMismatch(Long questionId, String questionLevel, String examLevel) {
        return new ExamBusinessException(
                422,
                "LEVEL_MISMATCH",
                "Câu hỏi " + questionId + " có cấp độ " + questionLevel + " không khớp với cấp độ đề thi " + examLevel);
    }

    public static ExamBusinessException questionNotPublished(Long id) {
        return new ExamBusinessException(
                422, "QUESTION_NOT_PUBLISHED", "Chỉ được gán câu hỏi đã xuất bản. Câu hỏi không hợp lệ: " + id);
    }

    public static ExamBusinessException emptyExam() {
        return new ExamBusinessException(422, "EMPTY_EXAM", "Đề thi chưa có câu hỏi nào");
    }
}
