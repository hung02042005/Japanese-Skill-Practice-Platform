/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.auth.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class IssueTempPasswordRequest {

    @NotNull(message = "Mã yêu cầu là bắt buộc") private Long requestId;
}
