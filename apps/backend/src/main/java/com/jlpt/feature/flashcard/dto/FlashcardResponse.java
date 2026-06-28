/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.flashcard.dto;

import java.time.LocalDate;

public record FlashcardResponse(
        Long flashcardId,
        Long deckId,
        String contentType,
        Long contentId,
        String frontText,
        String meaning,
        String furigana,
        String audioUrl,
        String jlptLevel,
        boolean isSystem,
        LocalDate nextReviewDate,
        Integer intervalDays,
        Integer repetitionCount,
        String lastRating,
        String addedReason,
        boolean isDue) {}
