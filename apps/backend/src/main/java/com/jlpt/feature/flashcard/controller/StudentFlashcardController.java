/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.flashcard.controller;

import com.jlpt.feature.flashcard.dto.AddFlashcardRequest;
import com.jlpt.feature.flashcard.dto.DeckCreateRequest;
import com.jlpt.feature.flashcard.dto.DeckSummaryResponse;
import com.jlpt.feature.flashcard.dto.DeckUpdateRequest;
import com.jlpt.feature.flashcard.dto.FlashcardResponse;
import com.jlpt.feature.flashcard.dto.FlashcardRevealResponse;
import com.jlpt.feature.flashcard.dto.ReviewDeckAddRequest;
import com.jlpt.feature.flashcard.dto.ReviewDeckAddResponse;
import com.jlpt.feature.flashcard.dto.ReviewRequest;
import com.jlpt.feature.flashcard.dto.ReviewResultResponse;
import com.jlpt.feature.flashcard.dto.SessionResponse;
import com.jlpt.feature.flashcard.service.FlashcardSrsService;
import com.jlpt.shared.common.ApiResponse;
import com.jlpt.shared.security.UserDetailsImpl;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@PreAuthorize("hasRole('STUDENT')")
public class StudentFlashcardController {

    private final FlashcardSrsService flashcardSrsService;

    @GetMapping("/flashcard-decks")
    public ResponseEntity<ApiResponse<List<DeckSummaryResponse>>> getDecks(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        List<DeckSummaryResponse> decks =
                flashcardSrsService.getDecks(userDetails.getStudentUser().getId());
        return ResponseEntity.ok(ApiResponse.success(decks));
    }

    @PostMapping("/flashcard-decks")
    public ResponseEntity<ApiResponse<DeckSummaryResponse>> createDeck(
            @Valid @RequestBody DeckCreateRequest request, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        DeckSummaryResponse deck =
                flashcardSrsService.createDeck(userDetails.getStudentUser().getId(), request.deckName());
        return ResponseEntity.status(201).body(ApiResponse.created(deck));
    }

    @PatchMapping("/flashcard-decks/{deckId}")
    public ResponseEntity<ApiResponse<DeckSummaryResponse>> updateDeck(
            @PathVariable Long deckId,
            @Valid @RequestBody DeckUpdateRequest request,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        DeckSummaryResponse deck =
                flashcardSrsService.updateDeck(userDetails.getStudentUser().getId(), deckId, request);
        return ResponseEntity.ok(ApiResponse.success("Đã cập nhật sổ tay", deck));
    }

    @DeleteMapping("/flashcard-decks/{deckId}")
    public ResponseEntity<ApiResponse<Void>> deleteDeck(
            @PathVariable Long deckId, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        flashcardSrsService.deleteDeck(userDetails.getStudentUser().getId(), deckId);
        return ResponseEntity.ok(ApiResponse.success("Đã xóa sổ tay", null));
    }

    @GetMapping("/flashcards")
    public ResponseEntity<ApiResponse<Page<FlashcardResponse>>> getCards(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestParam(required = false) Long deckId,
            @RequestParam(defaultValue = "false") boolean dueOnly,
            @RequestParam(required = false) String q,
            // Không set sort ở đây: JPQL các query findAllByDeck/findAllDue… đã có ORDER BY
            // f.nextReviewDate ASC. Thêm sort của Pageable sẽ sinh 2 mệnh đề ORDER BY → SQL Server lỗi 500.
            @PageableDefault(size = 20) Pageable pageable) {
        Page<FlashcardResponse> cards =
                flashcardSrsService.getCards(userDetails.getStudentUser().getId(), deckId, dueOnly, q, pageable);
        return ResponseEntity.ok(ApiResponse.success(cards));
    }

    /**
     * Phiên học trộn (§3.6/§3.7): ?deckId=… HOẶC ?level=N5&topic=…&newLimit=10.
     * POST (không phải GET) vì build phiên có side-effect: tạo deck/thẻ MỚI cho các từ được chọn.
     */
    @PostMapping("/flashcards/session")
    public ResponseEntity<ApiResponse<SessionResponse>> getSession(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestParam(required = false) Long deckId,
            @RequestParam(required = false) String level,
            @RequestParam(required = false) String topic,
            @RequestParam(required = false) Integer newLimit) {
        SessionResponse session =
                flashcardSrsService.getSession(userDetails.getStudentUser().getId(), deckId, level, topic, newLimit);
        return ResponseEntity.ok(ApiResponse.success(session));
    }

    @GetMapping("/flashcards/{id}/reveal")
    public ResponseEntity<ApiResponse<FlashcardRevealResponse>> revealCard(
            @PathVariable Long id, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        FlashcardRevealResponse response =
                flashcardSrsService.revealCard(id, userDetails.getStudentUser().getId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/flashcards/{id}/review")
    public ResponseEntity<ApiResponse<ReviewResultResponse>> submitReview(
            @PathVariable Long id,
            @Valid @RequestBody ReviewRequest request,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        ReviewResultResponse response = flashcardSrsService.submitReview(
                id, userDetails.getStudentUser().getId(), request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /** Gỡ một thẻ khỏi sổ tay (soft-delete card — SPEC-notebook §5). */
    @DeleteMapping("/flashcards/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCard(
            @PathVariable Long id, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        flashcardSrsService.deleteCard(userDetails.getStudentUser().getId(), id);
        return ResponseEntity.ok(ApiResponse.success("Đã gỡ thẻ khỏi sổ tay", null));
    }

    @PostMapping("/flashcards")
    public ResponseEntity<ApiResponse<FlashcardResponse>> addCard(
            @Valid @RequestBody AddFlashcardRequest request, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        FlashcardResponse response =
                flashcardSrsService.addCard(userDetails.getStudentUser().getId(), request);
        return ResponseEntity.status(201).body(ApiResponse.created(response));
    }

    /** Xác nhận thêm từ sai vào sổ "Từ cần ôn lại" (§3.5). */
    @PostMapping("/flashcards/review-deck/add")
    public ResponseEntity<ApiResponse<ReviewDeckAddResponse>> addToReviewDeck(
            @Valid @RequestBody ReviewDeckAddRequest request, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        ReviewDeckAddResponse response = flashcardSrsService.addWrongWordsToReviewDeck(
                userDetails.getStudentUser().getId(), request);
        return ResponseEntity.ok(ApiResponse.success("Đã thêm vào Từ cần ôn lại", response));
    }
}
