/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.learning;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class LessonTypeConverter implements AttributeConverter<Lesson.LessonType, String> {

    @Override
    public String convertToDatabaseColumn(Lesson.LessonType attribute) {
        return attribute == null ? null : attribute.getValue();
    }

    @Override
    public Lesson.LessonType convertToEntityAttribute(String dbData) {
        if (dbData == null) return null;
        for (Lesson.LessonType e : Lesson.LessonType.values()) {
            if (e.getValue().equalsIgnoreCase(dbData) || e.name().equalsIgnoreCase(dbData)) return e;
        }
        throw new IllegalArgumentException("Unknown LessonType: " + dbData);
    }
}
