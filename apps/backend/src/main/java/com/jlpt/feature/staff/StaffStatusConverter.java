/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.staff;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class StaffStatusConverter implements AttributeConverter<StaffUser.StaffStatus, String> {

    @Override
    public String convertToDatabaseColumn(StaffUser.StaffStatus attribute) {
        return attribute == null ? null : attribute.getValue();
    }

    @Override
    public StaffUser.StaffStatus convertToEntityAttribute(String dbData) {
        if (dbData == null) return null;
        for (StaffUser.StaffStatus e : StaffUser.StaffStatus.values()) {
            if (e.getValue().equalsIgnoreCase(dbData) || e.name().equalsIgnoreCase(dbData)) return e;
        }
        throw new IllegalArgumentException("Unknown StaffStatus: " + dbData);
    }
}
