/* (c) JLPT E-Learning Platform */
package com.jlpt.flashcard.dto;

import java.time.LocalDate;

public record FlashcardResponse(
        Long flashcardId,
        Long deckId,
        String contentType,
        Long contentId,
        String frontText,
        boolean isSystem,
        LocalDate nextReviewDate,
        Integer intervalDays,
        Integer repetitionCount,
        String lastRating,
        boolean isDue) {}
