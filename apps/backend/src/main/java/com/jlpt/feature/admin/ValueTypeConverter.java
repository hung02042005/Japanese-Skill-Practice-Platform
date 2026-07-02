/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.admin;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class ValueTypeConverter implements AttributeConverter<SystemSetting.ValueType, String> {

    @Override
    public String convertToDatabaseColumn(SystemSetting.ValueType type) {
        return type == null ? null : type.getValue();
    }

    @Override
    public SystemSetting.ValueType convertToEntityAttribute(String dbValue) {
        if (dbValue == null) return SystemSetting.ValueType.STRING;
        for (SystemSetting.ValueType t : SystemSetting.ValueType.values()) {
            if (t.getValue().equalsIgnoreCase(dbValue)) return t;
        }
        return SystemSetting.ValueType.STRING;
    }
}
