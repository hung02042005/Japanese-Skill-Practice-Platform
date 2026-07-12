/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.admin.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateSettingRequest {

    @NotNull(message = "settingValue không được null") @Size(max = 20000, message = "settingValue không vượt quá 20000 ký tự")
    private String settingValue;
}
