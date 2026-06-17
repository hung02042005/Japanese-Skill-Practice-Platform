/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.publishedcontent.exception;

/**
 * UC-34 — Chuyển trạng thái không hợp lệ (HTTP 409, INVALID_STATE_TRANSITION — FR-34-20).
 *
 * <p>Ví dụ: unpublish/archive/delete một item không phải {@code published}, hoặc restore một
 * item không phải {@code archived}; cũng dùng khi guarded update trả 0 dòng (đổi trạng thái đồng thời).
 */
public class InvalidStateTransitionException extends RuntimeException {
    public InvalidStateTransitionException() {
        super("Chuyển trạng thái không hợp lệ");
    }
}
