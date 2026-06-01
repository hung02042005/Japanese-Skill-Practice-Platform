/* (c) JLPT E-Learning Platform */
package com.jlpt.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateStaffInfoRequest {

    @Size(min = 2, max = 150, message = "Họ tên không hợp lệ")
    private String fullName;
}
