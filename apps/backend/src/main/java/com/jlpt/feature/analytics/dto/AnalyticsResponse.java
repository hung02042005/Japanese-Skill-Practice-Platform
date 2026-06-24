/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.analytics.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDate;
import java.util.Map;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AnalyticsResponse {

    private Long studentId;
    private Integer currentStreak;
    private Integer longestStreak;
    private LocalDate lastActivityDate;
    /** ACTIVE | AT_RISK | BROKEN */
    private String streakStatus;
    private String streakStatusMessage;
    /** key: lesson | kanji | vocabulary | grammar | kana */
    private Map<String, Integer> completions;
    /** key: contentType, value: 0-100 */
    private Map<String, Double> completionRates;
    /** key: grammar | vocabulary | reading | listening | speaking, value: 0-100 */
    private Map<String, Double> skillsRadar;
}
