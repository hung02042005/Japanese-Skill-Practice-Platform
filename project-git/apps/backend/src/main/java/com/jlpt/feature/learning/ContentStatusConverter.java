/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.learning;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class ContentStatusConverter implements AttributeConverter<Kanji.ContentStatus, String> {

    @Override
    public String convertToDatabaseColumn(Kanji.ContentStatus attribute) {
        return attribute == null ? null : attribute.getValue();
    }

    @Override
    public Kanji.ContentStatus convertToEntityAttribute(String dbData) {
        if (dbData == null) return null;
        for (Kanji.ContentStatus e : Kanji.ContentStatus.values()) {
            if (e.getValue().equalsIgnoreCase(dbData) || e.name().equalsIgnoreCase(dbData)) return e;
        }
        throw new IllegalArgumentException("Unknown ContentStatus: " + dbData);
    }
}
