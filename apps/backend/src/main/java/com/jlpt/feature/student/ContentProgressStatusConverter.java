/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.student;

import com.jlpt.feature.student.StudentContentProgress.ProgressStatus;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * Map {@link ProgressStatus} ↔ giá trị chữ thường trong DB ('learning','completed','reviewing').
 * Xem lý do tại {@link ContentProgressTypeConverter}.
 */
@Converter(autoApply = true)
public class ContentProgressStatusConverter implements AttributeConverter<ProgressStatus, String> {

    @Override
    public String convertToDatabaseColumn(ProgressStatus attribute) {
        return attribute == null ? null : attribute.getValue();
    }

    @Override
    public ProgressStatus convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }
        for (ProgressStatus s : ProgressStatus.values()) {
            if (s.getValue().equalsIgnoreCase(dbData.trim())) {
                return s;
            }
        }
        throw new IllegalArgumentException("Unknown ProgressStatus value: " + dbData);
    }
}
