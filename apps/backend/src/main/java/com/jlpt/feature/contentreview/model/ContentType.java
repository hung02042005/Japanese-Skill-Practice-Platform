/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.contentreview.model;

import com.jlpt.shared.exception.BusinessException;

/**
 * UC-33 — Loại nội dung chịu kiểm duyệt (discriminator cho {@code contentType}).
 *
 */
public enum ContentType {
    LESSON("lesson"),
    SPEAKING("speaking"),
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
            for (ContentType contentType : values()) {
                if (contentType.value.equalsIgnoreCase(raw.trim())
                        || contentType.name().equalsIgnoreCase(raw.trim())) {
                    return contentType;
                }
            }
        }
        throw new BusinessException(400, "VALIDATION_FAILED", "Loại nội dung không hợp lệ: " + raw);
    }
}
