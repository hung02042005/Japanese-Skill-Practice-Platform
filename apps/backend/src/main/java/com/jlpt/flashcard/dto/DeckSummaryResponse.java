/* (c) JLPT E-Learning Platform */
package com.jlpt.flashcard.dto;

public record DeckSummaryResponse(
        Long deckId,
        String deckName,
        String displayName,
        int totalCards,
        int dueToday,
        String jlptLevel,
        String topic,
        boolean isSystem) {}
