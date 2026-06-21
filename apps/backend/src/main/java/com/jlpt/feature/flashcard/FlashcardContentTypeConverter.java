/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.flashcard;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class FlashcardContentTypeConverter implements AttributeConverter<Flashcard.ContentType, String> {

    @Override
    public String convertToDatabaseColumn(Flashcard.ContentType attribute) {
        return attribute == null ? null : attribute.getValue();
    }

    @Override
    public Flashcard.ContentType convertToEntityAttribute(String dbData) {
        if (dbData == null) return null;
        for (Flashcard.ContentType e : Flashcard.ContentType.values()) {
            if (e.getValue().equalsIgnoreCase(dbData) || e.name().equalsIgnoreCase(dbData)) return e;
        }
        throw new IllegalArgumentException("Unknown FlashcardContentType: " + dbData);
    }
}
