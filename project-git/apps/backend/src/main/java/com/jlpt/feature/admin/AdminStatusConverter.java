/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.admin;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class AdminStatusConverter implements AttributeConverter<AdminUser.AdminStatus, String> {

    @Override
    public String convertToDatabaseColumn(AdminUser.AdminStatus attribute) {
        return attribute == null ? null : attribute.getValue();
    }

    @Override
    public AdminUser.AdminStatus convertToEntityAttribute(String dbData) {
        if (dbData == null) return null;
        for (AdminUser.AdminStatus e : AdminUser.AdminStatus.values()) {
            if (e.getValue().equalsIgnoreCase(dbData) || e.name().equalsIgnoreCase(dbData)) return e;
        }
        throw new IllegalArgumentException("Unknown AdminStatus: " + dbData);
    }
}
