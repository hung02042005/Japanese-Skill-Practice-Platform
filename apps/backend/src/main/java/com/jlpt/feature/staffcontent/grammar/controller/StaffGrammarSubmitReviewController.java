/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.staffcontent.grammar.controller;

import com.jlpt.feature.staffcontent.grammar.service.StaffGrammarService;

import com.jlpt.feature.staffcontent.grammar.dto.GrammarSubmitReviewResponse;
import com.jlpt.shared.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * UC-25 — Submit review endpoint for Grammar content.
 * POST /api/staff/grammar/{grammarId}/submit-review
 */
@RestController
@RequestMapping("/api/staff/grammar")
@RequiredArgsConstructor
@PreAuthorize("hasRole('STAFF')")
public class StaffGrammarSubmitReviewController {

    private final StaffGrammarService staffGrammarService;

    @PostMapping("/{grammarId}/submit-review")
    public ResponseEntity<ApiResponse<GrammarSubmitReviewResponse>> submitReview(
            @org.springframework.web.bind.annotation.PathVariable Long grammarId, Authentication authentication) {
        GrammarSubmitReviewResponse data = staffGrammarService.submitForReview(grammarId, authentication.getName());
        return ResponseEntity.ok(ApiResponse.success("Đã gửi duyệt ngữ pháp thành công", data));
    }
}
