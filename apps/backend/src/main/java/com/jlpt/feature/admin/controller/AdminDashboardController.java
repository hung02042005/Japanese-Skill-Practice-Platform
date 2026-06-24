/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.admin.controller;

import com.jlpt.shared.common.ApiResponse;
import com.jlpt.feature.admin.dto.DashboardResponse;
import com.jlpt.feature.analytics.dto.AnalyticsResponse;
import com.jlpt.feature.analytics.service.AnalyticsService;
import com.jlpt.feature.support.dto.AssignTicketRequest;
import com.jlpt.feature.support.dto.GradeResponse;
import com.jlpt.feature.support.dto.ManualGradeRequest;
import com.jlpt.feature.support.dto.TicketDetailResponse;
import com.jlpt.feature.support.dto.TicketReplyResponse;
import com.jlpt.feature.support.dto.TicketResponse;
import com.jlpt.feature.support.service.SupportTicketService;
import com.jlpt.feature.notification.dto.SendNotificationRequest;
import com.jlpt.feature.support.dto.TicketReplyRequest;
import jakarta.validation.Valid;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * Staff-facing dashboard + support + grading endpoints.
 * AdminController must NOT be modified (LESSON-001 / TASKS.md note P4.0.4).
 */
@RestController
@RequestMapping("/api/staff")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
public class AdminDashboardController {

    private final AnalyticsService analyticsService;
    private final SupportTicketService supportTicketService;

    // ── UC-38: Admin/Staff dashboard ─────────────────────────────────────────

    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<DashboardResponse>> getStaffDashboard(
            Authentication auth) {
        DashboardResponse result = analyticsService.getStaffDashboard(auth.getName());
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    // Admin dashboard — separate endpoint, restricted to ADMIN only
    @GetMapping("/admin-dashboard")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<DashboardResponse>> getAdminDashboard() {
        DashboardResponse result = analyticsService.getAdminDashboard();
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    // ── UC-19: Student analytics (Staff view) ────────────────────────────────

    @GetMapping("/students/{studentId}/analytics")
    public ResponseEntity<ApiResponse<AnalyticsResponse>> getStudentAnalytics(
            @PathVariable Long studentId) {
        AnalyticsResponse result = analyticsService.getStudentProgressAnalytics(studentId);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    // ── Support — Staff ticket management ────────────────────────────────────

    @GetMapping("/tickets")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAllTickets(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String priority,
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<TicketResponse> result =
                supportTicketService.getAllTickets(status, category, priority, q, page, size);
        return ResponseEntity.ok(ApiResponse.success(Map.of(
                "content", result.getContent(),
                "totalElements", result.getTotalElements(),
                "totalPages", result.getTotalPages())));
    }

    @GetMapping("/tickets/{ticketId}")
    public ResponseEntity<ApiResponse<TicketDetailResponse>> getTicketDetail(
            @PathVariable Long ticketId) {
        return ResponseEntity.ok(ApiResponse.success(
                supportTicketService.getStaffTicketDetail(ticketId)));
    }

    @PostMapping("/tickets/{ticketId}/reply")
    public ResponseEntity<ApiResponse<TicketReplyResponse>> replyToTicket(
            Authentication auth,
            @PathVariable Long ticketId,
            @Valid @RequestBody TicketReplyRequest req) {
        TicketReplyResponse result = supportTicketService.addStaffReply(ticketId, auth.getName(), req);
        return ResponseEntity.ok(ApiResponse.success("Gửi phản hồi ticket thành công", result));
    }

    @PostMapping("/tickets/{ticketId}/close")
    public ResponseEntity<ApiResponse<TicketResponse>> closeTicket(
            Authentication auth,
            @PathVariable Long ticketId) {
        TicketResponse result = supportTicketService.closeTicket(ticketId, auth.getName());
        return ResponseEntity.ok(ApiResponse.success("Đã đóng ticket thành công", result));
    }

    @PostMapping("/tickets/{ticketId}/assign")
    public ResponseEntity<ApiResponse<TicketResponse>> assignTicket(
            Authentication auth,
            @PathVariable Long ticketId,
            @Valid @RequestBody AssignTicketRequest req) {
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        TicketResponse result = supportTicketService.assignTicket(
                ticketId, req.getAssignToStaffId(), auth.getName(), isAdmin);
        return ResponseEntity.ok(ApiResponse.success("Đã gán ticket cho nhân viên thành công", result));
    }

    // ── Support — Staff broadcast notification ───────────────────────────────

    @PostMapping("/notifications")
    public ResponseEntity<ApiResponse<Map<String, String>>> sendNotification(
            Authentication auth,
            @Valid @RequestBody SendNotificationRequest req) {
        String jobId = supportTicketService.sendNotification(auth.getName(), req);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.<Map<String, String>>builder()
                        .status(201)
                        .message("Gửi thông báo thành công")
                        .data(Map.of("jobId", jobId))
                        .build());
    }

    // ── UC-31: Browse speaking submissions (grading queue) ──────────────────

    @GetMapping("/submissions")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAllSubmissions(
            @RequestParam(required = false) String submissionType,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<com.jlpt.feature.support.dto.SubmissionResponse> result =
                supportTicketService.getAllSubmissions(submissionType, status, page, size);
        return ResponseEntity.ok(ApiResponse.success(Map.of(
                "content", result.getContent(),
                "totalElements", result.getTotalElements(),
                "totalPages", result.getTotalPages())));
    }

    @GetMapping("/submissions/{submissionId}")
    public ResponseEntity<ApiResponse<com.jlpt.feature.support.dto.SubmissionResponse>> getSubmissionDetail(
            @PathVariable Long submissionId) {
        return ResponseEntity.ok(ApiResponse.success(supportTicketService.getSubmissionDetail(submissionId)));
    }

    // ── UC-31: Manual grade speaking ─────────────────────────────────────────

    @PostMapping("/submissions/{submissionId}/grade")
    public ResponseEntity<ApiResponse<GradeResponse>> manualGrade(
            Authentication auth,
            @PathVariable Long submissionId,
            @Valid @RequestBody ManualGradeRequest req) {
        GradeResponse result = supportTicketService.manualGrade(submissionId, auth.getName(), req);
        return ResponseEntity.ok(ApiResponse.success("Chấm điểm bài nộp nói thành công", result));
    }
}
