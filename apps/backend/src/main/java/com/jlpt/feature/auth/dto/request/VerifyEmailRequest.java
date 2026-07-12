/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class VerifyEmailRequest {
    @NotBlank(message = "Token là bắt buộc")
    private String token;
}
