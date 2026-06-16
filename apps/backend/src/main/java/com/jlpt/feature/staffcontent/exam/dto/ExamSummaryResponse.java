/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.staffcontent.exam.dto;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

/**
 * UC-28 — One row of the exam list (FR-28-10). Never exposes the JPA entity (NFR-28-05).
 */
@Data
@Builder
public class ExamSummaryResponse {

    private Long assessmentId;
    private String title;
    private String assessmentType;
    private String jlptLevel;
    private Integer durationMin;
    private Integer passScore;
    private Integer totalScore;
    private long questionCount;
    private String status;
    private String createdBy;
    private LocalDateTime updatedAt;
}
