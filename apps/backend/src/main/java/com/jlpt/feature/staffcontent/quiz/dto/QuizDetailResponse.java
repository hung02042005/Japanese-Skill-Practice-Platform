/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.staffcontent.quiz.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;
import lombok.Data;

/** UC-26 — Full quiz detail including ordered assignments + score gate fields (FR-26-13/14). */
@Data
@Builder
public class QuizDetailResponse {

    private Long assessmentId;
    private String assessmentType;
    private String title;
    private Long lessonId;
    private String topic;
    private String jlptLevel;
    private Integer durationMin;
    private Integer passScore;
    private Integer totalScore;
    private String status;
    private BigDecimal assignedScoreSum;
    private boolean scoreMatched;
    private List<AssignedQuestion> questions;
    private Long createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

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
