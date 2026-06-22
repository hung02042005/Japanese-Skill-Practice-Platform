/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.assessment.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ExamSubmitResponse {
    private Long attemptId;
    private BigDecimal totalScore;
    private BigDecimal maxScore;
    private Boolean isPassed;
    private Integer durationSeconds;
    private LocalDateTime submittedAt;
    private SectionScoresResponse sectionScores;
    private List<ExamResultItem> results;
}
