/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.notification.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class NotificationRuleResponse {

    private String ruleKey;
    private String description;
    private Boolean isEnabled;
    private String triggerCondition;
    private String channel;
    private String templateTitle;
    private String templateContent;
    private LocalDateTime updatedAt;
    private String updatedByAdminName;
}
