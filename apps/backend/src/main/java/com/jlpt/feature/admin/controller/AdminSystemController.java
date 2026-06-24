/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.admin.controller;

import com.jlpt.shared.common.ApiResponse;
import com.jlpt.feature.admin.dto.AdminDashboardSummaryResponse;
import com.jlpt.feature.admin.dto.AuditLogItemResponse;
import com.jlpt.feature.admin.dto.SystemSettingRequest;
import com.jlpt.feature.admin.dto.SystemSettingResponse;
import com.jlpt.feature.admin.service.SystemSettingService;
import com.jlpt.feature.analytics.service.AnalyticsService;
import com.jlpt.feature.notification.dto.NotificationRuleRequest;
import com.jlpt.feature.notification.dto.NotificationRuleResponse;
import com.jlpt.feature.notification.service.NotificationRuleService;
import com.jlpt.feature.admin.entity.AdminUser;
import com.jlpt.feature.admin.repository.AdminUserRepository;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * Admin-specific endpoints for system settings and notification rules.
 * Ticket/grading/broadcast support endpoints live in AdminDashboardController
 * (`/api/staff/**`, already reachable by ADMIN via hasAnyRole('STAFF','ADMIN')) — not duplicated here.
 * MUST NOT conflict with AdminController — use separate request mappings.
 */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminSystemController {

    private final SystemSettingService systemSettingService;
    private final NotificationRuleService notificationService;
    private final AdminUserRepository adminUserRepository;
    private final AnalyticsService analyticsService;

    // ── UC-39: System settings ───────────────────────────────────────────────

    @GetMapping("/settings/groups")
    public ResponseEntity<ApiResponse<List<String>>> getSettingGroups() {
        return ResponseEntity.ok(ApiResponse.success(systemSettingService.getAllGroups()));
    }

    @GetMapping("/settings/{group}")
    public ResponseEntity<ApiResponse<List<SystemSettingResponse>>> getSettingsByGroup(
            @PathVariable String group) {
        return ResponseEntity.ok(ApiResponse.success(systemSettingService.getSettingsByGroup(group)));
    }

    @GetMapping("/settings/{group}/{key}")
    public ResponseEntity<ApiResponse<SystemSettingResponse>> getSetting(
            @PathVariable String group, @PathVariable String key) {
        return ResponseEntity.ok(ApiResponse.success(systemSettingService.getSetting(group, key)));
    }

    @PutMapping("/settings/{group}/{key}")
    public ResponseEntity<ApiResponse<SystemSettingResponse>> updateSetting(
            Authentication auth,
            @PathVariable String group,
            @PathVariable String key,
            @Valid @RequestBody SystemSettingRequest req) {
        Long adminId = resolveAdminId(auth.getName());
        SystemSettingResponse result = systemSettingService.updateSetting(group, key, req, adminId);
        return ResponseEntity.ok(ApiResponse.success("Cài đặt đã được cập nhật thành công", result));
    }

    // ── UC-40: Notification rules ────────────────────────────────────────────

    @GetMapping("/notification-rules")
    public ResponseEntity<ApiResponse<List<NotificationRuleResponse>>> listRules() {
        return ResponseEntity.ok(ApiResponse.success(notificationService.listRules()));
    }

    @PostMapping("/notification-rules")
    public ResponseEntity<ApiResponse<NotificationRuleResponse>> createRule(
            Authentication auth,
            @Valid @RequestBody NotificationRuleRequest req) {
        Long adminId = resolveAdminId(auth.getName());
        NotificationRuleResponse result = notificationService.createRule(req, adminId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.<NotificationRuleResponse>builder()
                        .status(201)
                        .message("Quy tắc thông báo đã được tạo thành công")
                        .data(result)
                        .build());
    }

    @PutMapping("/notification-rules/{ruleKey}")
    public ResponseEntity<ApiResponse<NotificationRuleResponse>> updateRule(
            Authentication auth,
            @PathVariable String ruleKey,
            @Valid @RequestBody NotificationRuleRequest req) {
        Long adminId = resolveAdminId(auth.getName());
        NotificationRuleResponse result = notificationService.updateRule(ruleKey, req, adminId);
        return ResponseEntity.ok(ApiResponse.success("Quy tắc thông báo đã được cập nhật", result));
    }

    @DeleteMapping("/notification-rules/{ruleKey}")
    public ResponseEntity<ApiResponse<Void>> deleteRule(
            Authentication auth,
            @PathVariable String ruleKey) {
        Long adminId = resolveAdminId(auth.getName());
        notificationService.deleteRule(ruleKey, adminId);
        return ResponseEntity.ok(ApiResponse.success("Quy tắc đã được vô hiệu hóa (soft delete)", null));
    }

    // ── UC-36: Dashboard summary ─────────────────────────────────────────────

    @GetMapping("/dashboard/summary")
    public ResponseEntity<ApiResponse<AdminDashboardSummaryResponse>> getDashboardSummary() {
        boolean maintenance = systemSettingService.isMaintenanceMode();
        return ResponseEntity.ok(ApiResponse.success(
                analyticsService.getAdminDashboardSummary(maintenance)));
    }

    // ── UC-36: Audit log (paginated) ─────────────────────────────────────────

    @GetMapping("/audit-log")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAuditLog(
            @RequestParam(required = false) String action,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<AuditLogItemResponse> result = analyticsService.getAuditLogPaginated(action, page, size);
        return ResponseEntity.ok(ApiResponse.success(Map.of(
                "content", result.getContent(),
                "totalElements", result.getTotalElements(),
                "totalPages", result.getTotalPages())));
    }

    // ── UC-39: Settings root (alias for groups list, used when no group is selected) ──

    @GetMapping("/settings")
    public ResponseEntity<ApiResponse<List<String>>> getSettingsRoot() {
        return ResponseEntity.ok(ApiResponse.success(systemSettingService.getAllGroups()));
    }

    // ── SMTP connectivity test ───────────────────────────────────────────────

    @PostMapping("/settings/smtp/test")
    public ResponseEntity<ApiResponse<Map<String, Object>>> testSmtp() {
        try {
            SystemSettingResponse host = systemSettingService.getSetting("smtp", "smtp_host");
            SystemSettingResponse port = systemSettingService.getSetting("smtp", "smtp_port");
            boolean configured = host.getSettingValue() != null && !host.getSettingValue().isBlank()
                    && port.getSettingValue() != null && !port.getSettingValue().isBlank();
            return ResponseEntity.ok(ApiResponse.success(Map.of(
                    "status", configured ? "OK" : "NOT_CONFIGURED",
                    "message", configured
                            ? "Cấu hình SMTP hợp lệ. Vui lòng kiểm tra thực tế bằng cách gửi email thử."
                            : "Chưa cấu hình smtp_host hoặc smtp_port")));
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.success(Map.of(
                    "status", "ERROR",
                    "message", "Không thể đọc cấu hình SMTP: " + e.getMessage())));
        }
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private Long resolveAdminId(String email) {
        return adminUserRepository.findByEmail(email)
                .map(AdminUser::getId)
                .orElseThrow(() -> new com.jlpt.shared.exception.ResourceNotFoundException(
                        "Không tìm thấy tài khoản Admin: " + email));
    }
}
