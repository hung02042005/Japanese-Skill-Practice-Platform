/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.staffcontent.question;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * UC-24 — Response DTO for submit-review operation.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StaffQuestionSubmitReviewResponse {

    private Long contentId;
    private String contentType;
    private String status;
}
