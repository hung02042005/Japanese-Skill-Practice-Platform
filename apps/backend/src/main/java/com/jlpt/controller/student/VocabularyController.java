/* (c) JLPT E-Learning Platform */
package com.jlpt.controller.student;

import com.jlpt.common.ApiResponse;
import com.jlpt.dto.response.VocabularyLevelResponse;
import com.jlpt.dto.response.VocabularyPageResponse;
import com.jlpt.dto.response.VocabularyPathResponse;
import com.jlpt.security.UserDetailsImpl;
import com.jlpt.service.VocabularyService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/vocabulary")
@RequiredArgsConstructor
@PreAuthorize("hasRole('STUDENT')")
public class VocabularyController {

    private final VocabularyService vocabularyService;

    /**
     * GET /api/vocabulary?level=N5&topic=...&search=...&page=0&size=20
     * Frontend: getVocabularyList()
     */
    @GetMapping
    public ResponseEntity<ApiResponse<VocabularyPageResponse>> getVocabularyList(
            @RequestParam(required = false) String level,
            @RequestParam(required = false) String topic,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        // Không truyền Sort: thứ tự đã do JPQL findPublished định nghĩa
        // (jlptLevel, topic, word) — tránh ORDER BY trùng lặp/ghi đè.
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.min(size, 100));

        VocabularyPageResponse result = vocabularyService.getVocabularyList(
                userDetails.getStudentUser().getId(), level, topic, search, pageable);

        return ResponseEntity.ok(ApiResponse.success(result));
    }

    /**
     * GET /api/vocabulary/levels
     * Điều hướng học từ vựng: liệt kê N5→N1 kèm số chủ đề & số từ (§3.7).
     */
    @GetMapping("/levels")
    public ResponseEntity<ApiResponse<List<VocabularyLevelResponse>>> getLevels() {
        return ResponseEntity.ok(ApiResponse.success(vocabularyService.getLevels()));
    }

    @GetMapping("/path")
    public ResponseEntity<ApiResponse<List<VocabularyPathResponse>>> getVocabularyPath(
            @RequestParam(required = false) String level, @AuthenticationPrincipal UserDetailsImpl userDetails) {

        List<VocabularyPathResponse> path =
                vocabularyService.getVocabularyPath(userDetails.getStudentUser().getId(), level);
        return ResponseEntity.ok(ApiResponse.success(path));
    }

    /**
     * GET /api/vocabulary/topics?level=N5
     * Frontend: getVocabTopics()
     */
    @GetMapping("/topics")
    public ResponseEntity<ApiResponse<List<String>>> getTopics(
            @RequestParam(required = false) String level, @AuthenticationPrincipal UserDetailsImpl userDetails) {

        List<String> topics = vocabularyService.getTopics(level);
        return ResponseEntity.ok(ApiResponse.success(topics));
    }

    /**
     * POST /api/vocabulary/{id}/complete
     * Frontend: markVocabComplete()
     */
    @PostMapping("/{id}/complete")
    public ResponseEntity<ApiResponse<Void>> markComplete(
            @PathVariable Long id, @AuthenticationPrincipal UserDetailsImpl userDetails) {

        vocabularyService.markComplete(userDetails.getStudentUser().getId(), id);
        return ResponseEntity.ok(ApiResponse.success("Đã đánh dấu hoàn thành.", null));
    }
}
