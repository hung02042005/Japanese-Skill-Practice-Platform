/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.assessment;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class QuestionStatusConverter implements AttributeConverter<Question.ContentStatus, String> {

    @Override
    public String convertToDatabaseColumn(Question.ContentStatus attribute) {
        return attribute == null ? null : attribute.getValue();
    }

    @Override
    public Question.ContentStatus convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }
        for (Question.ContentStatus status : Question.ContentStatus.values()) {
            if (status.getValue().equalsIgnoreCase(dbData.trim())) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown Question.ContentStatus value: " + dbData);
    }
}
