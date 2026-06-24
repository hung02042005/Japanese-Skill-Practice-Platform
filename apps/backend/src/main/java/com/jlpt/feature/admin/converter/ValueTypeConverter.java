/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.admin.converter;

import com.jlpt.feature.admin.entity.SystemSetting;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class ValueTypeConverter implements AttributeConverter<SystemSetting.ValueType, String> {

    @Override
    public String convertToDatabaseColumn(SystemSetting.ValueType attribute) {
        return attribute == null ? null : attribute.getValue();
    }

    @Override
    public SystemSetting.ValueType convertToEntityAttribute(String dbData) {
        if (dbData == null) return null;
        for (SystemSetting.ValueType e : SystemSetting.ValueType.values()) {
            if (e.getValue().equalsIgnoreCase(dbData) || e.name().equalsIgnoreCase(dbData)) return e;
        }
        throw new IllegalArgumentException("Unknown ValueType: " + dbData);
    }
}
