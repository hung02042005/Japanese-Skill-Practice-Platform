/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.assessment.dto.response;

import lombok.Builder;
import lombok.Data;

/** KHÔNG chứa correctOption / correctAnswerText — xem BR-11-01. */
@Data
@Builder
public class QuestionResponse {
    private Long questionId;
    private String questionText;
    private String questionType;
    private String skill;
    private String optionA;
    private String optionB;
    private String optionC;
    private String optionD;
    private String audioUrl;
    private String imageUrl;
    private Integer displayOrder;
}
