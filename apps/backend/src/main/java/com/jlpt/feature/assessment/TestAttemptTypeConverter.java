/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.assessment;

import com.jlpt.feature.assessment.TestAttempt.AttemptType;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * Map {@link TestAttempt.AttemptType} ↔ chữ thường DB ('exam','quiz','practice','reading','listening').
 * Xem lý do tại {@link QuestionAssignmentParentTypeConverter}.
 */
@Converter(autoApply = true)
public class TestAttemptTypeConverter implements AttributeConverter<AttemptType, String> {

    @Override
    public String convertToDatabaseColumn(AttemptType attribute) {
        return attribute == null ? null : attribute.getValue();
    }

    @Override
    public AttemptType convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }
        for (AttemptType t : AttemptType.values()) {
            if (t.getValue().equalsIgnoreCase(dbData.trim())) {
                return t;
            }
        }
        throw new IllegalArgumentException("Unknown AttemptType value: " + dbData);
    }
}
