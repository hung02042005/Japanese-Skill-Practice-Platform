/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.flashcard.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DeckCreateRequest(@NotBlank @Size(max = 100) String deckName) {}
