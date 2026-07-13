/* (c) JLPT E-Learning Platform */
package com.jlpt.shared.email;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class EmailOutboxStatusConverter implements AttributeConverter<EmailOutbox.Status, String> {

    @Override
    public String convertToDatabaseColumn(EmailOutbox.Status attribute) {
        return attribute == null ? null : attribute.getValue();
    }

    @Override
    public EmailOutbox.Status convertToEntityAttribute(String dbData) {
        if (dbData == null) return null;
        for (EmailOutbox.Status e : EmailOutbox.Status.values()) {
            if (e.getValue().equalsIgnoreCase(dbData) || e.name().equalsIgnoreCase(dbData)) return e;
        }
        throw new IllegalArgumentException("Unknown EmailOutbox.Status: " + dbData);
    }
}
