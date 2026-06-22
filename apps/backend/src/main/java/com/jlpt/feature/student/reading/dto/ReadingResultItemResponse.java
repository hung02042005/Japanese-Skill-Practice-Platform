package com.jlpt.feature.student.reading.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReadingResultItemResponse {
    private Long questionId;
    private Boolean isCorrect;
    private String correctOption;
    private String explanation;
}
