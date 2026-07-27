/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.contentreview.model;

import java.time.LocalDateTime;
import java.util.Map;
import lombok.Builder;
import lombok.Getter;

/**
 * UC-33 — Ảnh chụp (snapshot) phẳng, tách rời Entity của một mục nội dung kiểm duyệt.
 *
 * <p>Dùng nội bộ giữa handler ↔ service để KHÔNG để Entity JPA rò rỉ ra ngoài (ADR-005).
 * {@code detail} chỉ được điền khi xem chi tiết (FR-33-06).
 */
@Getter
@Builder
public class ContentSnapshot {
    private final Long contentId;
    private final ContentType contentType;
    private final String titleOrText;
    private final String jlptLevel;
    private final String status;
    private final Long createdById;
    private final String createdByName;
    private final LocalDateTime submittedAt;
    private final Map<String, Object> detail;
}
