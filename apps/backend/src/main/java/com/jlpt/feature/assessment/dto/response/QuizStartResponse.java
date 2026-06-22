/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.assessment.dto.response;

import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class QuizStartResponse {
    private Long attemptId;
    private LocalDateTime startedAt;
    private Integer durationMin;
    private List<SectionResponse> sections;
}
