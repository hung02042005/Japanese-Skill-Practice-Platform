/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.assessment.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AssessmentSummaryResponse {
    private Long assessmentId;
    private String title;
    private String assessmentType;
    private String jlptLevel;
    private Integer durationMin;
    private Integer passScore;
    private Integer totalScore;
    private long questionCount;
}
