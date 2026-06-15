/* (c) JLPT E-Learning Platform */
package com.jlpt.dictionary.dto;

import java.util.List;

public record SearchResponse(
        String keyword,
        List<VocabItem> vocabulary,
        List<KanjiItem> kanji,
        List<GrammarItem> grammar,
        List<LessonItem> lessons) {

    public record VocabItem(
            Long id, String word, String furigana, String meaning, String wordType, String jlptLevel, String topic) {}

    public record KanjiItem(
            Long id, String character, String meaning, String onyomi, String kunyomi, String jlptLevel) {}

    public record GrammarItem(Long id, String structure, String meaning, String formula, String jlptLevel) {}

    public record LessonItem(Long id, String title, String jlptLevel, String lessonType) {}
}
