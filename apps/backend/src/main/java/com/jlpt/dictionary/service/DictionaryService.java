/* (c) JLPT E-Learning Platform */
package com.jlpt.dictionary.service;

import com.jlpt.dictionary.dto.SearchResponse;
import com.jlpt.entity.GrammarPoint;
import com.jlpt.entity.Kanji;
import com.jlpt.entity.Lesson;
import com.jlpt.entity.StudentUser;
import com.jlpt.entity.Vocabulary;
import com.jlpt.exception.BadRequestException;
import com.jlpt.repository.GrammarPointRepository;
import com.jlpt.repository.KanjiRepository;
import com.jlpt.repository.LessonRepository;
import com.jlpt.repository.VocabularyRepository;
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

        StudentUser.JlptLevel level = parseJlptLevel(jlptLevel);
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

    private StudentUser.JlptLevel parseJlptLevel(String jlptLevel) {
        if (jlptLevel == null || jlptLevel.isBlank()) return null;
        try {
            return StudentUser.JlptLevel.valueOf(jlptLevel.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Cấp độ JLPT không hợp lệ: " + jlptLevel);
        }
    }

    private SearchResponse.VocabItem toVocabItem(Vocabulary v) {
        return new SearchResponse.VocabItem(
                v.getId(),
                v.getWord(),
                v.getFurigana(),
                v.getMeaning(),
                v.getWordType(),
                v.getJlptLevel() != null ? v.getJlptLevel().name() : null,
                v.getTopic());
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
