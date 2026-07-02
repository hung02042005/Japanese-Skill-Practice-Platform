/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.admin.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SettingResponse {
    private final String settingKey;
    private final String settingValue;
    private final String valueType;
}
