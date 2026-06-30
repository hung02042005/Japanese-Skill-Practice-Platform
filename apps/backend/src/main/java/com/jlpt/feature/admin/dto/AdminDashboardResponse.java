/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.admin.dto;

import lombok.Builder;
import lombok.Data;

/** Gộp số liệu tổng quan (summary) + chỉ số vận hành (kpi) cho Admin dashboard trong 1 response. */
@Data
@Builder
public class AdminDashboardResponse {
    private AdminDashboardSummaryResponse summary;
    private DashboardResponse kpi;
}
