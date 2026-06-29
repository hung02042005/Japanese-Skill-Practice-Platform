/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.admin.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import lombok.Data;

/** Lưu nhiều setting cùng một nhóm trong 1 request (atomic). */
@Data
public class UpdateSettingsBatchRequest {

    @NotEmpty(message = "Danh sách cài đặt không được rỗng")
    @Valid
    private List<Item> settings;

    @Data
    public static class Item {

        @NotBlank(message = "settingKey không được rỗng")
        private String settingKey;

        private String settingValue;
    }
}
