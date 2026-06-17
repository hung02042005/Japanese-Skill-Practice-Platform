/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.contentreview;

import com.jlpt.shared.exception.BusinessException;

/**
 * UC-33 — Loại nội dung chịu kiểm duyệt (discriminator cho {@code contentType}).
 *
 * <p>Lưu ý: hệ thống không có bảng riêng {@code courses}; "course" dùng chung bảng
 * {@code lessons} (xác nhận nghiệp vụ), nên {@link #COURSE} được resolver ánh xạ
 * tới handler của {@link #LESSON}.
 */
public enum ContentType {
    COURSE("course"),
    LESSON("lesson"),
    GRAMMAR("grammar"),
    VOCABULARY("vocabulary"),
    KANJI("kanji"),
    QUESTION("question"),
    ASSESSMENT("assessment");

    private final String value;

    ContentType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    /** Parse không phân biệt hoa thường; giá trị sai → 400 VALIDATION_FAILED. */
    public static ContentType fromValue(String raw) {
        if (raw != null) {
            for (ContentType t : values()) {
                if (t.value.equalsIgnoreCase(raw.trim()) || t.name().equalsIgnoreCase(raw.trim())) {
                    return t;
                }
            }
        }
        throw new BusinessException(400, "VALIDATION_FAILED", "Loại nội dung không hợp lệ: " + raw);
    }
}
