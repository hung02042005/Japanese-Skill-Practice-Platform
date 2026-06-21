/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.flashcard.dto;

import com.jlpt.feature.flashcard.FlashcardConstants;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record AddFlashcardRequest(
        @NotNull @Pattern(regexp = "VOCABULARY|KANJI|GRAMMAR|CUSTOM") String contentType,
        Long contentId,
        @Positive Long deckId,
        @Size(max = FlashcardConstants.DECK_NAME_MAX) String deckName,
        String frontText,
        String backText) {}
