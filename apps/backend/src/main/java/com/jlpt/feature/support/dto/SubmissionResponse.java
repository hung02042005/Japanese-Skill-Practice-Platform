/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.support.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SubmissionResponse {

    private Long submissionId;
    private String studentName;
    private String jlptLevel;
    private Integer durationSeconds;
    private LocalDateTime submittedAt;
    private String status;
    private BigDecimal aiOverallScore;
    private String recordingUrl;
    private BigDecimal aiPronunciationScore;
    private BigDecimal aiFluencyScore;
    private String aiHighlightedErrors;
    private String aiSuggestions;
    private LocalDateTime aiGradedAt;
    private BigDecimal manualScore;
    private String manualFeedback;
    private String gradedBy;
    private LocalDateTime gradedAt;
    private BigDecimal finalScore;
}
