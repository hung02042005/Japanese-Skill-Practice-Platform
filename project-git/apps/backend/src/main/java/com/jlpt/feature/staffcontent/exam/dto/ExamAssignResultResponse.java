/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.staffcontent.exam.dto;

import java.math.BigDecimal;
import java.util.List;
import lombok.Builder;
import lombok.Data;

/** UC-28 — Result of assign-questions. */
@Data
@Builder
public class ExamAssignResultResponse {

    private Long assessmentId;
    private int assignedCount;
    private BigDecimal assignedScoreSum;
    private Integer totalScore;
    private boolean scoreMatched;
    private List<SectionSummary> sectionSummaries;

    @Data
    @Builder
    public static class SectionSummary {
        private String sectionName;
        private BigDecimal sectionScore;
        private long questionCount;
    }
}
