/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class StaffForgotPasswordRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Email is invalid")
    private String email;
}
