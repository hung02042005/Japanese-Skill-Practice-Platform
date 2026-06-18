/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.assessment.dto.response;

import java.math.BigDecimal;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ExamResultItem {
    private Long questionId;
    private String sectionName;
    private Boolean isCorrect;
    private String selectedOption;
    private String correctOption;
    private BigDecimal score;
    private String explanation;
}
