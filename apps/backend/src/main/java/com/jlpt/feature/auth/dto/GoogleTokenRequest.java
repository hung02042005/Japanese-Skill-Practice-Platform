/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class GoogleTokenRequest {

    @NotBlank(message = "ID Token không được để trống")
    private String idToken;
}
