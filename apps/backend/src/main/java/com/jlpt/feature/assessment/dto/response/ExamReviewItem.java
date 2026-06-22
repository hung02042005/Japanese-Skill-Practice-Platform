/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.assessment.dto.response;

import java.math.BigDecimal;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ExamReviewItem {
    private Long questionId;
    private String questionText;
    private String optionA;
    private String optionB;
    private String optionC;
    private String optionD;
    private String selectedOption;
    private String correctOption;
    private Boolean isCorrect;
    private BigDecimal score;
    private String explanation;
}
