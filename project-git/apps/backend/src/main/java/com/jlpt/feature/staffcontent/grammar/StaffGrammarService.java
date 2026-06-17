/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.staffcontent.grammar;

import com.jlpt.feature.staffcontent.grammar.dto.CreateGrammarRequest;
import com.jlpt.feature.staffcontent.grammar.dto.GrammarDetailResponse;
import com.jlpt.feature.staffcontent.grammar.dto.GrammarSubmitReviewResponse;
import com.jlpt.feature.staffcontent.grammar.dto.GrammarSummaryResponse;
import com.jlpt.feature.staffcontent.grammar.dto.UpdateGrammarRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * UC-25 — Interface for Staff grammar operations.
 */
public interface StaffGrammarService {

    /**
     * FR-01..08: Create a new grammar point.
     */
    GrammarDetailResponse createGrammar(CreateGrammarRequest request, String staffEmail);

    /**
     * FR-09/11: List grammar points created by the current staff.
     */
    Page<GrammarSummaryResponse> listGrammars(String jlptLevel, String status, Pageable pageable, String staffEmail);

    /**
     * FR-10/12: Get details of a grammar point.
     */
    GrammarDetailResponse getGrammar(Long grammarId, String staffEmail);

    /**
     * FR-13..17: Update an existing grammar point.
     */
    GrammarDetailResponse updateGrammar(Long grammarId, UpdateGrammarRequest request, String staffEmail);

    /**
     * FR-18..20: Submit a grammar point for review.
     */
    GrammarSubmitReviewResponse submitForReview(Long grammarId, String staffEmail);
}
