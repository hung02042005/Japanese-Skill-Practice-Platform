/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.assessment;

import com.jlpt.feature.assessment.TestAttempt.ParentType;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * Map {@link TestAttempt.ParentType} ↔ chữ thường DB ('assessment','lesson','random_practice').
 * Xem lý do tại {@link QuestionAssignmentParentTypeConverter}.
 */
@Converter(autoApply = true)
public class TestAttemptParentTypeConverter implements AttributeConverter<ParentType, String> {

    @Override
    public String convertToDatabaseColumn(ParentType attribute) {
        return attribute == null ? null : attribute.getValue();
    }

    @Override
    public ParentType convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }
        for (ParentType t : ParentType.values()) {
            if (t.getValue().equalsIgnoreCase(dbData.trim())) {
                return t;
            }
        }
        throw new IllegalArgumentException("Unknown TestAttempt.ParentType value: " + dbData);
    }
}
