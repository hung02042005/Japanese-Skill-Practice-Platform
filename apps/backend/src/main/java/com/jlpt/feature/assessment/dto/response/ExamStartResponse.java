/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.assessment.dto.response;

import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ExamStartResponse {
    private Long attemptId;
    private LocalDateTime startedAt;
    private LocalDateTime expiresAt;
    private List<SectionResponse> sections;
    private String listeningAudioUrl;
}
