/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.staffcontent.student.dto;

import lombok.Builder;
import lombok.Data;

/** Một học viên trong danh sách quản lý của Staff (read-only). */
@Data
@Builder
public class StaffStudentSummaryResponse {
    private Long studentId;
    private String fullName;
    private String email;
    private String jlptLevel;
    private String status; // active | suspended | pending
    private String subscription; // VIP | FREE (chưa có bảng subscription → mặc định FREE)
}
