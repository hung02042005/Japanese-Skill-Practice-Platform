/* (c) JLPT E-Learning Platform */
package com.jlpt.dto.response;

import java.util.List;

public record VocabularyPageResponse(
        List<VocabularyItemResponse> content,
        int totalPages,
        long totalElements,
        int number,
        int size,
        long completedCount) {

    public record VocabularyItemResponse(
            Long vocabId,
            String word,
            String furigana,
            String meaning,
            String wordType,
            String jlptLevel,
            String topic,
            String audioUrl,
            String exampleSentenceJp,
            String exampleSentenceVi,
            boolean isCompleted,
            boolean isInFlashcard) {}
}
