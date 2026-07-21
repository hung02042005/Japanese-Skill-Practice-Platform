/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.contentreview.service;

import com.jlpt.feature.contentreview.model.ContentType;

import com.jlpt.feature.contentreview.handler.ReviewableContentHandler;
import com.jlpt.shared.exception.BusinessException;
import java.util.Collection;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

/**
 * UC-33 — Ánh xạ {@link ContentType} → {@link ReviewableContentHandler} (PLAN §3).
 *
 * <p>{@link ContentType#COURSE} dùng chung bảng {@code lessons} nên được alias tới handler
 * của {@link ContentType#LESSON}.
 */
@Component
public class ReviewableContentResolver {

    private final Map<ContentType, ReviewableContentHandler> handlers = new EnumMap<>(ContentType.class);

    public ReviewableContentResolver(List<ReviewableContentHandler> handlerBeans) {
        for (ReviewableContentHandler handler : handlerBeans) {
            handlers.put(handler.type(), handler);
        }
        // "course" là bí danh nghiệp vụ của bảng lessons (xác nhận: không có bảng courses riêng).
        ReviewableContentHandler lessonHandler = handlers.get(ContentType.LESSON);
        if (lessonHandler != null) {
            handlers.putIfAbsent(ContentType.COURSE, lessonHandler);
        }
    }

    /** Lấy handler cho loại nội dung; loại không hỗ trợ → 400 VALIDATION_FAILED. */
    public ReviewableContentHandler resolve(ContentType type) {
        ReviewableContentHandler handler = handlers.get(type);
        if (handler == null) {
            throw new BusinessException(
                    400, "VALIDATION_FAILED", "Loại nội dung không được hỗ trợ: " + type.getValue());
        }
        return handler;
    }

    /** Tập handler duy nhất (đã loại bí danh course↔lesson) để duyệt toàn bộ hàng đợi. */
    public Collection<ReviewableContentHandler> distinctHandlers() {
        return handlers.values().stream().distinct().toList();
    }
}
