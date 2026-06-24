/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.auth.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class IssueTempPasswordRequest {

    @NotNull(message = "Request id is required")
    private Long requestId;
}
