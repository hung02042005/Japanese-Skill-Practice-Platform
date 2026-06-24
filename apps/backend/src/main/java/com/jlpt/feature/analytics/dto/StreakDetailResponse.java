/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.analytics.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDate;
import lombok.Builder;
import lombok.Data;

/** Response cho GET /api/analytics/streak — UC-19 FR-ANALYTICS-01/02. */
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class StreakDetailResponse {

    private Long studentId;
    private Integer currentStreak;
    private Integer longestStreak;
    private LocalDate lastActivityDate;

    /** ACTIVE | AT_RISK | BROKEN */
    private String streakStatus;

    /** Thông điệp khuyến khích phù hợp với streakStatus. */
    private String streakStatusMessage;
}
