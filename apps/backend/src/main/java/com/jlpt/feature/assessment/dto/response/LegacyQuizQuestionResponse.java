/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.assessment.dto.response;

import java.util.List;
import lombok.Builder;
import lombok.Data;

/** KHÔNG chứa correctOptionId — xem BR-11-01. */
@Data
@Builder
public class LegacyQuizQuestionResponse {
    private Long questionId;
    private String content;
    private List<LegacyQuizOptionResponse> options;
}
