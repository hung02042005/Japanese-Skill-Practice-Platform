/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.assessment;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class QuestionTypeConverter implements AttributeConverter<Question.QuestionType, String> {

    @Override
    public String convertToDatabaseColumn(Question.QuestionType attribute) {
        return attribute == null ? null : attribute.getValue();
    }

    @Override
    public Question.QuestionType convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }
        for (Question.QuestionType type : Question.QuestionType.values()) {
            if (type.getValue().equalsIgnoreCase(dbData.trim())) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown QuestionType value: " + dbData);
    }
}
