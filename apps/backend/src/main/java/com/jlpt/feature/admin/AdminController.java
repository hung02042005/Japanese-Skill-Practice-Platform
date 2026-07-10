/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.admin;

import com.jlpt.feature.admin.dto.request.SuspendUserRequest;
import com.jlpt.feature.admin.dto.response.ActivateUserResponse;
import com.jlpt.feature.admin.dto.response.RestoreUserResponse;
import com.jlpt.feature.admin.dto.response.SoftDeleteUserResponse;
import com.jlpt.feature.admin.dto.response.SuspendUserResponse;
import com.jlpt.feature.admin.dto.response.UserSummaryResponse;
import com.jlpt.feature.auth.dto.request.IssueTempPasswordRequest;
import com.jlpt.feature.auth.dto.response.IssueTempPasswordResponse;
import com.jlpt.feature.auth.dto.response.StaffResetRequestResponse;
import com.jlpt.feature.staff.StaffPasswordResetService;
import com.jlpt.feature.staff.dto.request.ChangeStaffRoleRequest;
import com.jlpt.feature.staff.dto.request.CreateStaffRequest;
import com.jlpt.feature.staff.dto.request.UpdateStaffInfoRequest;
import com.jlpt.feature.staff.dto.response.ChangeStaffRoleResponse;
import com.jlpt.feature.staff.dto.response.CreateStaffResponse;
import com.jlpt.feature.student.dto.request.UpdateStudentRequest;
import com.jlpt.shared.common.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Validated
public class AdminController {

    private final AdminUserService adminUserService;
    private final StaffPasswordResetService staffPasswordResetService;

    // ── UC-37-01: List users ────────────────────────────────────────────────

    @GetMapping("/users")
    public ResponseEntity<ApiResponse<Map<String, Object>>> listUsers(
            @RequestParam String type,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String jlptLevel,
            @RequestParam(required = false) String staffRole,
            @RequestParam(defaultValue = "0") @Min(value = 0, message = "page phải >= 0") int page,
            @RequestParam(defaultValue = "20")
                    @Min(value = 1, message = "size phải >= 1")
                    @Max(value = 100, message = "size tối đa 100")
                    int size) {

        Page<UserSummaryResponse> result =
                adminUserService.listUsers(type, q, status, jlptLevel, staffRole, page, size);

        Map<String, Object> data = Map.of(
                "content", result.getContent(),
                "totalElements", result.getTotalElements(),
                "totalPages", result.getTotalPages());

        return ResponseEntity.ok(ApiResponse.success(data));
    }

    // ── UC-37-02: Get user detail ───────────────────────────────────────────

    @GetMapping("/users/{type}/{userId}")
    public ResponseEntity<ApiResponse<Object>> getUserDetail(@PathVariable String type, @PathVariable Long userId) {
        Object detail = adminUserService.getUserDetail(type, userId);
        return ResponseEntity.ok(ApiResponse.success(detail));
    }

    // ── UC-37-03: Create Staff ──────────────────────────────────────────────

    @PostMapping("/staff")
    public ResponseEntity<ApiResponse<CreateStaffResponse>> createStaff(
            Authentication auth, @Valid @RequestBody CreateStaffRequest request) {
        CreateStaffResponse response = adminUserService.createStaff(auth.getName(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.<CreateStaffResponse>builder()
                        .status(201)
                        .message("Tạo tài khoản Staff thành công. Email mời đã được gửi.")
                        .data(response)
                        .build());
    }

    // ── UC-37-04: Edit user ─────────────────────────────────────────────────

    @PutMapping("/users/student/{userId}")
    public ResponseEntity<ApiResponse<Object>> updateStudent(
            Authentication auth, @PathVariable Long userId, @Valid @RequestBody UpdateStudentRequest request) {
        Object updated = adminUserService.updateUser(auth.getName(), "student", userId, request);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật thông tin người dùng thành công", updated));
    }

    @PutMapping("/users/staff/{userId}")
    public ResponseEntity<ApiResponse<Object>> updateStaff(
            Authentication auth, @PathVariable Long userId, @Valid @RequestBody UpdateStaffInfoRequest request) {
        Object updated = adminUserService.updateUser(auth.getName(), "staff", userId, request);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật thông tin người dùng thành công", updated));
    }

    // ── UC-37-05: Suspend user ──────────────────────────────────────────────

    @PostMapping("/users/{type}/{userId}/suspend")
    public ResponseEntity<ApiResponse<SuspendUserResponse>> suspendUser(
            Authentication auth,
            @PathVariable String type,
            @PathVariable Long userId,
            @Valid @RequestBody SuspendUserRequest request) {
        SuspendUserResponse response = adminUserService.suspendUser(auth.getName(), type, userId, request);
        return ResponseEntity.ok(ApiResponse.success("Đã đình chỉ tài khoản thành công", response));
    }

    // ── UC-37-06: Activate user ─────────────────────────────────────────────

    @PostMapping("/users/{type}/{userId}/activate")
    public ResponseEntity<ApiResponse<ActivateUserResponse>> activateUser(
            Authentication auth, @PathVariable String type, @PathVariable Long userId) {
        ActivateUserResponse response = adminUserService.activateUser(auth.getName(), type, userId);
        return ResponseEntity.ok(ApiResponse.success("Đã kích hoạt lại tài khoản thành công", response));
    }

    // ── UC-37-07: Reset password ────────────────────────────────────────────

    @PostMapping("/users/{type}/{userId}/reset-password")
    public ResponseEntity<ApiResponse<Void>> resetPassword(
            Authentication auth, @PathVariable String type, @PathVariable Long userId) {
        adminUserService.resetPassword(auth.getName(), type, userId);
        return ResponseEntity.ok(ApiResponse.success("Email đặt lại mật khẩu đã được gửi đến người dùng", null));
    }

    // ── UC-37-08: Soft delete user ──────────────────────────────────────────

    @GetMapping("/staff/reset-requests")
    public ResponseEntity<ApiResponse<List<StaffResetRequestResponse>>> listStaffResetRequests(
            @RequestParam(required = false, defaultValue = "pending") String status) {
        List<StaffResetRequestResponse> response = staffPasswordResetService.listRequests(status);
        return ResponseEntity.ok(ApiResponse.success("OK", response));
    }

    @PostMapping("/staff/{staffId}/issue-temp-password")
    public ResponseEntity<ApiResponse<IssueTempPasswordResponse>> issueTempPassword(
            Authentication auth, @PathVariable Long staffId, @Valid @RequestBody IssueTempPasswordRequest request) {
        IssueTempPasswordResponse response =
                staffPasswordResetService.issueTempPassword(auth.getName(), staffId, request);
        return ResponseEntity.ok(
                ApiResponse.success("Đã sinh và gửi mật khẩu tạm thời đến email của nhân viên.", response));
    }

    @DeleteMapping("/users/{type}/{userId}")
    public ResponseEntity<ApiResponse<SoftDeleteUserResponse>> softDeleteUser(
            Authentication auth, @PathVariable String type, @PathVariable Long userId) {
        SoftDeleteUserResponse response = adminUserService.softDeleteUser(auth.getName(), type, userId);
        return ResponseEntity.ok(ApiResponse.success("Đã xóa tài khoản thành công (soft delete)", response));
    }

    // ── UC-37-08B: Restore deleted user ─────────────────────────────────────

    @PostMapping("/users/{type}/{userId}/restore")
    public ResponseEntity<ApiResponse<RestoreUserResponse>> restoreUser(
            Authentication auth, @PathVariable String type, @PathVariable Long userId) {
        RestoreUserResponse response = adminUserService.restoreUser(auth.getName(), type, userId);
        return ResponseEntity.ok(ApiResponse.success("Đã khôi phục tài khoản thành công", response));
    }

    // ── UC-37-09: Change Staff role ─────────────────────────────────────────

    @PutMapping("/staff/{staffId}/role")
    public ResponseEntity<ApiResponse<ChangeStaffRoleResponse>> changeStaffRole(
            Authentication auth, @PathVariable Long staffId, @Valid @RequestBody ChangeStaffRoleRequest request) {
        ChangeStaffRoleResponse response = adminUserService.changeStaffRole(auth.getName(), staffId, request);
        return ResponseEntity.ok(ApiResponse.success("Đã cập nhật vai trò Staff thành công", response));
    }
}
