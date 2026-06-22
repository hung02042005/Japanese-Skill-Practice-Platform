package com.jlpt.feature.student.flashcard;

import com.jlpt.feature.student.flashcard.dto.FlashcardRequest;
import com.jlpt.feature.student.flashcard.dto.FlashcardResponse;
import com.jlpt.shared.common.ApiResponse;
import com.jlpt.shared.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/flashcards")
@RequiredArgsConstructor
@PreAuthorize("hasRole('STUDENT')")
public class StudentFlashcardController {

    private final StudentFlashcardService flashcardService;

    @PostMapping
    public ResponseEntity<ApiResponse<FlashcardResponse>> addToFlashcard(
            @RequestBody FlashcardRequest request,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        FlashcardResponse response = flashcardService.addToFlashcard(request, userDetails.getStudentUser().getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }
}
