/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.contentreview.model;

import com.jlpt.shared.exception.BusinessException;

/** UC-33 — Hành động kiểm duyệt cho {@code POST /api/manager/reviews}. */
public enum ReviewAction {
    APPROVE,
    REJECT;

    /** Parse không phân biệt hoa thường; giá trị sai → 400 VALIDATION_FAILED. */
    public static ReviewAction fromValue(String raw) {
        if (raw != null) {
            for (ReviewAction reviewAction : values()) {
                if (reviewAction.name().equalsIgnoreCase(raw.trim())) {
                    return reviewAction;
                }
            }
        }
        throw new BusinessException(400, "VALIDATION_FAILED", "Hành động kiểm duyệt không hợp lệ: " + raw);
    }
}
