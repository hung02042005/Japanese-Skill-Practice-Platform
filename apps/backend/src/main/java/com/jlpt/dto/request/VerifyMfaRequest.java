/* (c) JLPT E-Learning Platform */
package com.jlpt.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class VerifyMfaRequest {

    @NotBlank(message = "Token xác thực không hợp lệ")
    private String mfaToken;

    @NotBlank(message = "Mã xác thực là bắt buộc")
    @Pattern(regexp = "\\d{6}", message = "Mã xác thực phải gồm 6 chữ số")
    private String totpCode;
}
