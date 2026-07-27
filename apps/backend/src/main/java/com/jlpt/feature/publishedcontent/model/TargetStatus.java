/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.publishedcontent.model;

import com.jlpt.shared.exception.BusinessException;

/**
 * UC-34 — Trạng thái đích hợp lệ cho thao tác ẩn nội dung (FR-34-12).
 *
 * <p>Ánh xạ nghiệp vụ → giá trị cột {@code status} thực tế và → {@code action} ghi audit:
 * <ul>
 *   <li>{@link #UNPUBLISHED} → {@code draft} (đưa về vùng làm việc của Staff) / {@code unpublish_content}</li>
 *   <li>{@link #ARCHIVED} → {@code archived} / {@code archive_content}</li>
 *   <li>{@link #DELETED} → {@code deleted} (trạng thái cuối) / {@code delete_content}</li>
 * </ul>
 */
public enum TargetStatus {
    UNPUBLISHED("unpublished", "draft", "unpublish_content"),
    ARCHIVED("archived", "archived", "archive_content"),
    DELETED("deleted", "deleted", "delete_content");

    private final String value;
    private final String resultingStatus;
    private final String auditAction;

    TargetStatus(String value, String resultingStatus, String auditAction) {
        this.value = value;
        this.resultingStatus = resultingStatus;
        this.auditAction = auditAction;
    }

    public String getValue() {
        return value;
    }

    /** Giá trị cột {@code status} sau khi áp dụng (trả về client trong response). */
    public String getResultingStatus() {
        return resultingStatus;
    }

    /** Giá trị {@code admin_audit_logs.action} tương ứng (FR-34-23). */
    public String getAuditAction() {
        return auditAction;
    }

    /** Parse không phân biệt hoa thường; giá trị ngoài miền cho phép → 400 VALIDATION_FAILED (FR-34-12). */
    public static TargetStatus fromValue(String rawValue) {
        if (rawValue != null) {
            for (TargetStatus targetStatus : values()) {
                if (targetStatus.value.equalsIgnoreCase(rawValue.trim())
                        || targetStatus.name().equalsIgnoreCase(rawValue.trim())) {
                    return targetStatus;
                }
            }
        }
        throw new BusinessException(
                400,
                "VALIDATION_FAILED",
                "Trạng thái đích không hợp lệ (chỉ unpublished/archived/deleted): " + rawValue);
    }
}
