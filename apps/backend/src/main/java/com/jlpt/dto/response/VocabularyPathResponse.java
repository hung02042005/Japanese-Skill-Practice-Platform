/* (c) JLPT E-Learning Platform */
package com.jlpt.dto.response;

public record VocabularyPathResponse(
        Long topicId,
        String slug,
        String titleJa,
        String titleVi,
        int order,
        String level,
        long totalWords,
        long completedWords,
        String status) {}
