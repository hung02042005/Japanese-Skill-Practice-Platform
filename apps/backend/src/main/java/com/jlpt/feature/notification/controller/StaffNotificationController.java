/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.notification.controller;

import com.jlpt.feature.notification.service.NotificationService;
import com.jlpt.shared.common.ApiResponse;
import com.jlpt.shared.dto.request.SendNotificationRequest;
import jakarta.validation.Valid;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Staff — gui broadcast thong bao cho student (UC-30, bat dong bo tra jobId). */
@RestController
@RequestMapping("/api/staff/notifications")
@RequiredArgsConstructor
@PreAuthorize("hasRole('STAFF')")
public class StaffNotificationController {

    private final NotificationService notificationService;

    @PostMapping
    public ResponseEntity<ApiResponse<Map<String, String>>> broadcast(
            Authentication authentication, @Valid @RequestBody SendNotificationRequest req) {
        String jobId = notificationService.broadcast(authentication.getName(), req);
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(ApiResponse.success("Da gui yeu cau broadcast thong bao", Map.of("jobId", jobId)));
    }
}
