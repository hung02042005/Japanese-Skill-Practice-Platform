/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.admin.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SystemSettingResponse {

    private Integer settingId;
    private String settingGroup;
    private String settingKey;
    /** "*****" if key contains sensitive keyword (password/secret/api_key/token/private_key). */
    private String settingValue;
    private String valueType;
    private Boolean isEditable;
    private String updatedByAdminName;
    private LocalDateTime updatedAt;
}
