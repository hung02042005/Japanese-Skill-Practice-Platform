/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.staffcontent.quiz.dto;

import java.util.List;
import lombok.Builder;
import lombok.Data;

/** UC-26 — Paginated quiz list payload `{ content, totalElements, totalPages }` (API §6.2). */
@Data
@Builder
public class QuizListResponse {

    private List<QuizSummaryResponse> content;
    private long totalElements;
    private int totalPages;
}
