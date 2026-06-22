/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.student;

import com.jlpt.feature.student.StudentContentProgress.ContentType;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * Map {@link ContentType} ↔ giá trị chữ thường trong DB ('lesson','vocabulary','kanji','kana','grammar').
 *
 * <p>Cột {@code student_content_progress.content_type} có CHECK chỉ cho chữ thường; tên enum chữ HOA.
 * Dùng {@code @Enumerated(EnumType.STRING)} sẽ khiến {@code ContentType.valueOf("kanji")} ném lỗi khi đọc
 * dữ liệu seed/đã có. Converter này (so sánh bỏ qua hoa/thường) khắc phục — đồng bộ các *TypeConverter khác.
 */
@Converter(autoApply = true)
public class ContentProgressTypeConverter implements AttributeConverter<ContentType, String> {

    @Override
    public String convertToDatabaseColumn(ContentType attribute) {
        return attribute == null ? null : attribute.getValue();
    }

    @Override
    public ContentType convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }
        for (ContentType t : ContentType.values()) {
            if (t.getValue().equalsIgnoreCase(dbData.trim())) {
                return t;
            }
        }
        throw new IllegalArgumentException("Unknown ContentType value: " + dbData);
    }
}
