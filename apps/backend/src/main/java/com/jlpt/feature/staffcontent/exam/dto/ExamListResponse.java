/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.staffcontent.exam.dto;

import java.util.List;
import lombok.Builder;
import lombok.Data;

/**
 * UC-28 — Paginated exam list payload { content, totalElements, totalPages }.
 */
@Data
@Builder
public class ExamListResponse {

    private List<ExamSummaryResponse> content;
    private long totalElements;
    private int totalPages;
}
