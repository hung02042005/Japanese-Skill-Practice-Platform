/* (c) JLPT E-Learning Platform */
package com.jlpt.converter;

import com.jlpt.entity.StudentContentProgress;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class StudentProgressContentTypeConverter
        implements AttributeConverter<StudentContentProgress.ContentType, String> {

    @Override
    public String convertToDatabaseColumn(StudentContentProgress.ContentType attribute) {
        return attribute == null ? null : attribute.getValue();
    }

    @Override
    public StudentContentProgress.ContentType convertToEntityAttribute(String dbData) {
        if (dbData == null) return null;
        for (StudentContentProgress.ContentType e : StudentContentProgress.ContentType.values()) {
            if (e.getValue().equalsIgnoreCase(dbData) || e.name().equalsIgnoreCase(dbData)) return e;
        }
        throw new IllegalArgumentException("Unknown StudentProgressContentType: " + dbData);
    }
}
