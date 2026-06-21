/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.flashcard.dto;

public record DeckSummaryResponse(
        Long deckId,
        String deckName,
        int totalCards,
        int dueToday,
        String jlptLevel,
        String topic,
        boolean isSystem,
        boolean isReviewDeck) {}
