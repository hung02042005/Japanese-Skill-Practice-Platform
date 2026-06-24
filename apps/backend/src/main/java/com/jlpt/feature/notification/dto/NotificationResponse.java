/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.notification.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class NotificationResponse {

    private Long notificationId;
    private String title;
    private String content;
    private String notificationType;
    private String channel;
    private Boolean isRead;
    private LocalDateTime readAt;
    private LocalDateTime createdAt;
}
