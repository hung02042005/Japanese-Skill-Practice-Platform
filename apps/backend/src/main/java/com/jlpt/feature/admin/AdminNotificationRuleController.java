/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.admin;

import com.jlpt.shared.common.ApiResponse;
import com.jlpt.shared.exception.ResourceNotFoundException;
import com.jlpt.shared.notification.dto.NotificationRuleRequest;
import com.jlpt.shared.notification.dto.NotificationRuleResponse;
import com.jlpt.shared.notification.service.NotificationRuleService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Admin — cau hinh quy tac thong bao tu dong (UC-40). */
@RestController
@RequestMapping("/api/admin/notifications/rules")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminNotificationRuleController {

    private final NotificationRuleService notificationRuleService;
    private final AdminUserRepository adminUserRepository;

    @GetMapping
    public ResponseEntity<ApiResponse<List<NotificationRuleResponse>>> list() {
        return ResponseEntity.ok(ApiResponse.success(notificationRuleService.listRules()));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<NotificationRuleResponse>> create(
            Authentication authentication, @Valid @RequestBody NotificationRuleRequest req) {
        NotificationRuleResponse data = notificationRuleService.createRule(req, currentAdminId(authentication));
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created("Da tao quy tac thong bao", data));
    }

    @PutMapping("/{ruleKey}")
    public ResponseEntity<ApiResponse<NotificationRuleResponse>> update(
            Authentication authentication,
            @PathVariable String ruleKey,
            @Valid @RequestBody NotificationRuleRequest req) {
        NotificationRuleResponse data =
                notificationRuleService.updateRule(ruleKey, req, currentAdminId(authentication));
        return ResponseEntity.ok(ApiResponse.success("Da cap nhat quy tac thong bao", data));
    }

    private Long currentAdminId(Authentication authentication) {
        return adminUserRepository
                .findByEmail(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("Khong tim thay Admin"))
                .getId();
    }
}
