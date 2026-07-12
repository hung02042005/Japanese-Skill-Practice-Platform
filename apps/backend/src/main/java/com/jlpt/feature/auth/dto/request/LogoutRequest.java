/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LogoutRequest {
    @NotBlank(message = "Refresh token là bắt buộc")
    private String refreshToken;
}
