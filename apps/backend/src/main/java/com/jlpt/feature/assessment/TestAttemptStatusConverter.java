/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.assessment;

import com.jlpt.feature.assessment.TestAttempt.AttemptStatus;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * Map {@link TestAttempt.AttemptStatus} ↔ chữ thường DB ('in_progress','submitted','auto_submitted','abandoned').
 * Quan trọng cả chiều ghi: {@code @Enumerated} sẽ ghi 'IN_PROGRESS' (chữ HOA) vi phạm CHECK constraint.
 */
@Converter(autoApply = true)
public class TestAttemptStatusConverter implements AttributeConverter<AttemptStatus, String> {

    @Override
    public String convertToDatabaseColumn(AttemptStatus attribute) {
        return attribute == null ? null : attribute.getValue();
    }

    @Override
    public AttemptStatus convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }
        for (AttemptStatus s : AttemptStatus.values()) {
            if (s.getValue().equalsIgnoreCase(dbData.trim())) {
                return s;
            }
        }
        throw new IllegalArgumentException("Unknown AttemptStatus value: " + dbData);
    }
}
