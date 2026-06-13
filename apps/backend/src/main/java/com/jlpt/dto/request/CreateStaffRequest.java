/* (c) JLPT E-Learning Platform */
package com.jlpt.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateStaffRequest {

    @NotBlank(message = "Họ tên là bắt buộc và không vượt quá 150 ký tự")
    @Size(min = 2, max = 150, message = "Họ tên là bắt buộc và không vượt quá 150 ký tự")
    private String fullName;

    @NotBlank(message = "Email không hợp lệ")
    @Email(message = "Email không hợp lệ")
    @Size(max = 255, message = "Email không hợp lệ")
    private String email;

    @NotBlank(message = "Vai trò Staff không hợp lệ")
    @Pattern(regexp = "^(staff|staff_manager)$", message = "Vai trò Staff không hợp lệ")
    private String staffRole;
}
