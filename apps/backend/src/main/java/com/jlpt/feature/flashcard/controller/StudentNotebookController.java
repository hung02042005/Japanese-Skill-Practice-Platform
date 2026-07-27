/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.flashcard.controller;

import com.jlpt.feature.flashcard.dto.BulkDeleteRequest;
import com.jlpt.feature.flashcard.dto.DeckSummaryResponse;
import com.jlpt.feature.flashcard.dto.FlashcardResponse;
import com.jlpt.feature.flashcard.dto.ReviewDeckAddRequest;
import com.jlpt.feature.flashcard.dto.ReviewDeckAddResponse;
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

/**
 * Sổ Tay "Từ cần ôn lại": liệt kê deck/thẻ, gỡ thẻ, và thêm từ (từ sai cuối phiên ôn hoặc lưu thủ
 * công ở Từ điển). Tách khỏi {@link StudentFlashcardController} (phiên ôn SRS) để đường dẫn phản ánh
 * đúng domain — Sổ tay chỉ đọc/ghi danh sách thẻ, KHÔNG chạy phiên ôn.
 */
@RestController
@RequestMapping("/api/notebook")
@RequiredArgsConstructor
@PreAuthorize("hasRole('STUDENT')")
public class StudentNotebookController {

    private final NotebookService notebookService;

    @GetMapping("/decks")
    public ResponseEntity<ApiResponse<List<DeckSummaryResponse>>> getDecks(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        List<DeckSummaryResponse> decks =
                notebookService.getDecks(userDetails.getStudentUser().getId());
        return ResponseEntity.ok(ApiResponse.success(decks));
    }

    @GetMapping("/cards")
    public ResponseEntity<ApiResponse<Page<FlashcardResponse>>> getCards(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestParam(required = false) Long deckId,
            @RequestParam(defaultValue = "false") boolean dueOnly,
            @RequestParam(required = false) String q,
            // sortBy=due|recent|alpha|level (3B). Tên param KHÔNG được là "sort" — đó là tham số dành
            // riêng cho Pageable: Spring sẽ tự thêm "ORDER BY f.<sort>" vào JPQL vốn đã có ORDER BY →
            // 2 mệnh đề ORDER BY → 500. Sort thật xử lý ở Service.
            @RequestParam(name = "sortBy", required = false) String sortBy,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<FlashcardResponse> cards =
                notebookService.getCards(userDetails.getStudentUser().getId(), deckId, dueOnly, q, sortBy, pageable);
        return ResponseEntity.ok(ApiResponse.success(cards));
    }

    /** Gỡ một thẻ khỏi sổ tay (soft-delete card — SPEC-notebook §5). */
    @DeleteMapping("/cards/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCard(
            @PathVariable Long id, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        notebookService.deleteCard(userDetails.getStudentUser().getId(), id);
        return ResponseEntity.ok(ApiResponse.success("Đã gỡ thẻ khỏi sổ tay", null));
    }

    /** Gỡ hàng loạt thẻ khỏi sổ tay (3B). Trả số thẻ đã gỡ. */
    @PostMapping("/cards/bulk-delete")
    public ResponseEntity<ApiResponse<Integer>> bulkDelete(
            @Valid @RequestBody BulkDeleteRequest request, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        int removed = notebookService.bulkDelete(userDetails.getStudentUser().getId(), request.ids());
        return ResponseEntity.ok(ApiResponse.success("Đã gỡ " + removed + " từ khỏi sổ tay", removed));
    }

    /** Thêm từ vào sổ: từ sai cuối phiên ôn (reason='wrong') hoặc lưu thủ công ở Từ điển ('manual'). */
    @PostMapping("/words")
    public ResponseEntity<ApiResponse<ReviewDeckAddResponse>> addWords(
            @Valid @RequestBody ReviewDeckAddRequest request, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        ReviewDeckAddResponse response = notebookService.addWrongWordsToReviewDeck(
                userDetails.getStudentUser().getId(), request);
        return ResponseEntity.ok(ApiResponse.success("Đã thêm vào Từ cần ôn lại", response));
    }
}
