/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.flashcard.controller;

import com.jlpt.feature.flashcard.dto.BulkDeleteRequest;
import com.jlpt.feature.flashcard.dto.DeckSummaryResponse;
import com.jlpt.feature.flashcard.dto.FlashcardResponse;
import com.jlpt.feature.flashcard.dto.ReviewDeckAddRequest;
import com.jlpt.feature.flashcard.dto.ReviewDeckAddResponse;
import com.jlpt.feature.flashcard.dto.ReviewRequest;
import com.jlpt.feature.flashcard.dto.ReviewResultResponse;
import com.jlpt.feature.flashcard.dto.SessionResponse;
import com.jlpt.feature.flashcard.service.FlashcardSrsService;
import com.jlpt.feature.flashcard.service.NotebookService;
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
    private final NotebookService notebookService;

    @GetMapping("/flashcard-decks")
    public ResponseEntity<ApiResponse<List<DeckSummaryResponse>>> getDecks(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        List<DeckSummaryResponse> decks =
                notebookService.getDecks(userDetails.getStudentUser().getId());
        return ResponseEntity.ok(ApiResponse.success(decks));
    }

    @GetMapping("/flashcards")
    public ResponseEntity<ApiResponse<Page<FlashcardResponse>>> getCards(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestParam(required = false) Long deckId,
            @RequestParam(defaultValue = "false") boolean dueOnly,
            @RequestParam(required = false) String q,
            // sortBy=due|recent|alpha|level (3B). Tên param KHÔNG được là "sort" — đó là tham số dành
            // riêng cho Pageable: Spring sẽ tự thêm "ORDER BY f.<sort>" vào JPQL vốn đã có ORDER BY →
            // 2 mệnh đề ORDER BY → 500 (đúng cảnh báo cũ). Sort thật xử lý ở Service.
            @RequestParam(name = "sortBy", required = false) String sortBy,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<FlashcardResponse> cards =
                notebookService.getCards(userDetails.getStudentUser().getId(), deckId, dueOnly, q, sortBy, pageable);
        return ResponseEntity.ok(ApiResponse.success(cards));
    }

    /**
     * Phiên học trộn (§3.6/§3.7): ?topicId=…&newLimit=10.
     * POST (không phải GET) vì build phiên có side-effect: tạo deck/thẻ MỚI cho các từ được chọn.
     */
    @PostMapping("/flashcards/session")
    public ResponseEntity<ApiResponse<SessionResponse>> getSession(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestParam(required = false) Long topicId,
            @RequestParam(required = false) Integer newLimit) {
        SessionResponse session =
                flashcardSrsService.getSession(userDetails.getStudentUser().getId(), topicId, newLimit);
        return ResponseEntity.ok(ApiResponse.success(session));
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
        notebookService.deleteCard(userDetails.getStudentUser().getId(), id);
        return ResponseEntity.ok(ApiResponse.success("Đã gỡ thẻ khỏi sổ tay", null));
    }

    /** Gỡ hàng loạt thẻ khỏi sổ tay (3B). Trả số thẻ đã gỡ. */
    @PostMapping("/flashcards/bulk-delete")
    public ResponseEntity<ApiResponse<Integer>> bulkDelete(
            @Valid @RequestBody BulkDeleteRequest request, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        int removed = notebookService.bulkDelete(userDetails.getStudentUser().getId(), request.ids());
        return ResponseEntity.ok(ApiResponse.success("Đã gỡ " + removed + " từ khỏi sổ tay", removed));
    }

    /** Xác nhận thêm từ sai vào sổ "Từ cần ôn lại" (§3.5). */
    @PostMapping("/flashcards/review-deck/add")
    public ResponseEntity<ApiResponse<ReviewDeckAddResponse>> addToReviewDeck(
            @Valid @RequestBody ReviewDeckAddRequest request, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        ReviewDeckAddResponse response = notebookService.addWrongWordsToReviewDeck(
                userDetails.getStudentUser().getId(), request);
        return ResponseEntity.ok(ApiResponse.success("Đã thêm vào Từ cần ôn lại", response));
    }
}
