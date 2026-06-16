/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.staffcontent.quiz.exception;

import com.jlpt.shared.exception.BusinessException;

/**
 * UC-26 — Centralised business exception factory for quiz management.
 * Extends the shared {@link BusinessException} so it is handled by the existing GlobalExceptionHandler
 * (returns {@code {status, message, data}}) without modifying it. HTTP codes match UC-26 §7.
 */
public class QuizBusinessException extends BusinessException {

    private QuizBusinessException(int status, String errorCode, String message) {
        super(status, errorCode, message);
    }

    /* ── 400 ─────────────────────────────────────────────────────── */

    public static QuizBusinessException validationFailed(String detail) {
        return new QuizBusinessException(400, "VALIDATION_FAILED", detail);
    }

    public static QuizBusinessException invalidJlptLevel() {
        return new QuizBusinessException(400, "INVALID_JLPT_LEVEL", "Cấp độ JLPT phải là N5 đến N1");
    }

    /* ── 403 ─────────────────────────────────────────────────────── */

    public static QuizBusinessException ownershipDenied() {
        return new QuizBusinessException(403, "FORBIDDEN", "Không có quyền thao tác bài trắc nghiệm này");
    }

    public static QuizBusinessException publishNotAllowed() {
        return new QuizBusinessException(403, "PUBLISH_NOT_ALLOWED", "Staff không được tự xuất bản bài trắc nghiệm");
    }

    /* ── 404 ─────────────────────────────────────────────────────── */

    public static QuizBusinessException assessmentNotFound(Long id) {
        return new QuizBusinessException(404, "ASSESSMENT_NOT_FOUND", "Không tìm thấy bài trắc nghiệm với id: " + id);
    }

    public static QuizBusinessException lessonNotFound(Long id) {
        return new QuizBusinessException(404, "LESSON_NOT_FOUND", "Không tìm thấy bài học với id: " + id);
    }

    public static QuizBusinessException questionNotFound(Long id) {
        return new QuizBusinessException(404, "QUESTION_NOT_FOUND", "Không tìm thấy câu hỏi cần gán với id: " + id);
    }

    /* ── 409 ─────────────────────────────────────────────────────── */

    public static QuizBusinessException duplicateAssignment(Long questionId) {
        return new QuizBusinessException(
                409, "DUPLICATE_ASSIGNMENT", "Câu hỏi bị gán trùng trong bài trắc nghiệm: " + questionId);
    }

    public static QuizBusinessException assessmentPublished() {
        return new QuizBusinessException(
                409, "ASSESSMENT_PUBLISHED", "Bài trắc nghiệm đã xuất bản, không thể thay đổi danh sách câu hỏi");
    }

    public static QuizBusinessException invalidStatusTransition(String status) {
        return new QuizBusinessException(
                409, "INVALID_STATUS_TRANSITION", "Không thể thực hiện thao tác ở trạng thái hiện tại: " + status);
    }

    /* ── 422 ─────────────────────────────────────────────────────── */

    public static QuizBusinessException scoreMismatch() {
        return new QuizBusinessException(
                422, "SCORE_MISMATCH", "Tổng điểm câu hỏi không khớp với total_score của bài trắc nghiệm");
    }

    public static QuizBusinessException questionNotPublished(Long id) {
        return new QuizBusinessException(
                422, "QUESTION_NOT_PUBLISHED", "Chỉ được gán câu hỏi đã xuất bản. Câu hỏi không hợp lệ: " + id);
    }

    public static QuizBusinessException emptyQuiz() {
        return new QuizBusinessException(422, "EMPTY_QUIZ", "Bài trắc nghiệm chưa có câu hỏi nào");
    }
}
