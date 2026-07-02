/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.notification;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/** DB lưu notification_type chữ thường ('news','system',...); enum đọc/ghi qua getValue(). */
@Converter(autoApply = true)
public class NotificationTypeConverter implements AttributeConverter<Notification.NotificationType, String> {

    @Override
    public String convertToDatabaseColumn(Notification.NotificationType attribute) {
        return attribute == null ? null : attribute.getValue();
    }

    @Override
    public Notification.NotificationType convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }
        for (Notification.NotificationType t : Notification.NotificationType.values()) {
            if (t.getValue().equalsIgnoreCase(dbData.trim())) {
                return t;
            }
        }
        throw new IllegalArgumentException("Unknown Notification.NotificationType value: " + dbData);
    }
}
