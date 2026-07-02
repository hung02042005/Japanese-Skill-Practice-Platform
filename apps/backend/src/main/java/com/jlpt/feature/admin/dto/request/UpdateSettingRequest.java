/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.admin.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateSettingRequest {

    @NotNull(message = "settingValue không được null") private String settingValue;
}
