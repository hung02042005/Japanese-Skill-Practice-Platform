/* (c) JLPT E-Learning Platform */
package com.jlpt.flashcard.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record AddFlashcardRequest(
        @NotNull @Pattern(regexp = "VOCABULARY|KANJI|GRAMMAR|CUSTOM") String contentType,
        Long contentId,
        @Positive Long deckId,
        @Size(max = 255) String deckName,
        String frontText,
        String backText) {}
