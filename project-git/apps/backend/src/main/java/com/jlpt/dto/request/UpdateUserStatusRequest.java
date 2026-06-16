/* (c) JLPT E-Learning Platform */
package com.jlpt.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class UpdateUserStatusRequest {

    @NotBlank(message = "Action là bắt buộc")
    @Pattern(regexp = "BAN|ACTIVATE", message = "Action phải là BAN hoặc ACTIVATE")
    private String action;

    private String reason;
}
