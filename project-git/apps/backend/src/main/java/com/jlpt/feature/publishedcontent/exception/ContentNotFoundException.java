/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.publishedcontent.exception;

import com.jlpt.shared.exception.BusinessException;

/** UC-34 — Nội dung không tồn tại (HTTP 404, CONTENT_NOT_FOUND — FR-34-08). */
public class ContentNotFoundException extends BusinessException {
    public ContentNotFoundException(String contentType, Long contentId) {
        super(404, "CONTENT_NOT_FOUND", "Không tìm thấy nội dung: " + contentType + " #" + contentId);
    }
}
