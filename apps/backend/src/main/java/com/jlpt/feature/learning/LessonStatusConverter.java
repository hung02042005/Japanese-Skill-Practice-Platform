/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.learning;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class LessonStatusConverter implements AttributeConverter<Lesson.LessonStatus, String> {

    @Override
    public String convertToDatabaseColumn(Lesson.LessonStatus attribute) {
        return attribute == null ? null : attribute.getValue();
    }

    @Override
    public Lesson.LessonStatus convertToEntityAttribute(String dbData) {
        if (dbData == null) return null;
        for (Lesson.LessonStatus e : Lesson.LessonStatus.values()) {
            if (e.getValue().equalsIgnoreCase(dbData) || e.name().equalsIgnoreCase(dbData)) return e;
        }
        throw new IllegalArgumentException("Unknown LessonStatus: " + dbData);
    }
}
