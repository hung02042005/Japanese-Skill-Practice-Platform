/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.publishedcontent.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * UC-34 — Một tài nguyên đang tham chiếu tới nội dung (FR-34-16).
 *
 * <p>Constructor đủ tham số {@code (referenceType, referenceId, referenceTitle)} được dùng
 * trực tiếp trong JPQL constructor-expression của các reference query (xem repository).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReferenceItemResponse {
    private String referenceType;
    private Long referenceId;
    private String referenceTitle;
}
