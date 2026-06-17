/* (c) JLPT E-Learning Platform */
package com.jlpt.converter;

import com.jlpt.entity.StaffUser;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class StaffRoleConverter implements AttributeConverter<StaffUser.StaffRole, String> {

    @Override
    public String convertToDatabaseColumn(StaffUser.StaffRole attribute) {
        return attribute == null ? null : attribute.getValue();
    }

    @Override
    public StaffUser.StaffRole convertToEntityAttribute(String dbData) {
        if (dbData == null) return null;
        for (StaffUser.StaffRole e : StaffUser.StaffRole.values()) {
            if (e.getValue().equalsIgnoreCase(dbData) || e.name().equalsIgnoreCase(dbData)) return e;
        }
        throw new IllegalArgumentException("Unknown StaffRole: " + dbData);
    }
}
