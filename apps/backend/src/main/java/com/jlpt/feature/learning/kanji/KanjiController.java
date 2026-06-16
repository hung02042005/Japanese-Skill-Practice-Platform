/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.learning.kanji;

import com.jlpt.feature.learning.kanji.dto.response.KanjiDetailResponse;
import com.jlpt.feature.learning.kanji.dto.response.KanjiProgressSummaryResponse;
import com.jlpt.feature.learning.kanji.dto.response.KanjiSummaryResponse;
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

/** UC-07 — Học Kanji: danh sách theo level (phân trang) + chi tiết. */
@RestController
@RequestMapping("/api/kanji")
@RequiredArgsConstructor
@PreAuthorize("hasRole('STUDENT')")
public class KanjiController {

    private final KanjiService kanjiService;

    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> listKanji(
            @RequestParam(required = false) String level,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        int safePage = Math.max(page, 0);
        int safeSize = size > 50 ? 50 : (size < 1 ? 20 : size);
        Pageable pageable = PageRequest.of(safePage, safeSize);

        Page<KanjiSummaryResponse> result =
                kanjiService.listKanji(level, userDetails.getStudentUser().getId(), pageable);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("content", result.getContent());
        data.put("totalElements", result.getTotalElements());
        data.put("totalPages", result.getTotalPages());
        data.put("page", result.getNumber());
        data.put("size", result.getSize());

        return ResponseEntity.ok(ApiResponse.success(data));
    }

    @GetMapping("/progress-summary")
    public ResponseEntity<ApiResponse<KanjiProgressSummaryResponse>> getProgressSummary(
            @RequestParam String level, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        KanjiProgressSummaryResponse data =
                kanjiService.getProgressSummary(level, userDetails.getStudentUser().getId());
        return ResponseEntity.ok(ApiResponse.success(data));
    }

    @GetMapping("/{kanjiId}")
    public ResponseEntity<ApiResponse<KanjiDetailResponse>> getKanjiDetail(
            @PathVariable Long kanjiId, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        KanjiDetailResponse data = kanjiService.getKanjiDetail(kanjiId, userDetails.getStudentUser().getId());
        return ResponseEntity.ok(ApiResponse.success(data));
    }
}
