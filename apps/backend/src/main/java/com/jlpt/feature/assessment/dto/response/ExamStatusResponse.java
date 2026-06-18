/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.assessment.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ExamStatusResponse {
    private Long attemptId;
    private String status;
    private long remainingSeconds;
    private boolean isExpired;
}
