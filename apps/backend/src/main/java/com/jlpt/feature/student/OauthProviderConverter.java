/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.student;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class OauthProviderConverter implements AttributeConverter<StudentUser.OauthProvider, String> {

    @Override
    public String convertToDatabaseColumn(StudentUser.OauthProvider attribute) {
        return attribute == null ? null : attribute.getValue();
    }

    @Override
    public StudentUser.OauthProvider convertToEntityAttribute(String dbData) {
        if (dbData == null) return null;
        for (StudentUser.OauthProvider e : StudentUser.OauthProvider.values()) {
            if (e.getValue().equalsIgnoreCase(dbData) || e.name().equalsIgnoreCase(dbData)) return e;
        }
        throw new IllegalArgumentException("Unknown OauthProvider: " + dbData);
    }
}
