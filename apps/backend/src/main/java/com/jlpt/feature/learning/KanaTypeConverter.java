/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.learning;

import com.jlpt.feature.learning.KanaCharacter.KanaType;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * Map {@link KanaType} ↔ giá trị chữ thường trong DB ('hiragana' / 'katakana').
 *
 * <p>Cột {@code kana_characters.kana_type} có CHECK chỉ cho phép chữ thường, trong khi
 * tên enum là chữ HOA. Nếu dùng {@code @Enumerated(EnumType.STRING)} thì khi đọc,
 * Hibernate gọi {@code KanaType.valueOf("hiragana")} → ném {@code IllegalArgumentException}.
 * Converter này (so sánh bỏ qua hoa/thường) khắc phục, đồng bộ với các *TypeConverter khác.
 */
@Converter(autoApply = true)
public class KanaTypeConverter implements AttributeConverter<KanaType, String> {

    @Override
    public String convertToDatabaseColumn(KanaType attribute) {
        return attribute == null ? null : attribute.getValue();
    }

    @Override
    public KanaType convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }
        for (KanaType type : KanaType.values()) {
            if (type.getValue().equalsIgnoreCase(dbData.trim())) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown KanaType value: " + dbData);
    }
}
