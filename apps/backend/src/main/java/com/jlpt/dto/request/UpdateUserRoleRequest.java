/* (c) JLPT E-Learning Platform */
package com.jlpt.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class UpdateUserRoleRequest {

    @NotBlank(message = "newRole là bắt buộc")
    @Pattern(regexp = "STUDENT|STAFF", message = "Vai trò chỉ có thể là STUDENT hoặc STAFF")
    private String newRole;
}
