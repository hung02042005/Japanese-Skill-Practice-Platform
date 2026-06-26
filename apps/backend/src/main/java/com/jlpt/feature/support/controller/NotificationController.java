/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.support.controller;

import com.jlpt.shared.common.ApiResponse;
import com.jlpt.feature.notification.dto.NotificationResponse;
import com.jlpt.feature.support.service.SupportTicketService;
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

    private final SupportTicketService supportTicketService;

    // â”€â”€ UC-30: My notifications â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> getMyNotifications(
            @AuthenticationPrincipal UserDetailsImpl principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Long studentId = principal.getStudentUser().getId();
        Page<NotificationResponse> result =
                supportTicketService.getMyNotifications(studentId, page, size);
        long unreadCount = supportTicketService.getUnreadCount(studentId);
        return ResponseEntity.ok(ApiResponse.success(Map.of(
                "content", result.getContent(),
                "totalElements", result.getTotalElements(),
                "totalPages", result.getTotalPages(),
                "unreadCount", unreadCount)));
    }

    // â”€â”€ UC-30: Mark notification read â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    @PostMapping("/{notificationId}/read")
    public ResponseEntity<ApiResponse<Void>> markRead(
            @AuthenticationPrincipal UserDetailsImpl principal,
            @PathVariable Long notificationId) {
        supportTicketService.markNotificationRead(notificationId, principal.getStudentUser().getId());
        return ResponseEntity.ok(ApiResponse.success("ÄÃ£ Ä‘Ã¡nh dáº¥u Ä‘Ã£ Ä‘á»c", null));
    }

    // â”€â”€ UC-30: Mark all notifications read â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    @PostMapping("/read-all")
    public ResponseEntity<ApiResponse<Map<String, Integer>>> markAllRead(
            @AuthenticationPrincipal UserDetailsImpl principal) {
        int updated = supportTicketService.markAllNotificationsRead(principal.getStudentUser().getId());
        return ResponseEntity.ok(ApiResponse.success("ÄÃ£ Ä‘Ã¡nh dáº¥u táº¥t cáº£ lÃ  Ä‘Ã£ Ä‘á»c", Map.of("markedCount", updated)));
    }
}

