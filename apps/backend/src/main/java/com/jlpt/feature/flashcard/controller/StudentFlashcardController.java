/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.flashcard.controller;

import com.jlpt.feature.flashcard.dto.ReviewRequest;
import com.jlpt.feature.flashcard.dto.ReviewResultResponse;
import com.jlpt.feature.flashcard.dto.SessionResponse;
import com.jlpt.feature.flashcard.service.FlashcardSrsService;
import com.jlpt.shared.common.ApiResponse;
import com.jlpt.shared.security.UserDetailsImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * Phiên ôn Flashcard (SRS): dựng phiên học trộn NEW+REVIEW theo topic và chấm từng lượt ôn. CRUD Sổ
 * tay ("Từ cần ôn lại") tách sang {@link StudentNotebookController} (/api/notebook) để đường dẫn
 * phản ánh đúng domain.
 */
@RestController
@RequestMapping("/api/flashcards")
@RequiredArgsConstructor
@PreAuthorize("hasRole('STUDENT')")
public class StudentFlashcardController {

    private final FlashcardSrsService flashcardSrsService;

    /**
     * Phiên học trộn (§3.6/§3.7): ?topicId=…&newLimit=10.
     * POST (không phải GET) vì build phiên có side-effect: tạo deck/thẻ MỚI cho các từ được chọn.
     */
    @PostMapping("/session")
    public ResponseEntity<ApiResponse<SessionResponse>> getSession(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestParam(required = false) Long topicId,
            @RequestParam(required = false) Integer newLimit) {
        SessionResponse session =
                flashcardSrsService.getSession(userDetails.getStudentUser().getId(), topicId, newLimit);
        return ResponseEntity.ok(ApiResponse.success(session));
    }

    @PostMapping("/{id}/review")
    public ResponseEntity<ApiResponse<ReviewResultResponse>> submitReview(
            @PathVariable Long id,
            @Valid @RequestBody ReviewRequest request,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        ReviewResultResponse response = flashcardSrsService.submitReview(
                id, userDetails.getStudentUser().getId(), request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
