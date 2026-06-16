/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.staff;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class StaffPasswordResetStatusConverter
        implements AttributeConverter<StaffPasswordResetRequest.ResetStatus, String> {

    @Override
    public String convertToDatabaseColumn(StaffPasswordResetRequest.ResetStatus attribute) {
        return attribute == null ? null : attribute.getValue();
    }

    @Override
    public StaffPasswordResetRequest.ResetStatus convertToEntityAttribute(String dbData) {
        if (dbData == null) return null;
        for (StaffPasswordResetRequest.ResetStatus status : StaffPasswordResetRequest.ResetStatus.values()) {
            if (status.getValue().equalsIgnoreCase(dbData) || status.name().equalsIgnoreCase(dbData)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown StaffPasswordResetStatus: " + dbData);
    }
}
