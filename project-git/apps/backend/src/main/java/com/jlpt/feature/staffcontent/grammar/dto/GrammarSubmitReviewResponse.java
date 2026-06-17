/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.staffcontent.grammar.dto;

import lombok.Builder;
import lombok.Data;

/**
 * UC-25 — Response DTO for the submit-review operation (POST /api/staff/contents/submit-review).
 */
@Data
@Builder
public class GrammarSubmitReviewResponse {

    private Long contentId;
    private String contentType;
    private String status;
}
