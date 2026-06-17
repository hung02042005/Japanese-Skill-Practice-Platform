/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.staffcontent.quiz.dto;

import lombok.Builder;
import lombok.Data;

/** UC-26 — Result of submit-for-review (API §6.6). */
@Data
@Builder
public class QuizSubmitReviewResponse {

    private Long contentId;
    private String contentType;
    private String status;
}
