/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.student.grammar.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GrammarSummaryResponse {
    private Long grammarId;
    private String title;
    private String structure;
    private String meaning;
    private String jlptLevel;
    private Boolean isCompleted;
}
