/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.auth.converter;

import com.jlpt.feature.auth.entity.AuthToken;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class ActorTypeConverter implements AttributeConverter<AuthToken.ActorType, String> {

    @Override
    public String convertToDatabaseColumn(AuthToken.ActorType attribute) {
        return attribute == null ? null : attribute.getValue();
    }

    @Override
    public AuthToken.ActorType convertToEntityAttribute(String dbData) {
        if (dbData == null) return null;
        for (AuthToken.ActorType e : AuthToken.ActorType.values()) {
            if (e.getValue().equalsIgnoreCase(dbData) || e.name().equalsIgnoreCase(dbData)) return e;
        }
        throw new IllegalArgumentException("Unknown ActorType: " + dbData);
    }
}
