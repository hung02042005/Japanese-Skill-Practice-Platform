/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.flashcard.dto;

import com.jlpt.feature.flashcard.FlashcardConstants;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DeckCreateRequest(@NotBlank @Size(max = FlashcardConstants.DECK_NAME_MAX) String deckName) {}
