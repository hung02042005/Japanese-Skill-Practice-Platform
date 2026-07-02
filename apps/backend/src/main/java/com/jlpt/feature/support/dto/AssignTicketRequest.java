/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.support.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AssignTicketRequest {

    @NotNull(message = "Phải chọn nhân viên để giao ticket") private Long assignToStaffId;
}
