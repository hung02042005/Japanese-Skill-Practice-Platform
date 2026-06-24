/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.analytics.controller;

import com.jlpt.shared.common.ApiResponse;
import com.jlpt.feature.analytics.dto.AdminReportResponse;
import com.jlpt.feature.analytics.dto.AnalyticsResponse;
import com.jlpt.feature.analytics.dto.CompletionRateResponse;
import com.jlpt.feature.analytics.dto.ExamAttemptResponse;
import com.jlpt.feature.analytics.dto.QuizStatsResponse;
import com.jlpt.feature.analytics.dto.StreakDetailResponse;
import com.jlpt.feature.analytics.service.AnalyticsService;
import com.jlpt.shared.security.UserDetailsImpl;
import jakarta.validation.constraints.Min;
import java.time.LocalDate;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * Analytics API — /api/analytics/**.
 *
 * <ul>
 *   <li>Student endpoints: studentId luôn lấy từ JWT (FR-ANALYTICS-06), không bao giờ từ param.
 *   <li>Staff/Admin endpoints: cần ROLE_STAFF hoặc ROLE_ADMIN.
 *   <li>Admin-only endpoints: cần ROLE_ADMIN.
 * </ul>
 *
 * <p>NOTE: Controller cũ /api/students/me/analytics không bị thay đổi (backward compatible).
 */
@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
@Slf4j
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    // ─────────────────────────────────────────────────────────────────────────
    // UC-19: Student analytics endpoints
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * GET /api/analytics/my-progress — Thống kê học tập tổng hợp của học viên đang đăng nhập.
     * studentId lấy từ JWT, không bao giờ từ request (FR-ANALYTICS-06).
     */
    @GetMapping("/my-progress")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<AnalyticsResponse>> getMyProgress(
            @AuthenticationPrincipal UserDetailsImpl principal) {
        Long studentId = principal.getStudentUser().getId();
        AnalyticsResponse result = analyticsService.getStudentProgressAnalytics(studentId);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    /**
     * GET /api/analytics/streak — Chi tiết streak học tập của học viên.
     * studentId lấy từ JWT — không cho client truyền vào (FR-ANALYTICS-06).
     */
    @GetMapping("/streak")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<StreakDetailResponse>> getStreak(
            @AuthenticationPrincipal UserDetailsImpl principal) {
        Long studentId = principal.getStudentUser().getId();
        StreakDetailResponse result = analyticsService.getStudyStreakDetail(studentId);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    /**
     * GET /api/analytics/completion?contentType=... — Tỉ lệ hoàn thành nội dung học.
     *
     * @param contentType lesson | vocabulary | kanji | kana | grammar | null = all
     */
    @GetMapping("/completion")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<CompletionRateResponse>> getCompletionRate(
            @AuthenticationPrincipal UserDetailsImpl principal,
            @RequestParam(required = false) String contentType) {
        Long studentId = principal.getStudentUser().getId();
        CompletionRateResponse result = analyticsService.getCompletionRate(studentId, contentType);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // UC-32: Staff analytics endpoints
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * GET /api/analytics/quizzes/{assessmentId}/stats — Thống kê quiz: số lần làm, điểm trung
     * bình, tỉ lệ pass, độ chính xác từng câu.
     * Tất cả tính server-side (FR-ANALYTICS-10, FR-ANALYTICS-11).
     */
    @GetMapping("/quizzes/{assessmentId}/stats")
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    public ResponseEntity<ApiResponse<QuizStatsResponse>> getQuizStats(
            @PathVariable Long assessmentId) {
        QuizStatsResponse result = analyticsService.getQuizStats(assessmentId);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    /**
     * GET /api/analytics/students/{studentId}/exam-history — Lịch sử thi của học viên (Staff/Admin
     * xem). Paginated.
     */
    @GetMapping("/students/{studentId}/exam-history")
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getStudentExamHistory(
            @PathVariable Long studentId,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) int size) {
        Page<ExamAttemptResponse> result =
                analyticsService.getStudentExamHistory(studentId, page, size);
        return ResponseEntity.ok(ApiResponse.success(Map.of(
                "content", result.getContent(),
                "totalElements", result.getTotalElements(),
                "totalPages", result.getTotalPages(),
                "page", result.getNumber(),
                "size", result.getSize())));
    }

    /**
     * GET /api/analytics/dashboard — Staff/Admin dashboard. Trả về DashboardResponse phù hợp với
     * role của caller.
     * ADMIN → full admin dashboard; STAFF → staff dashboard với tickets của mình.
     */
    @GetMapping("/dashboard")
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    public ResponseEntity<ApiResponse<Object>> getDashboard(
            org.springframework.security.core.Authentication auth) {
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        Object result = isAdmin
                ? analyticsService.getAdminDashboard()
                : analyticsService.getStaffDashboard(auth.getName());

        return ResponseEntity.ok(ApiResponse.success(result));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // UC-38: Admin-only report endpoints
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * GET /api/analytics/admin/reports?startDate=...&endDate=... — Báo cáo thống kê theo khoảng
     * thời gian. Chỉ ADMIN.
     */
    @GetMapping("/admin/reports")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<AdminReportResponse>> getAdminReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        AdminReportResponse result = analyticsService.getAdminReport(startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    /**
     * GET /api/analytics/admin/reports/export?startDate=...&endDate=...&format=csv|xlsx — Xuất báo
     * cáo. Trả về downloadUrl + expiresAt. Chỉ ADMIN.
     */
    @GetMapping("/admin/reports/export")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> exportAdminReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "csv") String format) {
        log.info("Admin report export requested: startDate={} endDate={} format={}", startDate, endDate, format);
        Map<String, Object> result = analyticsService.exportReport(startDate, endDate, format);
        return ResponseEntity.ok(ApiResponse.success("Báo cáo đã được xuất thành công", result));
    }
}
