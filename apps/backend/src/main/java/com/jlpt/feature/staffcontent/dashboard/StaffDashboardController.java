/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.staffcontent.dashboard;

import com.jlpt.feature.staffcontent.dashboard.dto.StaffDashboardResponse;
import com.jlpt.shared.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Staff — bảng điều hành (số liệu tổng quan). */
@RestController
@RequestMapping("/api/staff/dashboard")
@RequiredArgsConstructor
@PreAuthorize("hasRole('STAFF')")
public class StaffDashboardController {

    private final StaffDashboardService staffDashboardService;

    @GetMapping
    public ResponseEntity<ApiResponse<StaffDashboardResponse>> getDashboard(Authentication authentication) {
        return ResponseEntity.ok(ApiResponse.success(staffDashboardService.getDashboard(authentication.getName())));
    }
}
