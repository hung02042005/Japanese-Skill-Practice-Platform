/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.dictionary;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.jlpt.feature.dictionary.dto.SearchResponse;
import com.jlpt.feature.dictionary.dto.TypeSearchResponse;
import com.jlpt.feature.dictionary.service.DictionaryService;
import com.jlpt.feature.learning.GrammarPoint;
import com.jlpt.feature.learning.GrammarPointRepository;
import com.jlpt.feature.learning.Kanji;
import com.jlpt.feature.learning.KanjiRepository;
import com.jlpt.feature.learning.Lesson;
import com.jlpt.feature.learning.LessonRepository;
import com.jlpt.feature.learning.Vocabulary;
import com.jlpt.feature.learning.VocabularyRepository;
import com.jlpt.feature.learning.VocabularyTopic;
import com.jlpt.feature.student.StudentUser.JlptLevel;
import com.jlpt.shared.exception.BadRequestException;
import java.util.List;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

/**
 * Bổ sung độ phủ tra từ điển: lọc theo từng loại nội dung, phân trang "Xem thêm" (giới hạn size,
 * suy hasMore) và các nhánh map khi thiếu cấp độ/chủ đề.
 */
@ExtendWith(MockitoExtension.class)
class DictionaryServiceCoverageTest {

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

    private Vocabulary vocabulary() {
        return Vocabulary.builder()
                .id(1L)
                .word("家族")
                .furigana("かぞく")
                .meaning("Gia đình")
                .wordType("danh từ")
                .jlptLevel(JlptLevel.N5)
                .topicRef(VocabularyTopic.builder().id(5L).titleVi("Gia đình").build())
                .exampleSentenceJp("私の家族です。")
                .exampleSentenceVi("Đây là gia đình tôi.")
                .audioUrl("/uploads/audio/1.mp3")
                .build();
    }

    private Kanji kanji() {
        return Kanji.builder()
                .id(2L)
                .characterValue("日")
                .meaning("Mặt trời")
                .onyomi("ニチ")
                .kunyomi("ひ")
                .jlptLevel(JlptLevel.N5)
                .build();
    }

    private GrammarPoint grammar() {
        return GrammarPoint.builder()
                .id(3L)
                .structure("V-てから")
                .meaning("Sau khi làm V")
                .formula("V-て + から")
                .jlptLevel(JlptLevel.N5)
                .build();
    }

    private Lesson lesson() {
        return Lesson.builder()
                .id(4L)
                .title("Bài 1")
                .jlptLevel(JlptLevel.N5)
                .lessonType(Lesson.LessonType.READING)
                .build();
    }

    // ── search: lọc theo type ────────────────────────────────────────────────

    @Test
    void search_nullKeyword_throwsBadRequest() {
        assertThrows(BadRequestException.class, () -> service.search(null, "N5", null));
    }

    @Test
    void search_noTypeFilter_queriesAllFourSources() {
        when(vocabularyRepository.searchPublished(eq("a"), any(), eq(Kanji.ContentStatus.PUBLISHED), any()))
                .thenReturn(List.of(vocabulary()));
        when(kanjiRepository.searchPublished(eq("a"), any(), eq(Kanji.ContentStatus.PUBLISHED), any()))
                .thenReturn(List.of(kanji()));
        when(grammarPointRepository.searchPublished(eq("a"), any(), eq(Kanji.ContentStatus.PUBLISHED), any()))
                .thenReturn(List.of(grammar()));
        when(lessonRepository.searchPublished(eq("a"), any(), eq(Lesson.LessonStatus.PUBLISHED), any()))
                .thenReturn(List.of(lesson()));

        SearchResponse response = service.search("a", null, null);

        assertEquals(1, response.vocabulary().size());
        assertEquals(1, response.kanji().size());
        assertEquals(1, response.grammar().size());
        assertEquals(1, response.lessons().size());
    }

    @Test
    void search_kanjiTypeOnly_skipsOtherSources() {
        when(kanjiRepository.searchPublished(eq("nhat"), any(), eq(Kanji.ContentStatus.PUBLISHED), any()))
                .thenReturn(List.of(kanji()));

        SearchResponse response = service.search("nhat", "N5", "kanji");

        assertEquals(1, response.kanji().size());
        assertTrue(response.vocabulary().isEmpty());
        assertTrue(response.grammar().isEmpty());
        assertTrue(response.lessons().isEmpty());
        verifyNoInteractions(vocabularyRepository, grammarPointRepository, lessonRepository);
    }

    @Test
    void search_grammarTypeOnly_mapsGrammarItem() {
        when(grammarPointRepository.searchPublished(eq("te"), any(), eq(Kanji.ContentStatus.PUBLISHED), any()))
                .thenReturn(List.of(grammar()));

        SearchResponse.GrammarItem item =
                service.search("te", null, "GRAMMAR").grammar().get(0);

        assertEquals("V-てから", item.structure());
        assertEquals("V-て + から", item.formula());
        assertEquals("N5", item.jlptLevel());
    }

    @Test
    void search_lessonTypeOnly_mapsLessonItem() {
        when(lessonRepository.searchPublished(eq("bai"), any(), eq(Lesson.LessonStatus.PUBLISHED), any()))
                .thenReturn(List.of(lesson()));

        SearchResponse.LessonItem item =
                service.search("bai", null, "lesson").lessons().get(0);

        assertEquals("Bài 1", item.title());
        assertEquals("READING", item.lessonType());
    }

    @Test
    void search_vocabularyMapsAllFields() {
        when(vocabularyRepository.searchPublished(eq("gia"), any(), eq(Kanji.ContentStatus.PUBLISHED), any()))
                .thenReturn(List.of(vocabulary()));

        SearchResponse.VocabItem item =
                service.search("gia", null, "VOCABULARY").vocabulary().get(0);

        assertEquals("家族", item.word());
        assertEquals("かぞく", item.furigana());
        assertEquals("danh từ", item.wordType());
        assertEquals("N5", item.jlptLevel());
        assertEquals("Gia đình", item.topic());
        assertEquals("/uploads/audio/1.mp3", item.audioUrl());
    }

    @Test
    void search_entitiesWithoutLevelOrTopic_mapToNulls() {
        Vocabulary bare = Vocabulary.builder().id(1L).word("x").build();
        Kanji bareKanji = Kanji.builder().id(2L).characterValue("y").build();
        Lesson bareLesson = Lesson.builder().id(4L).title("z").build();
        bareLesson.setLessonType(null);
        when(vocabularyRepository.searchPublished(any(), any(), any(), any())).thenReturn(List.of(bare));
        when(kanjiRepository.searchPublished(any(), any(), any(), any())).thenReturn(List.of(bareKanji));
        when(grammarPointRepository.searchPublished(any(), any(), any(), any()))
                .thenReturn(List.of(GrammarPoint.builder().id(3L).structure("s").build()));
        when(lessonRepository.searchPublished(any(), any(), any(), any())).thenReturn(List.of(bareLesson));

        SearchResponse response = service.search("x", null, null);

        assertNull(response.vocabulary().get(0).jlptLevel());
        assertNull(response.vocabulary().get(0).topic());
        assertNull(response.kanji().get(0).jlptLevel());
        assertNull(response.grammar().get(0).jlptLevel());
        assertNull(response.lessons().get(0).jlptLevel());
        assertNull(response.lessons().get(0).lessonType());
    }

    // ── searchByType ─────────────────────────────────────────────────────────

    @Test
    void searchByType_blankKeyword_throwsBadRequest() {
        assertThrows(BadRequestException.class, () -> service.searchByType("  ", null, "KANJI", 0, 10));
    }

    @Test
    void searchByType_missingType_throwsBadRequest() {
        assertThrows(BadRequestException.class, () -> service.searchByType("a", null, null, 0, 10));
    }

    @Test
    void searchByType_blankType_throwsBadRequest() {
        assertThrows(BadRequestException.class, () -> service.searchByType("a", null, "  ", 0, 10));
    }

    @Test
    void searchByType_unknownType_throwsBadRequest() {
        assertThrows(BadRequestException.class, () -> service.searchByType("a", null, "PODCAST", 0, 10));
    }

    @Test
    void searchByType_kanji_returnsItemsAndUppercasesType() {
        when(kanjiRepository.searchPublished(eq("nhat"), any(), eq(Kanji.ContentStatus.PUBLISHED), any()))
                .thenReturn(List.of(kanji()));

        TypeSearchResponse response = service.searchByType("nhat", "N5", "kanji", 0, 10);

        assertEquals("KANJI", response.type());
        assertEquals(1, response.items().size());
        assertFalse(response.hasMore());
    }

    @Test
    void searchByType_grammar_returnsItems() {
        when(grammarPointRepository.searchPublished(eq("te"), any(), eq(Kanji.ContentStatus.PUBLISHED), any()))
                .thenReturn(List.of(grammar()));

        assertEquals(
                "GRAMMAR", service.searchByType("te", null, "GRAMMAR", 0, 10).type());
    }

    @Test
    void searchByType_lesson_returnsItems() {
        when(lessonRepository.searchPublished(eq("bai"), any(), eq(Lesson.LessonStatus.PUBLISHED), any()))
                .thenReturn(List.of(lesson()));

        assertEquals(
                1, service.searchByType("bai", null, "LESSON", 0, 10).items().size());
    }

    @Test
    void searchByType_fullPage_reportsHasMore() {
        List<Kanji> fullPage = IntStream.range(0, 10).mapToObj(i -> kanji()).toList();
        when(kanjiRepository.searchPublished(eq("a"), any(), eq(Kanji.ContentStatus.PUBLISHED), any()))
                .thenReturn(fullPage);

        assertTrue(service.searchByType("a", null, "KANJI", 0, 10).hasMore());
    }

    @Test
    void searchByType_sizeAboveCap_isClampedTo50() {
        when(kanjiRepository.searchPublished(any(), any(), any(), any())).thenReturn(List.of());

        service.searchByType("a", null, "KANJI", 0, 999);

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(kanjiRepository).searchPublished(any(), any(), any(), captor.capture());
        assertEquals(50, captor.getValue().getPageSize());
    }

    @Test
    void searchByType_nonPositiveSizeAndNegativePage_areNormalized() {
        when(kanjiRepository.searchPublished(any(), any(), any(), any())).thenReturn(List.of());

        service.searchByType("a", null, "KANJI", -3, 0);

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(kanjiRepository).searchPublished(any(), any(), any(), captor.capture());
        assertEquals(1, captor.getValue().getPageSize());
        assertEquals(0, captor.getValue().getPageNumber());
    }

    @Test
    void searchByType_secondPage_isPassedThrough() {
        when(vocabularyRepository.searchPublished(any(), any(), any(), any())).thenReturn(List.of(vocabulary()));

        service.searchByType("a", "N5", "VOCABULARY", 2, 10);

        verify(vocabularyRepository).searchPublished(eq("a"), eq(JlptLevel.N5), any(), eq(PageRequest.of(2, 10)));
    }
}
