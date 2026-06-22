/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.assessment;

import com.jlpt.feature.assessment.QuestionAssignment.ParentType;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * Map {@link QuestionAssignment.ParentType} ↔ chữ thường DB ('assessment','lesson').
 *
 * <p>Cột {@code question_assignments.parent_type} lưu chữ thường; tên enum chữ HOA. Trước đây
 * {@code @Enumerated(EnumType.STRING)} khiến đọc câu hỏi đã gán (vd. khi Student bắt đầu thi) ném
 * {@code No enum constant ...ParentType.assessment}. Converter này khắc phục.
 */
@Converter(autoApply = true)
public class QuestionAssignmentParentTypeConverter implements AttributeConverter<ParentType, String> {

    @Override
    public String convertToDatabaseColumn(ParentType attribute) {
        return attribute == null ? null : attribute.getValue();
    }

    @Override
    public ParentType convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }
        for (ParentType t : ParentType.values()) {
            if (t.getValue().equalsIgnoreCase(dbData.trim())) {
                return t;
            }
        }
        throw new IllegalArgumentException("Unknown QuestionAssignment.ParentType value: " + dbData);
    }
}
