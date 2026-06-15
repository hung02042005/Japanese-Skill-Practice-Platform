/* (c) JLPT E-Learning Platform */
package com.jlpt.converter;

import com.jlpt.entity.StudentContentProgress;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class StudentProgressStatusConverter
        implements AttributeConverter<StudentContentProgress.ProgressStatus, String> {

    @Override
    public String convertToDatabaseColumn(StudentContentProgress.ProgressStatus attribute) {
        return attribute == null ? null : attribute.getValue();
    }

    @Override
    public StudentContentProgress.ProgressStatus convertToEntityAttribute(String dbData) {
        if (dbData == null) return null;
        for (StudentContentProgress.ProgressStatus e : StudentContentProgress.ProgressStatus.values()) {
            if (e.getValue().equalsIgnoreCase(dbData) || e.name().equalsIgnoreCase(dbData)) return e;
        }
        throw new IllegalArgumentException("Unknown StudentProgressStatus: " + dbData);
    }
}
