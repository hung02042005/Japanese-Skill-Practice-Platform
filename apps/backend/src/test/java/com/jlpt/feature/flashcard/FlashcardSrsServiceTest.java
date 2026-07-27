/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.flashcard;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.jlpt.feature.flashcard.dto.ReviewRequest;
import com.jlpt.feature.flashcard.dto.ReviewResultResponse;
import com.jlpt.feature.flashcard.dto.SessionResponse;
import com.jlpt.feature.flashcard.repository.FlashcardRepository;
import com.jlpt.feature.flashcard.service.FlashcardDeckSupport;
import com.jlpt.feature.flashcard.service.FlashcardResolver;
import com.jlpt.feature.flashcard.service.FlashcardSrsService;
import com.jlpt.feature.learning.Kanji;
import com.jlpt.feature.learning.Vocabulary;
import com.jlpt.feature.learning.VocabularyRepository;
import com.jlpt.feature.learning.VocabularyTopic;
import com.jlpt.feature.learning.VocabularyTopicRepository;
import com.jlpt.feature.student.StudentUser;
import com.jlpt.feature.student.StudentUserRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FlashcardSrsServiceTest {

    @Mock
    private FlashcardRepository flashcardRepository;

    @Mock
    private VocabularyRepository vocabularyRepository;

    @Mock
    private VocabularyTopicRepository vocabularyTopicRepository;

    @Mock
    private StudentUserRepository studentUserRepository;

    @Mock
    private FlashcardResolver resolver;

    @Mock
    private FlashcardDeckSupport deckSupport;

    @InjectMocks
    private FlashcardSrsService service;

    private Flashcard card;

    @BeforeEach
    void setUp() {
        card = Flashcard.builder()
                .id(1L)
                .contentType(Flashcard.ContentType.VOCABULARY)
                .contentId(10L)
                .easeFactor(BigDecimal.valueOf(2.50))
                .intervalDays(1)
                .repetitionCount(0)
                .build();
    }

    @Test
    void submitReview_vocabCorrect() {
        when(deckSupport.ownCardOrThrow(1L, 100L)).thenReturn(card);
        Vocabulary v = Vocabulary.builder().id(10L).meaning("Meaning").build();
        when(vocabularyRepository.findById(10L)).thenReturn(Optional.of(v));

        ReviewRequest req = new ReviewRequest(null, 10L, false, "session1");

        ReviewResultResponse res = service.submitReview(1L, 100L, req);
        assertTrue(res.correct());
        assertEquals("easy", res.rating());
    }

    @Test
    void submitReview_flipRating() {
        card.setContentType(Flashcard.ContentType.KANJI);
        when(deckSupport.ownCardOrThrow(1L, 100L)).thenReturn(card);

        ReviewRequest req = new ReviewRequest("EASY", null, false, "session1");

        ReviewResultResponse res = service.submitReview(1L, 100L, req);
        assertEquals("easy", res.rating());
        assertEquals(1, res.repetitionCount());
    }

    @Test
    void getSession_success() {
        StudentUser student = StudentUser.builder().id(100L).build();
        when(studentUserRepository.getReferenceById(100L)).thenReturn(student);
        VocabularyTopic topic = VocabularyTopic.builder()
                .id(5L)
                .status(Kanji.ContentStatus.PUBLISHED)
                .jlptLevel(StudentUser.JlptLevel.N5)
                .slug("topic-slug")
                .titleVi("Chủ đề")
                .build();
        when(vocabularyTopicRepository.findById(5L)).thenReturn(Optional.of(topic));

        FlashcardDeck deck = FlashcardDeck.builder().id(20L).build();
        when(deckSupport.getOrCreateDeck(any(), anyString())).thenReturn(deck);

        Vocabulary v1 =
                Vocabulary.builder().id(10L).word("Word1").meaning("Meaning1").build();
        Vocabulary v2 =
                Vocabulary.builder().id(11L).word("Word2").meaning("Meaning2").build();
        when(vocabularyRepository.findPublishedByTopicId(Kanji.ContentStatus.PUBLISHED, 5L))
                .thenReturn(List.of(v1, v2));

        when(flashcardRepository.findByStudentAndContentIds(anyLong(), any(), anyList()))
                .thenReturn(List.of(card));

        when(flashcardRepository.saveAll(anyList())).thenAnswer(i -> i.getArgument(0));

        SessionResponse res = service.getSession(100L, 5L, 10);
        assertNotNull(res.sessionId());
        assertEquals(2, res.wordCount());
    }
}
