/* (c) JLPT E-Learning Platform */
package com.jlpt.converter;

import com.jlpt.entity.Kanji;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * Map Kanji.ContentStatus ↔ giá trị lưu trong DB (chữ thường: 'published', 'draft', ...).
 *
 * <p>Lý do: dữ liệu seed (Flyway V2/V7) chèn status chữ thường khớp với {@code getValue()} của
 * enum. Nếu dùng {@code @Enumerated(EnumType.STRING)} thì Hibernate đọc/ghi theo TÊN hằng
 * ('PUBLISHED') → đọc row 'published' sẽ ném IllegalArgumentException (500). Converter đọc được cả
 * hai dạng để an toàn với dữ liệu cũ, và luôn ghi về chữ thường cho nhất quán.
 */
@Converter
public class ContentStatusConverter implements AttributeConverter<Kanji.ContentStatus, String> {

    @Override
    public String convertToDatabaseColumn(Kanji.ContentStatus attribute) {
        return attribute == null ? null : attribute.getValue();
    }

    @Override
    public Kanji.ContentStatus convertToEntityAttribute(String dbData) {
        if (dbData == null) return null;
        for (Kanji.ContentStatus e : Kanji.ContentStatus.values()) {
            if (e.getValue().equalsIgnoreCase(dbData) || e.name().equalsIgnoreCase(dbData)) return e;
        }
        throw new IllegalArgumentException("Unknown ContentStatus: " + dbData);
    }
}
