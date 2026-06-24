/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SystemSettingRequest {

    @NotBlank(message = "Giá trị setting không được để trống")
    private String value;

    @Size(max = 500, message = "Lý do thay đổi không vượt quá 500 ký tự")
    private String changeReason;
}
