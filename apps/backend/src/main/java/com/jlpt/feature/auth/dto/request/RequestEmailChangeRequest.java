/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RequestEmailChangeRequest {
    @NotBlank(message = "Email mới là bắt buộc")
    @Email(message = "Email không hợp lệ")
    private String newEmail;

    @NotBlank(message = "Mật khẩu hiện tại là bắt buộc")
    private String currentPassword;
}
