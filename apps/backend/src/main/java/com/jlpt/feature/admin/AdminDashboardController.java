/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.admin;

import com.jlpt.feature.admin.dto.AdminDashboardSummaryResponse;
import com.jlpt.feature.admin.dto.DashboardResponse;
import com.jlpt.shared.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Admin — bảng điều khiển tổng quan (UC-37). */
@RestController
@RequestMapping("/api/admin/dashboard")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminDashboardController {

    private final AdminDashboardService adminDashboardService;

    @GetMapping
    public ResponseEntity<ApiResponse<DashboardResponse>> getDashboard() {
        return ResponseEntity.ok(ApiResponse.success(adminDashboardService.getAdminDashboard()));
    }

    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<AdminDashboardSummaryResponse>> getSummary() {
        return ResponseEntity.ok(ApiResponse.success(adminDashboardService.getSummary()));
    }
}
