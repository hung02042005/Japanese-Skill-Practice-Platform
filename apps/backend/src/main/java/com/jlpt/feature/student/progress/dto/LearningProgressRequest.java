/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.student.progress.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import lombok.Data;

@Data
public class LearningProgressRequest {

    @NotBlank(message = "Loại nội dung là bắt buộc")
    private String contentType;

    @NotNull(message = "ID nội dung là bắt buộc") private Long contentId;

    @NotBlank(message = "Trạng thái là bắt buộc")
    private String status;

    @DecimalMin(value = "0", message = "Tiến độ không được nhỏ hơn 0")
    @DecimalMax(value = "100", message = "Tiến độ không được lớn hơn 100")
    private BigDecimal progressPercent;
}
