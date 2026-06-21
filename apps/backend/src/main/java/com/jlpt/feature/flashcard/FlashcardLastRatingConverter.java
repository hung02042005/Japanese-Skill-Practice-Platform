/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.flashcard;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class FlashcardLastRatingConverter implements AttributeConverter<Flashcard.LastRating, String> {

    @Override
    public String convertToDatabaseColumn(Flashcard.LastRating attribute) {
        return attribute == null ? null : attribute.getValue();
    }

    @Override
    public Flashcard.LastRating convertToEntityAttribute(String dbData) {
        if (dbData == null) return null;
        for (Flashcard.LastRating e : Flashcard.LastRating.values()) {
            if (e.getValue().equalsIgnoreCase(dbData) || e.name().equalsIgnoreCase(dbData)) return e;
        }
        throw new IllegalArgumentException("Unknown FlashcardLastRating: " + dbData);
    }
}
