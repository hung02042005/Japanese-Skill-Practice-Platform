/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.staffcontent.student.dto;

import lombok.Data;

/** Lý do tạm khoá học viên (tuỳ chọn). */
@Data
public class SuspendStudentRequest {
    private String reason;
}
