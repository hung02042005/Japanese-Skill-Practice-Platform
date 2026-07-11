/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.flashcard.dto;

public record DeckSummaryResponse(Long deckId, String deckName, int totalCards, boolean isReviewDeck) {}
