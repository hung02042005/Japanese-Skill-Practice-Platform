/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.admin.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class ChangeStaffRoleRequest {

    @NotBlank(message = "Vai trò Staff không hợp lệ")
    @Pattern(regexp = "^(staff|staff_manager)$", message = "Vai trò Staff không hợp lệ")
    private String staffRole;
}
