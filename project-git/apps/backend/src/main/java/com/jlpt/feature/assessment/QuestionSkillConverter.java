/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.assessment;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class QuestionSkillConverter implements AttributeConverter<Question.Skill, String> {

    @Override
    public String convertToDatabaseColumn(Question.Skill attribute) {
        return attribute == null ? null : attribute.getValue();
    }

    @Override
    public Question.Skill convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }
        for (Question.Skill skill : Question.Skill.values()) {
            if (skill.getValue().equalsIgnoreCase(dbData.trim())) {
                return skill;
            }
        }
        throw new IllegalArgumentException("Unknown Skill value: " + dbData);
    }
}
