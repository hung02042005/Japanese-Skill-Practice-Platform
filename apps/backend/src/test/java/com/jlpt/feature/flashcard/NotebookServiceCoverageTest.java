/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.flashcard;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.jlpt.feature.flashcard.dto.FlashcardResponse;
import com.jlpt.feature.flashcard.dto.ReviewDeckAddRequest;
import com.jlpt.feature.flashcard.dto.ReviewDeckAddResponse;
import com.jlpt.feature.flashcard.repository.FlashcardDeckRepository;
import com.jlpt.feature.flashcard.repository.FlashcardRepository;
import com.jlpt.feature.flashcard.service.FlashcardDeckSupport;
import com.jlpt.feature.flashcard.service.FlashcardResolver;
import com.jlpt.feature.flashcard.service.NotebookService;
import com.jlpt.feature.learning.Vocabulary;
import com.jlpt.feature.learning.VocabularyRepository;
import com.jlpt.feature.student.StudentUser;
import com.jlpt.feature.student.StudentUserRepository;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

/**
 * Sổ tay Flashcard — phủ NHÁNH của getCards (4 kiểu sort × có/không deckId × dueOnly × tìm kiếm
 * in-memory) và addWrongWordsToReviewDeck (thẻ đã có trong sổ / ở sổ khác / chưa có / từ không tồn
 * tại), theo SPEC-notebook §3.5 và FR-FC-31/43/44.
 */
@ExtendWith(MockitoExtension.class)
class NotebookServiceCoverageTest {

    private static final Long STUDENT_ID = 100L;
    private static final Long DECK_ID = 10L;

    @Mock
    private FlashcardRepository flashcardRepository;

    @Mock
    private FlashcardDeckRepository flashcardDeckRepository;

    @Mock
    private VocabularyRepository vocabularyRepository;

    @Mock
    private StudentUserRepository studentUserRepository;

    @Mock
    private FlashcardResolver resolver;

    @Mock
    private FlashcardDeckSupport deckSupport;

    @InjectMocks
    private NotebookService service;

    private StudentUser student;

    @BeforeEach
    void setUp() {
        student = StudentUser.builder().id(STUDENT_ID).build();
    }

    private Flashcard card(long id) {
        return Flashcard.builder().id(id).build();
    }

    private FlashcardResponse response(long id, String frontText, String level, LocalDate nextReview) {
        return new FlashcardResponse(
                id,
                DECK_ID,
                "VOCABULARY",
                id,
                frontText,
                "nghĩa",
                "ふりがな",
                null,
                level,
                false,
                nextReview,
                1,
                1,
                "EASY",
                "manual",
                true);
    }

    private void stubEmptyMaps() {
        when(resolver.loadContentMaps(anyList()))
                .thenReturn(new FlashcardResolver.ContentMaps(Map.of(), Map.of(), Map.of()));
    }

    // ── getDecks ─────────────────────────────────────────────────────────────

    @Test
    void getDecks_nullTotalCards_defaultsToZero() {
        ArrayList<Object[]> rows = new ArrayList<>();
        rows.add(new Object[] {1L, "Sổ 1", null, null});
        when(flashcardDeckRepository.findDeckSummaries(STUDENT_ID)).thenReturn(rows);

        var result = service.getDecks(STUDENT_ID);

        assertEquals(0, result.get(0).totalCards());
        assertFalse(result.get(0).isReviewDeck());
    }

    @Test
    void getDecks_reviewDeckFlagIsMapped() {
        ArrayList<Object[]> rows = new ArrayList<>();
        rows.add(new Object[] {2L, "Từ cần ôn lại", 7, true});
        when(flashcardDeckRepository.findDeckSummaries(STUDENT_ID)).thenReturn(rows);

        assertTrue(service.getDecks(STUDENT_ID).get(0).isReviewDeck());
    }

    // ── getCards: nhánh sort theo deck ───────────────────────────────────────

    @Test
    void getCards_deckWithRecentSort_usesRecentQuery() {
        when(flashcardRepository.findByDeckOrderByRecent(
                        eq(STUDENT_ID), eq(DECK_ID), eq(false), any(LocalDate.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(card(1))));
        stubEmptyMaps();
        when(resolver.toFlashcardResponse(any(), any())).thenReturn(response(1, "a", "N5", LocalDate.now()));

        assertEquals(
                1,
                service.getCards(STUDENT_ID, DECK_ID, false, null, "recent", PageRequest.of(0, 10))
                        .getTotalElements());
    }

    @Test
    void getCards_deckWithAlphaSort_usesWordQuery() {
        when(flashcardRepository.findByDeckOrderByWord(
                        eq(STUDENT_ID), eq(DECK_ID), any(), eq(false), any(LocalDate.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(card(1))));
        stubEmptyMaps();
        when(resolver.toFlashcardResponse(any(), any())).thenReturn(response(1, "a", "N5", LocalDate.now()));

        assertEquals(
                1,
                service.getCards(STUDENT_ID, DECK_ID, false, null, "alpha", PageRequest.of(0, 10))
                        .getTotalElements());
    }

    @Test
    void getCards_deckWithLevelSort_usesLevelQuery() {
        when(flashcardRepository.findByDeckOrderByLevel(
                        eq(STUDENT_ID), eq(DECK_ID), any(), eq(true), any(LocalDate.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(card(1))));
        stubEmptyMaps();
        when(resolver.toFlashcardResponse(any(), any())).thenReturn(response(1, "a", "N5", LocalDate.now()));

        assertEquals(
                1,
                service.getCards(STUDENT_ID, DECK_ID, true, null, "level", PageRequest.of(0, 10))
                        .getTotalElements());
    }

    /** sort lạ hoặc null → chuẩn hoá về "due", đi nhánh findDueByDeck/findAllByDeck. */
    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"due", "khong-ton-tai", "  RECENT  "})
    void getCards_sortIsNormalized(String sort) {
        boolean recent = sort != null && "recent".equals(sort.trim().toLowerCase());
        if (recent) {
            when(flashcardRepository.findByDeckOrderByRecent(
                            eq(STUDENT_ID), eq(DECK_ID), eq(false), any(LocalDate.class), any(Pageable.class)))
                    .thenReturn(new PageImpl<>(List.of(card(1))));
        } else {
            when(flashcardRepository.findAllByDeck(eq(STUDENT_ID), eq(DECK_ID), any(Pageable.class)))
                    .thenReturn(new PageImpl<>(List.of(card(1))));
        }
        stubEmptyMaps();
        when(resolver.toFlashcardResponse(any(), any())).thenReturn(response(1, "a", "N5", LocalDate.now()));

        assertEquals(
                1,
                service.getCards(STUDENT_ID, DECK_ID, false, null, sort, PageRequest.of(0, 10))
                        .getTotalElements());
    }

    @Test
    void getCards_deckDueOnlyWithDueSort_usesFindDueByDeck() {
        when(flashcardRepository.findDueByDeck(eq(STUDENT_ID), eq(DECK_ID), any(LocalDate.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(card(1))));
        stubEmptyMaps();
        when(resolver.toFlashcardResponse(any(), any())).thenReturn(response(1, "a", "N5", LocalDate.now()));

        assertEquals(
                1,
                service.getCards(STUDENT_ID, DECK_ID, true, null, "due", PageRequest.of(0, 10))
                        .getTotalElements());
    }

    @Test
    void getCards_noDeckDueOnly_usesFindAllDue() {
        when(flashcardRepository.findAllDue(eq(STUDENT_ID), any(LocalDate.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(card(1))));
        stubEmptyMaps();
        when(resolver.toFlashcardResponse(any(), any())).thenReturn(response(1, "a", "N5", LocalDate.now()));

        assertEquals(
                1,
                service.getCards(STUDENT_ID, null, true, null, null, PageRequest.of(0, 10))
                        .getTotalElements());
    }

    @Test
    void getCards_noDeckNoFilter_usesFindAllByStudent() {
        when(flashcardRepository.findAllByStudent(eq(STUDENT_ID), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(card(1))));
        stubEmptyMaps();
        when(resolver.toFlashcardResponse(any(), any())).thenReturn(response(1, "a", "N5", LocalDate.now()));

        assertEquals(
                1,
                service.getCards(STUDENT_ID, null, false, null, null, PageRequest.of(0, 10))
                        .getTotalElements());
    }

    /** FR-FC-34: thẻ có nguồn đã xoá/không published (frontText null) phải bị ẩn. */
    @Test
    void getCards_hidesCardsWhoseSourceContentIsGone() {
        when(flashcardRepository.findAllByStudent(eq(STUDENT_ID), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(card(1), card(2))));
        stubEmptyMaps();
        when(resolver.toFlashcardResponse(any(), any()))
                .thenReturn(response(1, null, "N5", LocalDate.now()), response(2, "còn sống", "N5", LocalDate.now()));

        Page<FlashcardResponse> result = service.getCards(STUDENT_ID, null, false, null, null, PageRequest.of(0, 10));

        assertEquals(1, result.getContent().size());
        assertEquals("còn sống", result.getContent().get(0).frontText());
    }

    // ── getCards: nhánh tìm kiếm in-memory ───────────────────────────────────

    @Test
    void getCards_searchWithDeck_scopesToDeckAndFiltersByFrontText() {
        when(flashcardRepository.findByStudentAndDeck(STUDENT_ID, DECK_ID)).thenReturn(List.of(card(1), card(2)));
        stubEmptyMaps();
        when(resolver.toFlashcardResponse(any(), any()))
                .thenReturn(response(1, "Nihon", "N5", LocalDate.now()), response(2, "Kankoku", "N5", LocalDate.now()));

        Page<FlashcardResponse> result =
                service.getCards(STUDENT_ID, DECK_ID, false, "  NIHON  ", null, PageRequest.of(0, 10));

        assertEquals(1, result.getTotalElements());
        assertEquals("Nihon", result.getContent().get(0).frontText());
    }

    /** Trước đây tìm kiếm bị bỏ qua âm thầm khi không có deckId — phải quét toàn bộ thẻ của học viên. */
    @Test
    void getCards_searchWithoutDeck_scansAllStudentCards() {
        when(flashcardRepository.findByStudent(STUDENT_ID)).thenReturn(List.of(card(1)));
        stubEmptyMaps();
        when(resolver.toFlashcardResponse(any(), any())).thenReturn(response(1, "Nihon", "N5", LocalDate.now()));

        assertEquals(
                1,
                service.getCards(STUDENT_ID, null, false, "nihon", null, PageRequest.of(0, 10))
                        .getTotalElements());
        verify(flashcardRepository, never()).findByStudentAndDeck(any(), any());
    }

    @Test
    void getCards_searchSkipsCardsWithNullFrontText() {
        when(flashcardRepository.findByStudent(STUDENT_ID)).thenReturn(List.of(card(1)));
        stubEmptyMaps();
        when(resolver.toFlashcardResponse(any(), any())).thenReturn(response(1, null, "N5", LocalDate.now()));

        assertEquals(
                0,
                service.getCards(STUDENT_ID, null, false, "nihon", null, PageRequest.of(0, 10))
                        .getTotalElements());
    }

    @Test
    void getCards_searchWithPagingBeyondResultSize_returnsEmptySlice() {
        when(flashcardRepository.findByStudent(STUDENT_ID)).thenReturn(List.of(card(1)));
        stubEmptyMaps();
        when(resolver.toFlashcardResponse(any(), any())).thenReturn(response(1, "Nihon", "N5", LocalDate.now()));

        Page<FlashcardResponse> result =
                service.getCards(STUDENT_ID, null, false, "nihon", null, PageRequest.of(5, 10));

        assertTrue(result.getContent().isEmpty());
        assertEquals(1, result.getTotalElements());
    }

    @Test
    void getCards_searchBlankQuery_fallsBackToNormalQuery() {
        when(flashcardRepository.findAllByStudent(eq(STUDENT_ID), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(card(1))));
        stubEmptyMaps();
        when(resolver.toFlashcardResponse(any(), any())).thenReturn(response(1, "a", "N5", LocalDate.now()));

        service.getCards(STUDENT_ID, null, false, "   ", null, PageRequest.of(0, 10));

        verify(flashcardRepository, never()).findByStudent(any());
    }

    @Test
    void getCards_searchSortedAlphabetically() {
        when(flashcardRepository.findByStudent(STUDENT_ID)).thenReturn(List.of(card(1), card(2)));
        stubEmptyMaps();
        when(resolver.toFlashcardResponse(any(), any()))
                .thenReturn(response(1, "zebra", "N5", LocalDate.now()), response(2, "alpha", "N5", LocalDate.now()));

        Page<FlashcardResponse> result = service.getCards(STUDENT_ID, null, false, "a", "alpha", PageRequest.of(0, 10));

        assertEquals("alpha", result.getContent().get(0).frontText());
        assertEquals("zebra", result.getContent().get(1).frontText());
    }

    @Test
    void getCards_searchSortedByRecencyIsIdDescending() {
        when(flashcardRepository.findByStudent(STUDENT_ID)).thenReturn(List.of(card(1), card(2)));
        stubEmptyMaps();
        when(resolver.toFlashcardResponse(any(), any()))
                .thenReturn(response(1, "aaa", "N5", LocalDate.now()), response(2, "aab", "N5", LocalDate.now()));

        Page<FlashcardResponse> result =
                service.getCards(STUDENT_ID, null, false, "aa", "recent", PageRequest.of(0, 10));

        assertEquals(2L, result.getContent().get(0).flashcardId());
    }

    @Test
    void getCards_searchSortedByLevelThenFrontText() {
        when(flashcardRepository.findByStudent(STUDENT_ID)).thenReturn(List.of(card(1), card(2), card(3)));
        stubEmptyMaps();
        when(resolver.toFlashcardResponse(any(), any()))
                .thenReturn(
                        response(1, "ab", "N5", LocalDate.now()),
                        response(2, "aa", "N5", LocalDate.now()),
                        response(3, "ac", null, LocalDate.now()));

        Page<FlashcardResponse> result = service.getCards(STUDENT_ID, null, false, "a", "level", PageRequest.of(0, 10));

        assertEquals("aa", result.getContent().get(0).frontText());
        assertEquals("ab", result.getContent().get(1).frontText());
        assertEquals("ac", result.getContent().get(2).frontText(), "level null xếp cuối");
    }

    @Test
    void getCards_searchSortedByDueDateWithNullsLast() {
        when(flashcardRepository.findByStudent(STUDENT_ID)).thenReturn(List.of(card(1), card(2)));
        stubEmptyMaps();
        when(resolver.toFlashcardResponse(any(), any()))
                .thenReturn(response(1, "aa", "N5", null), response(2, "ab", "N5", LocalDate.now()));

        Page<FlashcardResponse> result = service.getCards(STUDENT_ID, null, false, "a", "due", PageRequest.of(0, 10));

        assertEquals("ab", result.getContent().get(0).frontText());
        assertNull(result.getContent().get(1).nextReviewDate());
    }

    // ── bulkDelete ───────────────────────────────────────────────────────────

    @Test
    void bulkDelete_nullIds_returnsZeroWithoutQuery() {
        assertEquals(0, service.bulkDelete(STUDENT_ID, null));
        verifyNoInteractions(flashcardRepository);
    }

    @Test
    void bulkDelete_emptyIds_returnsZeroWithoutQuery() {
        assertEquals(0, service.bulkDelete(STUDENT_ID, List.of()));
        verifyNoInteractions(flashcardRepository);
    }

    @Test
    void bulkDelete_withIds_delegatesSoftDelete() {
        when(flashcardRepository.softDeleteByIds(List.of(1L, 2L), STUDENT_ID)).thenReturn(2);

        assertEquals(2, service.bulkDelete(STUDENT_ID, List.of(1L, 2L)));
    }

    // ── addWrongWordsToReviewDeck ────────────────────────────────────────────

    private FlashcardDeck reviewDeck() {
        return FlashcardDeck.builder()
                .id(99L)
                .student(student)
                .name(FlashcardConstants.REVIEW_DECK_NAME)
                .isReviewDeck(true)
                .build();
    }

    private ReviewDeckAddRequest request(String reason, Long... contentIds) {
        List<ReviewDeckAddRequest.Item> items = java.util.Arrays.stream(contentIds)
                .map(id -> new ReviewDeckAddRequest.Item("VOCABULARY", id))
                .toList();
        return new ReviewDeckAddRequest(items, reason);
    }

    @Test
    void addWrongWords_createsReviewDeckOnFirstUse() {
        when(studentUserRepository.getReferenceById(STUDENT_ID)).thenReturn(student);
        when(flashcardDeckRepository.findByStudentIdAndIsReviewDeckTrue(STUDENT_ID))
                .thenReturn(Optional.empty());
        when(flashcardDeckRepository.save(any())).thenReturn(reviewDeck());
        when(flashcardRepository.findByStudentAndContent(STUDENT_ID, Flashcard.ContentType.VOCABULARY, 1L))
                .thenReturn(Optional.empty());
        when(vocabularyRepository.findById(1L))
                .thenReturn(Optional.of(Vocabulary.builder().id(1L).word("日本").build()));

        ReviewDeckAddResponse response = service.addWrongWordsToReviewDeck(STUDENT_ID, request("wrong", 1L));

        assertEquals(1, response.addedCount());
        assertEquals(0, response.skippedCount());
        ArgumentCaptor<FlashcardDeck> deckCaptor = ArgumentCaptor.forClass(FlashcardDeck.class);
        verify(flashcardDeckRepository).save(deckCaptor.capture());
        assertTrue(deckCaptor.getValue().getIsReviewDeck());
    }

    @Test
    void addWrongWords_newCardCarriesReasonAndIsDueToday() {
        when(studentUserRepository.getReferenceById(STUDENT_ID)).thenReturn(student);
        when(flashcardDeckRepository.findByStudentIdAndIsReviewDeckTrue(STUDENT_ID))
                .thenReturn(Optional.of(reviewDeck()));
        when(flashcardRepository.findByStudentAndContent(STUDENT_ID, Flashcard.ContentType.VOCABULARY, 1L))
                .thenReturn(Optional.empty());
        when(vocabularyRepository.findById(1L))
                .thenReturn(Optional.of(Vocabulary.builder().id(1L).word("日本").build()));

        service.addWrongWordsToReviewDeck(STUDENT_ID, request("wrong", 1L));

        ArgumentCaptor<Flashcard> captor = ArgumentCaptor.forClass(Flashcard.class);
        verify(flashcardRepository).save(captor.capture());
        Flashcard saved = captor.getValue();
        assertEquals("wrong", saved.getAddedReason());
        assertEquals(LocalDate.now(), saved.getNextReviewDate());
        assertFalse(saved.getIsSystem());
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"   "})
    void addWrongWords_blankReason_defaultsToManual(String reason) {
        when(studentUserRepository.getReferenceById(STUDENT_ID)).thenReturn(student);
        when(flashcardDeckRepository.findByStudentIdAndIsReviewDeckTrue(STUDENT_ID))
                .thenReturn(Optional.of(reviewDeck()));
        when(flashcardRepository.findByStudentAndContent(STUDENT_ID, Flashcard.ContentType.VOCABULARY, 1L))
                .thenReturn(Optional.empty());
        when(vocabularyRepository.findById(1L))
                .thenReturn(Optional.of(Vocabulary.builder().id(1L).word("日本").build()));

        service.addWrongWordsToReviewDeck(STUDENT_ID, request(reason, 1L));

        ArgumentCaptor<Flashcard> captor = ArgumentCaptor.forClass(Flashcard.class);
        verify(flashcardRepository).save(captor.capture());
        assertEquals("manual", captor.getValue().getAddedReason());
    }

    /** FR-FC-31/44: thẻ đã ở sẵn trong sổ ôn lại → bỏ qua, không tạo trùng. */
    @Test
    void addWrongWords_cardAlreadyInReviewDeck_isSkipped() {
        FlashcardDeck deck = reviewDeck();
        Flashcard existing = Flashcard.builder().id(5L).deck(deck).build();
        when(studentUserRepository.getReferenceById(STUDENT_ID)).thenReturn(student);
        when(flashcardDeckRepository.findByStudentIdAndIsReviewDeckTrue(STUDENT_ID))
                .thenReturn(Optional.of(deck));
        when(flashcardRepository.findByStudentAndContent(STUDENT_ID, Flashcard.ContentType.VOCABULARY, 1L))
                .thenReturn(Optional.of(existing));

        ReviewDeckAddResponse response = service.addWrongWordsToReviewDeck(STUDENT_ID, request("wrong", 1L));

        assertEquals(0, response.addedCount());
        assertEquals(1, response.skippedCount());
        verify(flashcardRepository, never()).save(any());
    }

    /** Thẻ đang ở sổ khác → CHUYỂN sang sổ ôn lại, không được im lặng bỏ qua. */
    @Test
    void addWrongWords_cardInAnotherDeck_isMovedIntoReviewDeck() {
        FlashcardDeck otherDeck = FlashcardDeck.builder().id(1L).build();
        Flashcard existing = Flashcard.builder().id(5L).deck(otherDeck).build();
        when(studentUserRepository.getReferenceById(STUDENT_ID)).thenReturn(student);
        when(flashcardDeckRepository.findByStudentIdAndIsReviewDeckTrue(STUDENT_ID))
                .thenReturn(Optional.of(reviewDeck()));
        when(flashcardRepository.findByStudentAndContent(STUDENT_ID, Flashcard.ContentType.VOCABULARY, 1L))
                .thenReturn(Optional.of(existing));

        ReviewDeckAddResponse response = service.addWrongWordsToReviewDeck(STUDENT_ID, request("manual", 1L));

        assertEquals(1, response.addedCount());
        assertEquals(0, response.skippedCount());
        assertEquals(99L, existing.getDeck().getId());
        assertEquals("manual", existing.getAddedReason());
        verify(flashcardRepository).save(existing);
    }

    @Test
    void addWrongWords_cardWithoutDeck_isMovedIntoReviewDeck() {
        Flashcard existing = Flashcard.builder().id(5L).deck(null).build();
        when(studentUserRepository.getReferenceById(STUDENT_ID)).thenReturn(student);
        when(flashcardDeckRepository.findByStudentIdAndIsReviewDeckTrue(STUDENT_ID))
                .thenReturn(Optional.of(reviewDeck()));
        when(flashcardRepository.findByStudentAndContent(STUDENT_ID, Flashcard.ContentType.VOCABULARY, 1L))
                .thenReturn(Optional.of(existing));

        assertEquals(
                1,
                service.addWrongWordsToReviewDeck(STUDENT_ID, request("manual", 1L))
                        .addedCount());
    }

    @Test
    void addWrongWords_vocabularyNotFound_isSkipped() {
        when(studentUserRepository.getReferenceById(STUDENT_ID)).thenReturn(student);
        when(flashcardDeckRepository.findByStudentIdAndIsReviewDeckTrue(STUDENT_ID))
                .thenReturn(Optional.of(reviewDeck()));
        when(flashcardRepository.findByStudentAndContent(STUDENT_ID, Flashcard.ContentType.VOCABULARY, 404L))
                .thenReturn(Optional.empty());
        when(vocabularyRepository.findById(404L)).thenReturn(Optional.empty());

        ReviewDeckAddResponse response = service.addWrongWordsToReviewDeck(STUDENT_ID, request("wrong", 404L));

        assertEquals(0, response.addedCount());
        assertEquals(1, response.skippedCount());
        verify(flashcardRepository, never()).save(any());
    }

    @Test
    void addWrongWords_emptyItemList_returnsZeroCounts() {
        when(studentUserRepository.getReferenceById(STUDENT_ID)).thenReturn(student);
        when(flashcardDeckRepository.findByStudentIdAndIsReviewDeckTrue(STUDENT_ID))
                .thenReturn(Optional.of(reviewDeck()));

        ReviewDeckAddResponse response = service.addWrongWordsToReviewDeck(STUDENT_ID, request("wrong"));

        assertEquals(0, response.addedCount());
        assertEquals(0, response.skippedCount());
        assertEquals(99L, response.deckId());
    }
}
