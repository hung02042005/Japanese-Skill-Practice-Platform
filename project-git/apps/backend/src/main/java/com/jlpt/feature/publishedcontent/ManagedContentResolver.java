/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.publishedcontent;

import com.jlpt.feature.contentreview.ContentType;
import com.jlpt.feature.publishedcontent.handler.ManagedContentHandler;
import com.jlpt.shared.exception.BusinessException;
import java.util.Collection;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

/**
 * UC-34 — Ánh xạ {@link ContentType} → {@link ManagedContentHandler}.
 *
 * <p>{@link ContentType#COURSE} dùng chung bảng {@code lessons} nên được alias tới handler
 * của {@link ContentType#LESSON} (xác nhận nghiệp vụ: không có bảng {@code courses} riêng).
 */
@Component
public class ManagedContentResolver {

    private final Map<ContentType, ManagedContentHandler> handlers = new EnumMap<>(ContentType.class);

    public ManagedContentResolver(List<ManagedContentHandler> handlerBeans) {
        for (ManagedContentHandler handler : handlerBeans) {
            handlers.put(handler.type(), handler);
        }
        ManagedContentHandler lessonHandler = handlers.get(ContentType.LESSON);
        if (lessonHandler != null) {
            handlers.putIfAbsent(ContentType.COURSE, lessonHandler);
        }
    }

    /** Lấy handler cho loại nội dung; loại không hỗ trợ → 400 VALIDATION_FAILED. */
    public ManagedContentHandler resolve(ContentType type) {
        ManagedContentHandler handler = handlers.get(type);
        if (handler == null) {
            throw new BusinessException(
                    400, "VALIDATION_FAILED", "Loại nội dung không được hỗ trợ: " + type.getValue());
        }
        return handler;
    }

    /** Tập handler duy nhất (đã loại bí danh course↔lesson) để duyệt toàn bộ danh sách. */
    public Collection<ManagedContentHandler> distinctHandlers() {
        return handlers.values().stream().distinct().toList();
    }
}
