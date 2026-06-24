/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.analytics.controller;

import com.jlpt.shared.common.ApiResponse;
import com.jlpt.feature.analytics.dto.AnalyticsResponse;
import com.jlpt.feature.analytics.service.AnalyticsService;
import com.jlpt.shared.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/students/me/analytics")
@RequiredArgsConstructor
@PreAuthorize("hasRole('STUDENT')")
public class StudentAnalyticsController {

    private final AnalyticsService analyticsService;

    // ── UC-19: My progress analytics ─────────────────────────────────────────

    @GetMapping
    public ResponseEntity<ApiResponse<AnalyticsResponse>> getMyAnalytics(
            @AuthenticationPrincipal UserDetailsImpl principal) {
        // studentId comes from JWT (server-side) — never from request body/param
        Long studentId = principal.getStudentUser().getId();
        AnalyticsResponse result = analyticsService.getStudentProgressAnalytics(studentId);
        return ResponseEntity.ok(ApiResponse.success(result));
    }
}
