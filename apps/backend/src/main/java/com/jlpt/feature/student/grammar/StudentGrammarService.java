/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.student.grammar;

import com.jlpt.feature.student.grammar.dto.GrammarDetailResponse;
import com.jlpt.feature.student.grammar.dto.GrammarListResponse;

/**
 * UC-06 — Service interface for Student grammar operations.
 */
public interface StudentGrammarService {

    GrammarListResponse getGrammarList(String level, Long studentId, int page, int size);

    GrammarDetailResponse getGrammarDetail(Long grammarId, Long studentId);
}
