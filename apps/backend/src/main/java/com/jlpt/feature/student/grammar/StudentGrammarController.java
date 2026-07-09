/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.student.grammar;

import com.jlpt.feature.student.grammar.dto.GrammarDetailResponse;
import com.jlpt.feature.student.grammar.dto.GrammarListResponse;
import com.jlpt.shared.common.ApiResponse;
import com.jlpt.shared.security.UserDetailsImpl;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * UC-06 — Controller for Student grammar learning features.
 */
@RestController
@RequestMapping("/api/grammar-points")
@RequiredArgsConstructor
@PreAuthorize("hasRole('STUDENT')")
@Validated
public class StudentGrammarController {

    private final StudentGrammarService studentGrammarService;

    @GetMapping
    public ResponseEntity<ApiResponse<GrammarListResponse>> getGrammarList(
            @RequestParam String level,
            @RequestParam(defaultValue = "0") @Min(value = 0, message = "page phải >= 0") int page,
            @RequestParam(defaultValue = "20")
                    @Min(value = 1, message = "size phải >= 1")
                    @Max(value = 100, message = "size tối đa 100")
                    int size,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        GrammarListResponse response = studentGrammarService.getGrammarList(
                level, userDetails.getStudentUser().getId(), page, size);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{grammarId}")
    public ResponseEntity<ApiResponse<GrammarDetailResponse>> getGrammarDetail(
            @PathVariable Long grammarId, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        GrammarDetailResponse response = studentGrammarService.getGrammarDetail(
                grammarId, userDetails.getStudentUser().getId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
