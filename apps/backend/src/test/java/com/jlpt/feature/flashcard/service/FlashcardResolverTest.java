/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.flashcard.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.jlpt.feature.flashcard.Flashcard;
import com.jlpt.feature.flashcard.dto.FlashcardResponse;
import com.jlpt.feature.learning.*;
import com.jlpt.feature.student.StudentUser;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FlashcardResolverTest {

    @Mock
    private VocabularyRepository vocabularyRepository;

    @Mock
    private KanjiRepository kanjiRepository;

    @Mock
    private GrammarPointRepository grammarPointRepository;

    @InjectMocks
    private FlashcardResolver resolver;

    @Test
    void loadContentMaps_success() {
        Flashcard card1 = Flashcard.builder()
                .contentType(Flashcard.ContentType.VOCABULARY)
                .contentId(10L)
                .build();
        Flashcard card2 = Flashcard.builder()
                .contentType(Flashcard.ContentType.KANJI)
                .contentId(20L)
                .build();

        Vocabulary vocab = Vocabulary.builder().id(10L).word("Word").build();
        Kanji kanji = Kanji.builder().id(20L).characterValue("Kanji").build();

        when(vocabularyRepository.findAllById(any())).thenReturn(List.of(vocab));
        when(kanjiRepository.findAllById(any())).thenReturn(List.of(kanji));
        when(grammarPointRepository.findAllById(any())).thenReturn(List.of());

        FlashcardResolver.ContentMaps maps = resolver.loadContentMaps(List.of(card1, card2));
        assertNotNull(maps);
        assertEquals("Word", maps.vocab().get(10L).getWord());
        assertEquals("Kanji", maps.kanji().get(20L).getCharacterValue());
    }

    @Test
    void toFlashcardResponse_vocabSuccess() {
        Flashcard card = Flashcard.builder()
                .id(1L)
                .contentType(Flashcard.ContentType.VOCABULARY)
                .contentId(10L)
                .build();

        Vocabulary vocab = Vocabulary.builder()
                .id(10L)
                .word("Word")
                .meaning("Meaning")
                .status(Kanji.ContentStatus.PUBLISHED)
                .jlptLevel(StudentUser.JlptLevel.N5)
                .build();

        FlashcardResolver.ContentMaps maps = new FlashcardResolver.ContentMaps(Map.of(10L, vocab), Map.of(), Map.of());

        FlashcardResponse res = resolver.toFlashcardResponse(card, maps);
        assertEquals("Word", res.frontText());
        assertEquals("Meaning", res.meaning());
        assertEquals("N5", res.jlptLevel());
    }
}
