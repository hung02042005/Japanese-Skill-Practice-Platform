/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.assessment.dto.response;

import lombok.Builder;
import lombok.Data;

/**
 * Adapter DTO khớp contract cũ của studentService.js (GET /api/quizzes).
 * Map 1-1 từ Assessment, KHÔNG phải entity mới.
 */
@Data
@Builder
public class LegacyQuizSummaryResponse {
    private Long quizId;
    private String jlptLevel;
    private String skill;
    private String title;
    private long questionCount;
    private long attemptCount;
    private int bestScore;
}
