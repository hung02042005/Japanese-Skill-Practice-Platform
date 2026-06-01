/* (c) JLPT E-Learning Platform */
package com.jlpt.controller.admin;

import com.jlpt.common.ApiResponse;
import com.jlpt.dto.request.UpdateUserRoleRequest;
import com.jlpt.dto.request.UpdateUserStatusRequest;
import com.jlpt.dto.response.AdminUserResponse;
import com.jlpt.service.AdminUserService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminUserService adminUserService;

    @GetMapping("/users")
    public ResponseEntity<ApiResponse<List<AdminUserResponse>>> listUsers() {
        return ResponseEntity.ok(ApiResponse.success(adminUserService.listAllUsers()));
    }

    @PatchMapping("/users/{userType}/{id}/status")
    public ResponseEntity<ApiResponse<AdminUserResponse>> updateStatus(
            @PathVariable String userType, @PathVariable Long id, @Valid @RequestBody UpdateUserStatusRequest request) {
        AdminUserResponse updated = adminUserService.updateUserStatus(userType, id, request);
        String msg = "BAN".equals(request.getAction()) ? "Đã khóa tài khoản" : "Đã mở khóa tài khoản";
        return ResponseEntity.ok(ApiResponse.success(msg, updated));
    }

    @PatchMapping("/users/{userType}/{id}/role")
    public ResponseEntity<ApiResponse<AdminUserResponse>> updateRole(
            @PathVariable String userType, @PathVariable Long id, @Valid @RequestBody UpdateUserRoleRequest request) {
        AdminUserResponse updated = adminUserService.updateUserRole(userType, id, request);
        return ResponseEntity.ok(ApiResponse.success("Đã cập nhật vai trò thành " + request.getNewRole(), updated));
    }
}
