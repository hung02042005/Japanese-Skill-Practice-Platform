package com.jlpt.feature.student.reading.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReadingLessonSummaryResponse {
    private Long id;
    private String title;
    private String jlptLevel;
    private Integer questionCount;
    private Boolean hasAttempted;
}
