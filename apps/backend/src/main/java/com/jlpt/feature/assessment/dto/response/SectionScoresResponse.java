/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.assessment.dto.response;

import java.math.BigDecimal;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SectionScoresResponse {
    private BigDecimal languageKnowledge;
    private BigDecimal reading;
    private BigDecimal listening;
}
