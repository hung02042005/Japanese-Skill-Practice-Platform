/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.dictionary;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.jlpt.feature.dictionary.dto.SearchResponse;
import com.jlpt.feature.dictionary.dto.TypeSearchResponse;
import com.jlpt.feature.dictionary.service.DictionaryService;
import com.jlpt.feature.learning.GrammarPointRepository;
import com.jlpt.feature.learning.Kanji;
import com.jlpt.feature.learning.KanjiRepository;
import com.jlpt.feature.learning.LessonRepository;
import com.jlpt.feature.learning.Vocabulary;
import com.jlpt.feature.learning.VocabularyRepository;
import com.jlpt.shared.exception.BadRequestException;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class DictionaryServiceTest {

    @Mock
    private VocabularyRepository vocabularyRepository;

    @Mock
    private KanjiRepository kanjiRepository;

    @Mock
    private GrammarPointRepository grammarPointRepository;

    @Mock
    private LessonRepository lessonRepository;

    @InjectMocks
    private DictionaryService service;

    @Test
    void search_blankKeywordThrows() {
        assertThrows(BadRequestException.class, () -> service.search("", "N5", null));
    }

    @Test
    void search_allTypesSuccess() {
        Vocabulary v =
                Vocabulary.builder().id(1L).word("Word").meaning("Meaning").build();
        when(vocabularyRepository.searchPublished(
                        eq("test"), any(), eq(Kanji.ContentStatus.PUBLISHED), any(Pageable.class)))
                .thenReturn(List.of(v));

        SearchResponse res = service.search("test", "N5", null);
        assertEquals("test", res.keyword());
        assertEquals(1, res.vocabulary().size());
        assertEquals("Word", res.vocabulary().get(0).word());
    }

    @Test
    void searchByType_vocabularySuccess() {
        Vocabulary v =
                Vocabulary.builder().id(1L).word("Word").meaning("Meaning").build();
        when(vocabularyRepository.searchPublished(
                        eq("test"), any(), eq(Kanji.ContentStatus.PUBLISHED), any(Pageable.class)))
                .thenReturn(List.of(v));

        TypeSearchResponse res = service.searchByType("test", "N5", "VOCABULARY", 0, 10);
        assertEquals("VOCABULARY", res.type());
        assertEquals(1, res.items().size());
        assertFalse(res.hasMore());
    }
}
