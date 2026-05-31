/* (c) JLPT E-Learning Platform */
package com.jlpt.dto.request;

import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import lombok.Data;

@Data
public class UpdateProfileRequest {
    @Size(max = 150, message = "Họ tên không được vượt quá 150 ký tự")
    private String fullName;

    @Size(max = 20, message = "Số điện thoại không hợp lệ")
    private String phone;

    private LocalDate dateOfBirth;

    @Size(max = 500, message = "Bio không được vượt quá 500 ký tự")
    private String bio;

    private String targetJlptLevel;

    @Size(max = 500, message = "Avatar URL quá dài")
    private String avatarUrl;
}
