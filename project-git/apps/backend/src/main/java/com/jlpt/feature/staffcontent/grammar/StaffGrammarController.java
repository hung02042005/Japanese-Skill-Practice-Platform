/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.staffcontent.grammar;

import com.jlpt.feature.staffcontent.grammar.dto.CreateGrammarRequest;
import com.jlpt.feature.staffcontent.grammar.dto.GrammarDetailResponse;
import com.jlpt.feature.staffcontent.grammar.dto.GrammarSummaryResponse;
import com.jlpt.feature.staffcontent.grammar.dto.UpdateGrammarRequest;
import com.jlpt.shared.common.ApiResponse;
import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * UC-25 — Staff endpoints for managing grammar content.
 */
@RestController
@RequestMapping("/api/staff/grammar")
@RequiredArgsConstructor
@PreAuthorize("hasRole('STAFF')")
public class StaffGrammarController {

    private final StaffGrammarService staffGrammarService;

    @PostMapping
    public ResponseEntity<ApiResponse<GrammarDetailResponse>> createGrammar(
            @Valid @RequestBody CreateGrammarRequest request, Authentication authentication) {
        GrammarDetailResponse data = staffGrammarService.createGrammar(request, authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.<GrammarDetailResponse>builder()
                        .status(201)
                        .message("Tạo ngữ pháp thành công")
                        .data(data)
                        .build());
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> listGrammars(
            @RequestParam(required = false) String jlptLevel,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication) {

        int effectiveSize = Math.min(size, 100);
        Pageable pageable = PageRequest.of(page, effectiveSize, Sort.by(Sort.Direction.DESC, "updatedAt"));
        Page<GrammarSummaryResponse> resultPage =
                staffGrammarService.listGrammars(jlptLevel, status, pageable, authentication.getName());

        Map<String, Object> data = new HashMap<>();
        data.put("content", resultPage.getContent());
        data.put("totalElements", resultPage.getTotalElements());
        data.put("totalPages", resultPage.getTotalPages());

        return ResponseEntity.ok(ApiResponse.success("OK", data));
    }

    @GetMapping("/{grammarId}")
    public ResponseEntity<ApiResponse<GrammarDetailResponse>> getGrammar(
            @PathVariable Long grammarId, Authentication authentication) {
        GrammarDetailResponse data = staffGrammarService.getGrammar(grammarId, authentication.getName());
        return ResponseEntity.ok(ApiResponse.success("OK", data));
    }

    @PutMapping("/{grammarId}")
    public ResponseEntity<ApiResponse<GrammarDetailResponse>> updateGrammar(
            @PathVariable Long grammarId,
            @Valid @RequestBody UpdateGrammarRequest request,
            Authentication authentication) {
        GrammarDetailResponse data = staffGrammarService.updateGrammar(grammarId, request, authentication.getName());
        return ResponseEntity.ok(ApiResponse.success("Cập nhật ngữ pháp thành công", data));
    }
}
