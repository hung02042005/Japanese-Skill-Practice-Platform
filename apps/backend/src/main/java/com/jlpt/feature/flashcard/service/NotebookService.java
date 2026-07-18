/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.flashcard.service;

import com.jlpt.feature.flashcard.Flashcard;
import com.jlpt.feature.flashcard.FlashcardDeck;
import com.jlpt.feature.flashcard.dto.DeckSummaryResponse;
import com.jlpt.feature.flashcard.dto.FlashcardResponse;
import com.jlpt.feature.flashcard.dto.ReviewDeckAddRequest;
import com.jlpt.feature.flashcard.dto.ReviewDeckAddResponse;
import com.jlpt.feature.flashcard.repository.FlashcardDeckRepository;
import com.jlpt.feature.flashcard.repository.FlashcardRepository;
import com.jlpt.feature.flashcard.service.FlashcardResolver.ContentMaps;
import com.jlpt.feature.learning.Kanji;
import com.jlpt.feature.learning.Vocabulary;
import com.jlpt.feature.learning.VocabularyRepository;
import com.jlpt.feature.student.StudentUser;
import com.jlpt.feature.student.StudentUserRepository;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Sổ Tay / bộ sưu tập Flashcard: CRUD sổ (deck) và thẻ (list, thêm, gỡ, gỡ hàng loạt) + nạp sổ
 * "Từ cần ôn lại". Đây là phần backend phục vụ trang Sổ tay và thao tác lưu từ ở Từ điển — KHÔNG
 * chứa thuật toán ôn tập (SRS) vốn nằm ở {@link FlashcardSrsService}. Cả hai chia sẻ
 * {@link FlashcardResolver} (resolve mặt thẻ) và {@link FlashcardDeckSupport} (sở hữu + get-or-create).
 */
@Service
@RequiredArgsConstructor
@Transactional
public class NotebookService {

    private static final Kanji.ContentStatus PUBLISHED = Kanji.ContentStatus.PUBLISHED;

    private final FlashcardRepository flashcardRepository;
    private final FlashcardDeckRepository flashcardDeckRepository;
    private final VocabularyRepository vocabularyRepository;
    private final StudentUserRepository studentUserRepository;
    private final FlashcardResolver resolver;
    private final FlashcardDeckSupport deckSupport;

    // ── Deck CRUD ────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<DeckSummaryResponse> getDecks(Long studentId) {
        // Object[]: {deckId, name, totalCards, isReviewDeck}
        return flashcardDeckRepository.findDeckSummaries(studentId).stream()
                .map(r -> new DeckSummaryResponse(
                        ((Number) r[0]).longValue(),
                        (String) r[1],
                        r[2] != null ? ((Number) r[2]).intValue() : 0,
                        Boolean.TRUE.equals(r[3])))
                .toList();
    }

    // ── Card list / reveal (live-resolve §3.4) ───────────────────────────────

    @Transactional(readOnly = true)
    public Page<FlashcardResponse> getCards(
            Long studentId, Long deckId, boolean dueOnly, String q, String sort, Pageable pageable) {
        LocalDate today = LocalDate.now();
        String needle = q == null ? null : q.trim().toLowerCase();
        String sortKey = normalizeSort(sort);

        // Tìm kiếm server-side (SPEC-notebook): resolve live rồi lọc theo mặt trước — không bị giới
        // hạn bởi paging DB nên không bỏ sót thẻ ngoài trang đầu. Khi không có deckId thì tìm trên
        // toàn bộ thẻ của student (trước đây bỏ qua `q` âm thầm — anti-pattern silent ignore).
        if (needle != null && !needle.isEmpty()) {
            List<Flashcard> all = deckId != null
                    ? flashcardRepository.findByStudentAndDeck(studentId, deckId)
                    : flashcardRepository.findByStudent(studentId);
            ContentMaps maps = resolver.loadContentMaps(all);
            List<FlashcardResponse> matched = all.stream()
                    .filter(c -> !dueOnly || FlashcardResolver.isDue(c, today))
                    .map(c -> resolver.toFlashcardResponse(c, maps))
                    .filter(r ->
                            r.frontText() != null && r.frontText().toLowerCase().contains(needle))
                    .sorted(responseComparator(sortKey))
                    .toList();
            int from = (int) Math.min(pageable.getOffset(), matched.size());
            int to = Math.min(from + pageable.getPageSize(), matched.size());
            return new PageImpl<>(matched.subList(from, to), pageable, matched.size());
        }

        Page<Flashcard> cards;
        if (deckId != null && !"due".equals(sortKey)) {
            // Sort sổ tay (3B) — deck-scoped; alpha/level join Vocabulary (review deck = vocab-only).
            cards = switch (sortKey) {
                case "recent" -> flashcardRepository.findByDeckOrderByRecent(
                        studentId, deckId, dueOnly, today, pageable);
                case "alpha" -> flashcardRepository.findByDeckOrderByWord(
                        studentId, deckId, PUBLISHED, dueOnly, today, pageable);
                default -> flashcardRepository.findByDeckOrderByLevel(
                        studentId, deckId, PUBLISHED, dueOnly, today, pageable);};
        } else if (dueOnly) {
            cards = deckId != null
                    ? flashcardRepository.findDueByDeck(studentId, deckId, today, pageable)
                    : flashcardRepository.findAllDue(studentId, today, pageable);
        } else {
            cards = deckId != null
                    ? flashcardRepository.findAllByDeck(studentId, deckId, pageable)
                    : flashcardRepository.findAllByStudent(studentId, pageable);
        }
        ContentMaps maps = resolver.loadContentMaps(cards.getContent());
        List<FlashcardResponse> items = cards.getContent().stream()
                .map(c -> resolver.toFlashcardResponse(c, maps))
                // Ẩn thẻ tích hợp có nguồn đã xóa/không published (FR-FC-34); custom luôn có text.
                .filter(r -> r.frontText() != null)
                .toList();
        return new PageImpl<>(items, pageable, cards.getTotalElements());
    }

    /** Soft-delete (ADR-004) nhiều thẻ — quyền sở hữu ép trong query; trả số thẻ đã gỡ (3B). */
    public int bulkDelete(Long studentId, List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return 0;
        }
        return flashcardRepository.softDeleteByIds(ids, studentId);
    }

    /** Gỡ một thẻ khỏi sổ tay (soft-delete, ADR-004 — SPEC-notebook §5). */
    public void deleteCard(Long studentId, Long flashcardId) {
        Flashcard card = deckSupport.ownCardOrThrow(flashcardId, studentId);
        card.setIsDeleted(true);
        flashcardRepository.save(card);
    }

    /** Xác nhận thêm các từ sai vào sổ "Từ cần ôn lại" (§3.5, FR-FC-43/44). */
    public ReviewDeckAddResponse addWrongWordsToReviewDeck(Long studentId, ReviewDeckAddRequest request) {
        StudentUser student = studentUserRepository.getReferenceById(studentId);
        FlashcardDeck deck = deckSupport.getOrCreateReviewDeck(student);
        // Nguồn thẻ (SPEC-notebook §7): 'wrong' từ phiên ôn, 'manual' từ Từ điển. Mặc định 'manual'.
        String reason = (request.reason() != null && !request.reason().isBlank()) ? request.reason() : "manual";
        int added = 0;
        int skipped = 0;
        for (ReviewDeckAddRequest.Item item : request.items()) {
            Long contentId = item.contentId();
            // Mỗi nội dung chỉ 1 thẻ (FR-FC-31/44). Nếu thẻ đã tồn tại ở sổ khác (vd đã học
            // trong phiên level_topic) thì CHUYỂN sang sổ "Từ cần ôn lại"; chỉ bỏ qua khi đã ở
            // sẵn trong sổ này — nếu không, lưu thủ công từ Từ điển sẽ "im lặng" không vào sổ.
            Optional<Flashcard> existing =
                    flashcardRepository.findByStudentAndContent(studentId, Flashcard.ContentType.VOCABULARY, contentId);
            if (existing.isPresent()) {
                Flashcard card = existing.get();
                if (card.getDeck() != null && deck.getId().equals(card.getDeck().getId())) {
                    skipped++;
                } else {
                    card.setDeck(deck);
                    card.setAddedReason(reason);
                    flashcardRepository.save(card);
                    added++;
                }
                continue;
            }
            Vocabulary vocab = vocabularyRepository.findById(contentId).orElse(null);
            if (vocab == null) {
                skipped++;
                continue;
            }
            flashcardRepository.save(Flashcard.builder()
                    .student(student)
                    .deck(deck)
                    .contentType(Flashcard.ContentType.VOCABULARY)
                    .contentId(contentId)
                    .isSystem(false)
                    .addedReason(reason)
                    .nextReviewDate(LocalDate.now())
                    .build());
            added++;
        }
        return new ReviewDeckAddResponse(deck.getId(), deck.getName(), added, skipped);
    }

    // ── Helpers riêng của Sổ tay ──────────────────────────────────────────────

    /** Khoá sort hợp lệ; mặc định "due" (lịch ôn) cho client cũ/giá trị lạ. */
    private static String normalizeSort(String sort) {
        if (sort == null) return "due";
        return switch (sort.trim().toLowerCase()) {
            case "recent", "alpha", "level" -> sort.trim().toLowerCase();
            default -> "due";
        };
    }

    /** Comparator FlashcardResponse khớp thứ tự DB cho nhánh tìm kiếm in-memory (recency ≈ id DESC). */
    private static Comparator<FlashcardResponse> responseComparator(String sortKey) {
        return switch (sortKey) {
            case "recent" -> Comparator.comparing(FlashcardResponse::flashcardId)
                    .reversed();
            case "alpha" -> Comparator.comparing(
                    FlashcardResponse::frontText, Comparator.nullsLast(Comparator.naturalOrder()));
            case "level" -> Comparator.comparing(
                            FlashcardResponse::jlptLevel, Comparator.nullsLast(Comparator.naturalOrder()))
                    .thenComparing(FlashcardResponse::frontText, Comparator.nullsLast(Comparator.naturalOrder()));
            default -> Comparator.comparing(
                    FlashcardResponse::nextReviewDate, Comparator.nullsLast(Comparator.naturalOrder()));
        };
    }
}
