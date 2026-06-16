/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.learning.flashcard;

import com.jlpt.feature.learning.flashcard.dto.request.AddFlashcardRequest;
import com.jlpt.feature.learning.flashcard.dto.response.FlashcardResponse;
import com.jlpt.shared.common.ApiResponse;
import com.jlpt.shared.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** UC-07 — "Add to Flashcard" (FR-LEARN-13). */
@RestController
@RequestMapping("/api/flashcards")
@RequiredArgsConstructor
@PreAuthorize("hasRole('STUDENT')")
public class FlashcardController {

    private final FlashcardService flashcardService;

    @PostMapping
    public ResponseEntity<ApiResponse<FlashcardResponse>> addToFlashcard(
            @RequestBody AddFlashcardRequest request, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        FlashcardResponse data = flashcardService.addToFlashcard(request, userDetails.getStudentUser().getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(data));
    }
}
