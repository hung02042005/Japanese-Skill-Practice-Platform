/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.learning.grammar;

import com.jlpt.feature.learning.grammar.dto.response.GrammarDetailResponse;
import com.jlpt.feature.learning.grammar.dto.response.GrammarSummaryResponse;
import com.jlpt.shared.common.ApiResponse;
import com.jlpt.shared.security.UserDetailsImpl;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** UC-06 — Học Ngữ Pháp: danh sách theo level (phân trang) + chi tiết. */
@RestController
@RequestMapping("/api/grammar-points")
@RequiredArgsConstructor
@PreAuthorize("hasRole('STUDENT')")
public class GrammarController {

    private final GrammarService grammarService;

    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> listGrammar(
            @RequestParam(required = false) String level,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        int safePage = Math.max(page, 0);
        int safeSize = size > 50 ? 50 : (size < 1 ? 20 : size);
        Pageable pageable = PageRequest.of(safePage, safeSize);

        Page<GrammarSummaryResponse> result =
                grammarService.listGrammar(level, userDetails.getStudentUser().getId(), pageable);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("content", result.getContent());
        data.put("totalElements", result.getTotalElements());
        data.put("totalPages", result.getTotalPages());
        data.put("page", result.getNumber());
        data.put("size", result.getSize());

        return ResponseEntity.ok(ApiResponse.success(data));
    }

    @GetMapping("/{grammarId}")
    public ResponseEntity<ApiResponse<GrammarDetailResponse>> getGrammarDetail(
            @PathVariable Long grammarId, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        GrammarDetailResponse data = grammarService.getGrammarDetail(
                grammarId, userDetails.getStudentUser().getId());
        return ResponseEntity.ok(ApiResponse.success(data));
    }
}
