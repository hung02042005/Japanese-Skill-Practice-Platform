/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.flashcard;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.jlpt.feature.flashcard.dto.*;
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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

@ExtendWith(MockitoExtension.class)
class NotebookServiceTest {

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
        student = StudentUser.builder().id(100L).build();
    }

    @Test
    void getDecks_success() {
        ArrayList<Object[]> rows = new ArrayList<>();
        rows.add(new Object[] {1L, "Deck 1", 5, true});
        when(flashcardDeckRepository.findDeckSummaries(100L)).thenReturn(rows);

        List<DeckSummaryResponse> res = service.getDecks(100L);
        assertEquals(1, res.size());
        assertEquals("Deck 1", res.get(0).deckName());
    }

    @Test
    void getCards_withQueryFilter() {
        Flashcard card = Flashcard.builder().id(1L).build();
        when(flashcardRepository.findByStudentAndDeck(100L, 10L)).thenReturn(List.of(card));
        FlashcardResolver.ContentMaps maps = new FlashcardResolver.ContentMaps(Map.of(), Map.of(), Map.of());
        when(resolver.loadContentMaps(anyList())).thenReturn(maps);
        FlashcardResponse cardRes = new FlashcardResponse(
                1L,
                10L,
                "VOCABULARY",
                1L,
                "Nihon",
                "Japan",
                "にほん",
                null,
                "N5",
                false,
                LocalDate.now(),
                1,
                1,
                "EASY",
                "manual",
                true);
        when(resolver.toFlashcardResponse(any(), any())).thenReturn(cardRes);

        Page<FlashcardResponse> res = service.getCards(100L, 10L, false, "nihon", "recent", PageRequest.of(0, 10));
        assertEquals(1, res.getTotalElements());
    }

    @Test
    void deleteCard_success() {
        Flashcard card = Flashcard.builder().id(1L).build();
        when(deckSupport.ownCardOrThrow(1L, 100L)).thenReturn(card);

        service.deleteCard(100L, 1L);
        assertTrue(card.getIsDeleted());
        verify(flashcardRepository).save(card);
    }

    @Test
    void addWrongWordsToReviewDeck_success() {
        when(studentUserRepository.getReferenceById(100L)).thenReturn(student);
        FlashcardDeck deck = FlashcardDeck.builder().id(5L).name("Review").build();
        when(flashcardDeckRepository.findByStudentIdAndIsReviewDeckTrue(100L)).thenReturn(Optional.of(deck));

        Vocabulary vocab = Vocabulary.builder().id(10L).word("Word").build();
        when(vocabularyRepository.findById(10L)).thenReturn(Optional.of(vocab));
        when(flashcardRepository.findByStudentAndContent(100L, Flashcard.ContentType.VOCABULARY, 10L))
                .thenReturn(Optional.empty());

        ReviewDeckAddRequest req =
                new ReviewDeckAddRequest(List.of(new ReviewDeckAddRequest.Item("VOCABULARY", 10L)), "wrong");

        ReviewDeckAddResponse res = service.addWrongWordsToReviewDeck(100L, req);
        assertEquals(1, res.addedCount());
        assertEquals(0, res.skippedCount());
    }
}
