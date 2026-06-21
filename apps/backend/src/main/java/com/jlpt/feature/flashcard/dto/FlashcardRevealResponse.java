/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.flashcard.dto;

public record FlashcardRevealResponse(
        Long flashcardId,
        String frontText,
        String backText,
        String furigana,
        String exampleSentenceJp,
        String exampleSentenceVi,
        String audioUrl,
        String strokeOrderUrl) {}
