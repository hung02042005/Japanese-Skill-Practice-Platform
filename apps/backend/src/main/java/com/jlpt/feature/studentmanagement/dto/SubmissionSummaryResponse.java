/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.studentmanagement.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SubmissionSummaryResponse {

    private Long submissionId;
    private String submissionType;
    private String status;
    private BigDecimal aiOverallScore;
    private BigDecimal manualScore;
    private BigDecimal finalScore;
    private String gradedByStaffName;
    private LocalDateTime submittedAt;
}
