/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.student;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class StudentStatusConverter implements AttributeConverter<StudentUser.StudentStatus, String> {

    @Override
    public String convertToDatabaseColumn(StudentUser.StudentStatus attribute) {
        return attribute == null ? null : attribute.getValue();
    }

    @Override
    public StudentUser.StudentStatus convertToEntityAttribute(String dbData) {
        if (dbData == null) return null;
        for (StudentUser.StudentStatus e : StudentUser.StudentStatus.values()) {
            if (e.getValue().equalsIgnoreCase(dbData) || e.name().equalsIgnoreCase(dbData)) return e;
        }
        throw new IllegalArgumentException("Unknown StudentStatus: " + dbData);
    }
}
