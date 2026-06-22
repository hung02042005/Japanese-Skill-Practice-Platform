package com.jlpt.feature.student.vocabulary;

import com.jlpt.feature.student.vocabulary.dto.VocabularyDetailResponse;
import com.jlpt.feature.student.vocabulary.dto.VocabularyListResponse;
import com.jlpt.shared.common.ApiResponse;
import com.jlpt.shared.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/vocabulary")
@RequiredArgsConstructor
@PreAuthorize("hasRole('STUDENT')")
public class StudentVocabularyController {

    private final StudentVocabularyService vocabularyService;

    @GetMapping
    public ResponseEntity<ApiResponse<VocabularyListResponse>> getVocabularyList(
            @RequestParam(required = false) String level,
            @RequestParam(required = false) String topic,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        int validPage = Math.max(0, page);
        int validSize = Math.max(1, Math.min(50, size));

        VocabularyListResponse response = vocabularyService.getVocabularyList(
                level, topic, userDetails.getStudentUser().getId(), validPage, validSize);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<VocabularyDetailResponse>> getVocabularyDetail(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        
        VocabularyDetailResponse response = vocabularyService.getVocabularyDetail(
                id, userDetails.getStudentUser().getId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
