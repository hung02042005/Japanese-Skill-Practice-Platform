/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.assessment;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class AssessmentTypeConverter implements AttributeConverter<Assessment.AssessmentType, String> {

    @Override
    public String convertToDatabaseColumn(Assessment.AssessmentType attribute) {
        return attribute == null ? null : attribute.getValue();
    }

    @Override
    public Assessment.AssessmentType convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }
        for (Assessment.AssessmentType type : Assessment.AssessmentType.values()) {
            if (type.getValue().equalsIgnoreCase(dbData.trim())) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown AssessmentType value: " + dbData);
    }
}
