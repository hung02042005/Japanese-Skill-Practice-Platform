/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.dictionary.service;

import com.jlpt.feature.dictionary.dto.SearchResponse;
import com.jlpt.feature.dictionary.dto.TypeSearchResponse;
import com.jlpt.feature.learning.GrammarPoint;
import com.jlpt.feature.learning.GrammarPointRepository;
import com.jlpt.feature.learning.Kanji;
import com.jlpt.feature.learning.KanjiRepository;
import com.jlpt.feature.learning.Lesson;
import com.jlpt.feature.learning.LessonRepository;
import com.jlpt.feature.learning.Vocabulary;
import com.jlpt.feature.learning.VocabularyRepository;
import com.jlpt.feature.student.StudentUser;
import com.jlpt.shared.common.JlptLevels;
import com.jlpt.shared.exception.BadRequestException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DictionaryService {

    private static final int MAX_RESULTS_PER_TYPE = 10;

    private final VocabularyRepository vocabularyRepository;
    private final KanjiRepository kanjiRepository;
    private final GrammarPointRepository grammarPointRepository;
    private final LessonRepository lessonRepository;

    public SearchResponse search(String keyword, String jlptLevel, String type) {
        if (keyword == null || keyword.isBlank()) {
            throw new BadRequestException("Từ khóa tìm kiếm không được để trống");
        }

        StudentUser.JlptLevel level = JlptLevels.parseOptional(jlptLevel);
        PageRequest limit = PageRequest.of(0, MAX_RESULTS_PER_TYPE);

        List<SearchResponse.VocabItem> vocabulary = List.of();
        List<SearchResponse.KanjiItem> kanji = List.of();
        List<SearchResponse.GrammarItem> grammar = List.of();
        List<SearchResponse.LessonItem> lessons = List.of();

        if (type == null || "VOCABULARY".equalsIgnoreCase(type)) {
            vocabulary =
                    vocabularyRepository.searchPublished(keyword, level, Kanji.ContentStatus.PUBLISHED, limit).stream()
                            .map(this::toVocabItem)
                            .toList();
        }
        if (type == null || "KANJI".equalsIgnoreCase(type)) {
            kanji = kanjiRepository.searchPublished(keyword, level, Kanji.ContentStatus.PUBLISHED, limit).stream()
                    .map(this::toKanjiItem)
                    .toList();
        }
        if (type == null || "GRAMMAR".equalsIgnoreCase(type)) {
            grammar =
                    grammarPointRepository
                            .searchPublished(keyword, level, Kanji.ContentStatus.PUBLISHED, limit)
                            .stream()
                            .map(this::toGrammarItem)
                            .toList();
        }
        if (type == null || "LESSON".equalsIgnoreCase(type)) {
            lessons = lessonRepository.searchPublished(keyword, level, Lesson.LessonStatus.PUBLISHED, limit).stream()
                    .map(this::toLessonItem)
                    .toList();
        }

        return new SearchResponse(keyword, vocabulary, kanji, grammar, lessons);
    }

    /**
     * Tra cứu phân trang cho MỘT loại — phục vụ "Xem thêm" (1B). page-index khớp với overview
     * (size mặc định = {@link #MAX_RESULTS_PER_TYPE}): page 0 = đúng 10 mục overview, page 1+ nối tiếp.
     * {@code hasMore} suy từ việc trang trả về đủ {@code size} phần tử (tránh COUNT thừa).
     */
    public TypeSearchResponse searchByType(String keyword, String jlptLevel, String type, int page, int size) {
        if (keyword == null || keyword.isBlank()) {
            throw new BadRequestException("Từ khóa tìm kiếm không được để trống");
        }
        if (type == null || type.isBlank()) {
            throw new BadRequestException("Tham số 'type' là bắt buộc");
        }
        StudentUser.JlptLevel level = JlptLevels.parseOptional(jlptLevel);
        int capped = Math.min(Math.max(size, 1), 50);
        PageRequest pageable = PageRequest.of(Math.max(page, 0), capped);
        Kanji.ContentStatus pub = Kanji.ContentStatus.PUBLISHED;

        List<Object> items =
                switch (type.toUpperCase()) {
                    case "VOCABULARY" -> vocabularyRepository.searchPublished(keyword, level, pub, pageable).stream()
                            .map(this::toVocabItem)
                            .map(Object.class::cast)
                            .toList();
                    case "KANJI" -> kanjiRepository.searchPublished(keyword, level, pub, pageable).stream()
                            .map(this::toKanjiItem)
                            .map(Object.class::cast)
                            .toList();
                    case "GRAMMAR" -> grammarPointRepository.searchPublished(keyword, level, pub, pageable).stream()
                            .map(this::toGrammarItem)
                            .map(Object.class::cast)
                            .toList();
                    case "LESSON" -> lessonRepository
                            .searchPublished(keyword, level, Lesson.LessonStatus.PUBLISHED, pageable)
                            .stream()
                            .map(this::toLessonItem)
                            .map(Object.class::cast)
                            .toList();
                    default -> throw new BadRequestException("Loại không hợp lệ: " + type);
                };

        boolean hasMore = items.size() == capped;
        return new TypeSearchResponse(type.toUpperCase(), items, hasMore);
    }

    private SearchResponse.VocabItem toVocabItem(Vocabulary v) {
        return new SearchResponse.VocabItem(
                v.getId(),
                v.getWord(),
                v.getFurigana(),
                v.getMeaning(),
                v.getWordType(),
                v.getJlptLevel() != null ? v.getJlptLevel().name() : null,
                v.getTopicRef() != null ? v.getTopicRef().getTitleVi() : null,
                v.getExampleSentenceJp(),
                v.getExampleSentenceVi(),
                v.getAudioUrl());
    }

    private SearchResponse.KanjiItem toKanjiItem(Kanji k) {
        return new SearchResponse.KanjiItem(
                k.getId(),
                k.getCharacterValue(),
                k.getMeaning(),
                k.getOnyomi(),
                k.getKunyomi(),
                k.getJlptLevel() != null ? k.getJlptLevel().name() : null);
    }

    private SearchResponse.GrammarItem toGrammarItem(GrammarPoint g) {
        return new SearchResponse.GrammarItem(
                g.getId(),
                g.getStructure(),
                g.getMeaning(),
                g.getFormula(),
                g.getJlptLevel() != null ? g.getJlptLevel().name() : null);
    }

    private SearchResponse.LessonItem toLessonItem(Lesson l) {
        return new SearchResponse.LessonItem(
                l.getId(),
                l.getTitle(),
                l.getJlptLevel() != null ? l.getJlptLevel().name() : null,
                l.getLessonType() != null ? l.getLessonType().name() : null);
    }
}
