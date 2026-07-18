/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.student.dto.request;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateProfileRequest {
    @Size(max = 150, message = "Họ tên không được vượt quá 150 ký tự")
    private String fullName;

    @Size(max = 20, message = "Số điện thoại không hợp lệ")
    @Pattern(regexp = "^[+]?[0-9\\s\\-()]{0,20}$", message = "Số điện thoại không hợp lệ")
    private String phone;

    @Pattern(regexp = "^(N5|N4|N3|N2|N1)$", message = "Cấp độ JLPT không hợp lệ")
    private String targetJlptLevel;

    @Size(max = 500, message = "Avatar URL quá dài")
    private String avatarUrl;
}
