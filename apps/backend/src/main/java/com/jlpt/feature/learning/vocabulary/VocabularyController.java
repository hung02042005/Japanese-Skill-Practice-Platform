/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.learning.vocabulary;

import com.jlpt.feature.learning.vocabulary.dto.response.VocabularyDetailResponse;
import com.jlpt.feature.learning.vocabulary.dto.response.VocabularyProgressSummaryResponse;
import com.jlpt.feature.learning.vocabulary.dto.response.VocabularySummaryResponse;
import com.jlpt.shared.common.ApiResponse;
import com.jlpt.shared.security.UserDetailsImpl;
import java.util.LinkedHashMap;
import java.util.List;
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

/** UC-09 — Học Từ Vựng: danh sách theo level/topic (phân trang) + chi tiết. */
@RestController
@RequestMapping("/api/vocabulary")
@RequiredArgsConstructor
@PreAuthorize("hasRole('STUDENT')")
public class VocabularyController {

    private final VocabularyService vocabularyService;

    /** GET /api/vocabulary?level=N5&topic=Du lịch&page=0&size=20 — BR-09-01, BR-09-02. */
    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> listVocabulary(
            @RequestParam(required = false) String level,
            @RequestParam(required = false) String topic,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        int safePage = Math.max(page, 0);
        int safeSize = size > 50 ? 50 : (size < 1 ? 20 : size);
        Pageable pageable = PageRequest.of(safePage, safeSize);

        Page<VocabularySummaryResponse> result = vocabularyService.listVocabulary(
                level, topic, userDetails.getStudentUser().getId(), pageable);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("content", result.getContent());
        data.put("totalElements", result.getTotalElements());
        data.put("totalPages", result.getTotalPages());
        data.put("page", result.getNumber());
        data.put("size", result.getSize());

        return ResponseEntity.ok(ApiResponse.success(data));
    }

    /** GET /api/vocabulary/topics?level=N5 — danh sách topic khả dụng theo level. */
    @GetMapping("/topics")
    public ResponseEntity<ApiResponse<List<String>>> getAvailableTopics(
            @RequestParam String level) {
        List<String> topics = vocabularyService.getAvailableTopics(level);
        return ResponseEntity.ok(ApiResponse.success(topics));
    }

    /** GET /api/vocabulary/progress-summary?level=N5 — tổng kết tiến độ theo level. */
    @GetMapping("/progress-summary")
    public ResponseEntity<ApiResponse<VocabularyProgressSummaryResponse>> getProgressSummary(
            @RequestParam String level, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        VocabularyProgressSummaryResponse data =
                vocabularyService.getProgressSummary(level, userDetails.getStudentUser().getId());
        return ResponseEntity.ok(ApiResponse.success(data));
    }

    /** GET /api/vocabulary/{vocabularyId} — chi tiết từ vựng + BR-09-06 (cập nhật last_activity_date). */
    @GetMapping("/{vocabularyId}")
    public ResponseEntity<ApiResponse<VocabularyDetailResponse>> getVocabularyDetail(
            @PathVariable Long vocabularyId, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        VocabularyDetailResponse data =
                vocabularyService.getVocabularyDetail(vocabularyId, userDetails.getStudentUser().getId());
        return ResponseEntity.ok(ApiResponse.success(data));
    }
}
