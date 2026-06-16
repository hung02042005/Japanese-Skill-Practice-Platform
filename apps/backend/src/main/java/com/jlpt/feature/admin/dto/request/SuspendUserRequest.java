/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.admin.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SuspendUserRequest {

    @NotBlank(message = "Lý do đình chỉ phải từ 10 đến 500 ký tự")
    @Size(min = 10, max = 500, message = "Lý do đình chỉ phải từ 10 đến 500 ký tự")
    private String reason;
}
