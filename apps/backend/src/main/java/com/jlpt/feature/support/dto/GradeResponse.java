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
public class GradeResponse {

    private Long submissionId;
    private Long studentId;
    private String studentName;
    private String submissionType;
    private String status;
    private BigDecimal aiOverallScore;
    private BigDecimal manualScore;
    private BigDecimal finalScore;
    private String manualFeedback;
    private String gradedByStaffName;
    private LocalDateTime gradedAt;
    private LocalDateTime submittedAt;
}
