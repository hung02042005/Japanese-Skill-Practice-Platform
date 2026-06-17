/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.staffcontent.quiz.dto;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

/** UC-26 — One row of the quiz list (FR-26-10). Never exposes the JPA entity (NFR-26-05). */
@Data
@Builder
public class QuizSummaryResponse {

    private Long assessmentId;
    private String title;
    private String assessmentType;
    private String jlptLevel;
    private Long lessonId;
    private String topic;
    private Integer durationMin;
    private Integer passScore;
    private Integer totalScore;
    private long questionCount;
    private String status;
    private LocalDateTime updatedAt;
}
