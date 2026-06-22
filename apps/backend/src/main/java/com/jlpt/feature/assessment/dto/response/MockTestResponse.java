/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.assessment.dto.response;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MockTestResponse {
    private Long id;
    private String title;
    private String jlptLevel;
    private Integer durationMin;
    private Integer passScore;
    private Integer totalScore;
    private String audioUrl;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
