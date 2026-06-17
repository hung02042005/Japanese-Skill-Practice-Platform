/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.admin.dto.request;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateStudentRequest {

    @Size(min = 2, max = 150, message = "Họ tên không hợp lệ")
    private String fullName;

    @Size(max = 20, message = "Số điện thoại không hợp lệ")
    @Pattern(regexp = "^[+]?[0-9\\s\\-()]{0,20}$", message = "Số điện thoại không hợp lệ")
    private String phone;

    @Pattern(regexp = "^(N5|N4|N3|N2|N1)$", message = "Cấp độ JLPT không hợp lệ")
    private String targetJlptLevel;
}
