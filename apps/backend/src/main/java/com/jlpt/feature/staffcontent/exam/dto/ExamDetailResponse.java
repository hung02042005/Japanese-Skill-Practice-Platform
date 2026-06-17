/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.staffcontent.exam.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;
import lombok.Data;

/**
 * UC-28 — Full exam detail including sections grouped by sectionName with sectionScore,
 * plus score gate fields (FR-28-13/14).
 */
@Data
@Builder
public class ExamDetailResponse {

    private Long assessmentId;
    private String assessmentType;
    private String title;
    private String jlptLevel;
    private String description;
    private Integer durationMin;
    private Integer passScore;
    private Integer totalScore;
    private String status;
    private BigDecimal assignedScoreSum;
    private boolean scoreMatched;
    private List<ExamSection> sections;
    private List<AssignedQuestion> questions;
    private Long createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Data
    @Builder
    public static class ExamSection {
        private String sectionName;
        private BigDecimal sectionScore;
        private long questionCount;
    }

    @Data
    @Builder
    public static class AssignedQuestion {
        private Long assignmentId;
        private Long questionId;
        private Integer displayOrder;
        private BigDecimal score;
        private String sectionName;
        private String questionText;
    }
}
