/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.learning.dto.request;

import lombok.Data;

/** UC-06 — POST /api/learning-progress body. Validate thủ công ở Service (LearningException). */
@Data
public class MarkLearningProgressRequest {

    private String contentType;
    private Long contentId;
    private String status;
    private Integer progressPercent;
}
