/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.contentreview.handler;

import com.jlpt.feature.staff.StaffUser;
import com.jlpt.shared.exception.BusinessException;
import java.util.LinkedHashMap;
import java.util.Map;

/** UC-33 — Tiện ích nhỏ dùng chung cho các handler (không trạng thái). */
final class HandlerSupport {

    private HandlerSupport() {}

    /** Đưa cặp key/value vào map nếu value khác null (tránh field null rò ra response). */
    static void put(Map<String, Object> map, String key, Object value) {
        if (value != null) {
            map.put(key, value);
        }
    }

    static Map<String, Object> newDetail() {
        return new LinkedHashMap<>();
    }

    static Long creatorId(StaffUser creator) {
        return creator != null ? creator.getId() : null;
    }

    static String creatorName(StaffUser creator) {
        return creator != null ? creator.getFullName() : null;
    }

    /** Ánh xạ targetStatus dạng chuỗi → tên enum (UPPER); sai miền giá trị → 400 VALIDATION_FAILED. */
    static <E extends Enum<E>> E toEnum(Class<E> enumType, String targetStatus) {
        try {
            return Enum.valueOf(enumType, targetStatus.trim().toUpperCase());
        } catch (IllegalArgumentException | NullPointerException invalidStatusError) {
            throw new BusinessException(400, "VALIDATION_FAILED", "Trạng thái đích không hợp lệ: " + targetStatus);
        }
    }
}
