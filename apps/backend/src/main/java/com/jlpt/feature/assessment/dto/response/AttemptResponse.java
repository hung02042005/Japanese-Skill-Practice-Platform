/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.assessment.dto.response;

import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AttemptResponse {
    private Long attemptId;
    private Long assessmentId;
    private String assessmentTitle;
    private LocalDateTime startedAt;
    private Integer durationMin;
    private String status;
    private List<SectionResponse> sections;
}
