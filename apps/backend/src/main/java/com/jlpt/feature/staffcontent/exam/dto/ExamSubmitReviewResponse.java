/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.staffcontent.exam.dto;

import lombok.Builder;
import lombok.Data;

/** UC-28 — Result of submit-for-review. */
@Data
@Builder
public class ExamSubmitReviewResponse {

    private Long contentId;
    private String contentType;
    private String status;
}
