/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.assessment.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LegacyQuizOptionResponse {
    private int optionId;
    private String label;
    private String text;
}
