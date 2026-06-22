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
public class GrammarDetailResponse {
    private Long grammarId;
    private String title;
    private String structure;
    private String formula;
    private String meaning;
    private String usageExplanation;
    private String jlptLevel;
    private String exampleSentenceJp;
    private String exampleSentenceVi;
    private String progressStatus;
}
