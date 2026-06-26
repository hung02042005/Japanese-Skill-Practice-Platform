/* (c) JLPT E-Learning Platform */
package com.jlpt.shared.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class SendNotificationRequest {

    @NotBlank(message = "Tieu de khong duoc de trong")
    @Size(max = 255, message = "Tieu de khong vuot qua 255 ky tu")
    private String title;

    @NotBlank(message = "Noi dung khong duoc de trong")
    private String content;

    @Pattern(
            regexp = "^(news|warning|promotion|system|achievement|reminder)$",
            message = "Loai thong bao khong hop le")
    private String notificationType = "system";

    @Pattern(regexp = "^(in_app|email|both)$", message = "Kenh gui khong hop le")
    private String channel = "in_app";

    @Pattern(regexp = "^(N1|N2|N3|N4|N5|ALL)$", message = "JLPT target khong hop le")
    private String targetJlptLevel = "ALL";

    private LocalDateTime scheduledAt;
}
