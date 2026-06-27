/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.notification;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/** DB lưu channel chữ thường ('in_app','email','both'); enum đọc/ghi qua getValue(). */
@Converter(autoApply = true)
public class ChannelConverter implements AttributeConverter<Notification.Channel, String> {

    @Override
    public String convertToDatabaseColumn(Notification.Channel attribute) {
        return attribute == null ? null : attribute.getValue();
    }

    @Override
    public Notification.Channel convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }
        for (Notification.Channel c : Notification.Channel.values()) {
            if (c.getValue().equalsIgnoreCase(dbData.trim())) {
                return c;
            }
        }
        throw new IllegalArgumentException("Unknown Notification.Channel value: " + dbData);
    }
}
