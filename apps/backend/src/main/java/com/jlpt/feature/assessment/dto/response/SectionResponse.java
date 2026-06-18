/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.assessment.dto.response;

import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SectionResponse {
    private String sectionName;
    private List<QuestionResponse> questions;
}
