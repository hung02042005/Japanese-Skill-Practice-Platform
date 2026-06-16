/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.staffcontent.learning.dto;

import lombok.Builder;
import lombok.Data;

/**
 * UC-27 — Response DTO for a submit-for-review transition.
 */
@Data
@Builder
public class SubmitReviewResponse {

    private Long contentId;
    private String contentType;
    private String status;
}
