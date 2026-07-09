/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.staffcontent.student.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

/** Lý do tạm khoá học viên (tuỳ chọn — null/rỗng đều hợp lệ, chỉ giới hạn độ dài). */
@Data
public class SuspendStudentRequest {

    @Size(max = 500, message = "Lý do không vượt quá 500 ký tự")
    private String reason;
}
