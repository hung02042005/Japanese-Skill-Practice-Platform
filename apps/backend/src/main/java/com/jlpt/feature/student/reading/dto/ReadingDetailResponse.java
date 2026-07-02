/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.student.reading.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReadingDetailResponse {
    private Long id;
    private String title;
    private String jlptLevel;
    private String passageText;
    private List<ReadingQuestionResponse> questions;
}
