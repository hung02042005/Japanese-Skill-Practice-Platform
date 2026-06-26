/* (c) JLPT E-Learning Platform */
package com.jlpt.shared.notification.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class NotificationRuleRequest {

    @NotBlank(message = "Rule key không được để trống")
    @Pattern(regexp = "^[a-z][a-z0-9_]{2,49}$",
            message = "ruleKey chỉ chứa chữ thường, số và underscore, bắt đầu bằng chữ cái (3-50 ký tự)")
    private String ruleKey;

    @NotBlank(message = "Mô tả không được để trống")
    @Size(max = 255, message = "Mô tả không vượt quá 255 ký tự")
    private String description;

    @NotNull(message = "Trạng thái kích hoạt không được để trống")
    private Boolean isEnabled;

    @Size(max = 100, message = "Điều kiện kích hoạt không vượt quá 100 ký tự")
    private String triggerCondition;

    @Pattern(regexp = "^(in_app|email|both)$", message = "Kênh gửi không hợp lệ")
    private String channel;

    @Size(max = 255, message = "Tiêu đề template không vượt quá 255 ký tự")
    private String templateTitle;

    @Size(max = 2000, message = "Nội dung template không vượt quá 2000 ký tự")
    private String templateContent;
}
