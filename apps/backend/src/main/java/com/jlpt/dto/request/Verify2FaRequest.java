/* (c) JLPT E-Learning Platform */
package com.jlpt.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class Verify2FaRequest {
    @NotBlank(message = "Temp token không được để trống")
    private String tempToken;

    @NotBlank(message = "Code không được để trống")
    private String code;
}
