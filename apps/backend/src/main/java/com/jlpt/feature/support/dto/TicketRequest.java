/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.support.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class TicketRequest {

    @NotBlank(message = "Tiêu đề không được để trống")
    @Size(max = 255, message = "Tiêu đề không vượt quá 255 ký tự")
    private String subject;

    @NotBlank(message = "Nội dung không được để trống")
    private String content;

    @Size(max = 50, message = "Danh mục không vượt quá 50 ký tự")
    private String category;

    @Pattern(regexp = "^(low|normal|high|urgent)$", message = "Mức độ ưu tiên không hợp lệ")
    private String priority;
}
