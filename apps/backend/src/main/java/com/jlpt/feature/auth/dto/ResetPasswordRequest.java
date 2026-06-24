/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class ResetPasswordRequest {
    @NotBlank(message = "Token is required")
    private String token;

    @NotBlank(message = "Mật khẩu mới là bắt buộc")
    @Pattern(regexp = "^(?=.*[A-Z])(?=.*\\d).{8,}$", message = "Mật khẩu phải có ít nhất 8 ký tự, 1 chữ hoa và 1 số")
    private String newPassword;

    @NotBlank(message = "Xác nhận mật khẩu là bắt buộc")
    private String confirmPassword;
}
