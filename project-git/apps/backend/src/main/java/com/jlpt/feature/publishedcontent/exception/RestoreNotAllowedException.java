/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.publishedcontent.exception;

/** UC-34 — Cố khôi phục nội dung đã {@code deleted} (HTTP 409, RESTORE_NOT_ALLOWED — FR-34-19). */
public class RestoreNotAllowedException extends RuntimeException {
    public RestoreNotAllowedException() {
        super("Nội dung đã bị xóa không thể khôi phục (trạng thái cuối)");
    }
}
