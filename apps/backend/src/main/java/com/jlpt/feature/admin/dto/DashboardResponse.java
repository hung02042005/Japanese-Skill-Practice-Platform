/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.admin.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

/** Chỉ số vận hành cho KPI row của Admin dashboard (GET /api/admin/dashboard). */
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DashboardResponse {

    private Long suspendedStudents;
    private Long newStudentsThisMonth;
    private Long openTickets;
    private Long inProgressTickets;
    private Long pendingSubmissions;
}
