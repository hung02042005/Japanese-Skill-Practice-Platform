/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.learning.dto.response;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

/** UC-06 — Response DTO trả về sau khi upsert student_content_progress. */
@Data
@Builder
public class LearningProgressResponse {

    private Long progressId;
    private String contentType;
    private Long contentId;
    private String status;
    private Integer progressPercent;
    private LocalDateTime completedAt;
}
