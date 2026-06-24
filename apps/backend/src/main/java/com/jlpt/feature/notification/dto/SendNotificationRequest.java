/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.notification.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class SendNotificationRequest {

    @NotBlank(message = "Tiêu đề thông báo không được để trống")
    @Size(max = 255, message = "Tiêu đề không vượt quá 255 ký tự")
    private String title;

    @NotBlank(message = "Nội dung thông báo không được để trống")
    private String content;

    @NotBlank(message = "Loại thông báo không được để trống")
    @Pattern(regexp = "^(news|warning|promotion|system|achievement|reminder)$",
            message = "Loại thông báo không hợp lệ")
    private String notificationType;

    @NotBlank(message = "Kênh gửi không được để trống")
    @Pattern(regexp = "^(in_app|email|both)$", message = "Kênh gửi không hợp lệ")
    private String channel;

    /** null = broadcast to all active students */
    @Pattern(regexp = "^(N5|N4|N3|N2|N1)$", message = "Cấp độ JLPT không hợp lệ")
    private String targetJlptLevel;

    /** null = send immediately */
    private LocalDateTime scheduledAt;
}
