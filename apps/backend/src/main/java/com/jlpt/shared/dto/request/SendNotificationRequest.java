/* (c) JLPT E-Learning Platform */
package com.jlpt.shared.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class SendNotificationRequest {

    @NotBlank(message = "Tiêu đề không được để trống")
    @Size(max = 255, message = "Tiêu đề không vượt quá 255 ký tự")
    private String title;

    @NotBlank(message = "Nội dung không được để trống")
    private String content;

    @Pattern(regexp = "^(news|warning|promotion|system|achievement|reminder)$", message = "Loại thông báo không hợp lệ")
    private String notificationType = "system";

    @Pattern(regexp = "^(in_app|email|both)$", message = "Kênh gửi không hợp lệ")
    private String channel = "in_app";

    @Pattern(regexp = "^(N1|N2|N3|N4|N5|ALL)$", message = "Cấp độ JLPT mục tiêu không hợp lệ")
    private String targetJlptLevel = "ALL";

    private LocalDateTime scheduledAt;
}
