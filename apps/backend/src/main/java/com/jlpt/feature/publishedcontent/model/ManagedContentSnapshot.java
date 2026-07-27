/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.publishedcontent.model;

import com.jlpt.feature.contentreview.model.ContentType;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

/**
 * UC-34 — Snapshot phẳng tách rời Entity JPA của một mục nội dung (ADR-005).
 *
 * <p>Dùng nội bộ giữa handler ↔ service để không rò rỉ Entity ra ngoài API (FR-34-09).
 */
@Getter
@Builder
public class ManagedContentSnapshot {
    private final Long contentId;
    private final ContentType contentType;
    private final String titleOrText;
    private final String jlptLevel;
    private final String status;
    private final LocalDateTime publishedAt;
}
