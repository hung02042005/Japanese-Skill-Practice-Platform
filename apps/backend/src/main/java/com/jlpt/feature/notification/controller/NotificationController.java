/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.notification.controller;

import com.jlpt.feature.notification.dto.NotificationResponse;
import com.jlpt.feature.notification.service.NotificationService;
import com.jlpt.shared.common.ApiResponse;
import com.jlpt.shared.security.UserDetailsImpl;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@PreAuthorize("hasRole('STUDENT')")
public class NotificationController {

    private final NotificationService notificationService;

    // ── UC-30: My notifications ───────────────────────────────────────────────

    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> getMyNotifications(
            @AuthenticationPrincipal UserDetailsImpl principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Long studentId = principal.getStudentUser().getId();
        Page<NotificationResponse> result = notificationService.getMyNotifications(studentId, page, size);
        long unreadCount = notificationService.getUnreadCount(studentId);
        return ResponseEntity.ok(ApiResponse.success(Map.of(
                "content", result.getContent(),
                "totalElements", result.getTotalElements(),
                "totalPages", result.getTotalPages(),
                "unreadCount", unreadCount)));
    }

    // ── UC-30: Mark notification read ─────────────────────────────────────────

    @PostMapping("/{notificationId}/read")
    public ResponseEntity<ApiResponse<Void>> markRead(
            @AuthenticationPrincipal UserDetailsImpl principal,
            @PathVariable Long notificationId) {
        notificationService.markNotificationRead(notificationId, principal.getStudentUser().getId());
        return ResponseEntity.ok(ApiResponse.success("Đã đánh dấu đã đọc", null));
    }

    // ── UC-30: Mark all notifications read ─────────────────────────────────────

    @PostMapping("/read-all")
    public ResponseEntity<ApiResponse<Map<String, Integer>>> markAllRead(
            @AuthenticationPrincipal UserDetailsImpl principal) {
        int updated = notificationService.markAllNotificationsRead(principal.getStudentUser().getId());
        return ResponseEntity.ok(
                ApiResponse.success("Đã đánh dấu tất cả là đã đọc", Map.of("markedCount", updated)));
    }
}
