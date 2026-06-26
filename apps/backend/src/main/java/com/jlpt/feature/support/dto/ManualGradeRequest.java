/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.support.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import lombok.Data;

@Data
public class ManualGradeRequest {

    @NotNull(message = "Điểm chấm không được để trống")
    @DecimalMin(value = "0.00", message = "Điểm chấm không được nhỏ hơn 0")
    @DecimalMax(value = "100.00", message = "Điểm chấm không được lớn hơn 100")
    private BigDecimal manualScore;

    @Size(max = 2000, message = "Nhận xét không vượt quá 2000 ký tự")
    private String manualFeedback;
}
