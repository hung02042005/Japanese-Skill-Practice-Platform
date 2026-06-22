/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.student.grammar;

import com.jlpt.feature.student.grammar.dto.GrammarDetailResponse;
import com.jlpt.feature.student.grammar.dto.GrammarListResponse;
import com.jlpt.shared.common.ApiResponse;
import com.jlpt.shared.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * UC-06 — Controller for Student grammar learning features.
 */
@RestController
@RequestMapping("/api/grammar-points")
@RequiredArgsConstructor
@PreAuthorize("hasRole('STUDENT')")
public class StudentGrammarController {

    private final StudentGrammarService studentGrammarService;

    @GetMapping
    public ResponseEntity<ApiResponse<GrammarListResponse>> getGrammarList(
            @RequestParam String level,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        GrammarListResponse response = studentGrammarService.getGrammarList(
                level, userDetails.getStudentUser().getId(), page, size);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{grammarId}")
    public ResponseEntity<ApiResponse<GrammarDetailResponse>> getGrammarDetail(
            @PathVariable Long grammarId,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        GrammarDetailResponse response = studentGrammarService.getGrammarDetail(
                grammarId, userDetails.getStudentUser().getId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
