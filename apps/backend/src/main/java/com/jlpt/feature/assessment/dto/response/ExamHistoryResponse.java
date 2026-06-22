/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.assessment.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ExamHistoryResponse {
    private Long attemptId;
    private String assessmentTitle;
    private String jlptLevel;
    private BigDecimal totalScore;
    private BigDecimal maxScore;
    private Boolean isPassed;
    private String status;
    private SectionScoresResponse sectionScores;
    private LocalDateTime submittedAt;
    private Integer durationSeconds;
}
