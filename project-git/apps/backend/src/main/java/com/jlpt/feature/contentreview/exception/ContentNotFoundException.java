/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.contentreview.exception;

import com.jlpt.shared.exception.BusinessException;

/** FR-33-07 — Nội dung không tồn tại hoặc đã bị xóa (HTTP 404, CONTENT_NOT_FOUND). */
public class ContentNotFoundException extends BusinessException {
    public ContentNotFoundException(String contentType, Long contentId) {
        super(404, "CONTENT_NOT_FOUND", "Không tìm thấy nội dung yêu cầu phê duyệt: " + contentType + " #" + contentId);
    }
}
