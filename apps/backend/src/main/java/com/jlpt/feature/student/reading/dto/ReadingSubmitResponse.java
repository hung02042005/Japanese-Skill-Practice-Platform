package com.jlpt.feature.student.reading.dto;

import java.math.BigDecimal;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReadingSubmitResponse {
    private Long attemptId;
    private BigDecimal score;
    private BigDecimal maxScore;
    private List<ReadingResultItemResponse> results;
}
