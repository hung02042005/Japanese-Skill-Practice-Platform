/* (c) JLPT E-Learning Platform */
package com.jlpt.controller.auth;

import com.jlpt.common.ApiResponse;
import com.jlpt.dto.request.ChangeTempPasswordRequest;
import com.jlpt.dto.request.LoginRequest;
import com.jlpt.dto.request.StaffForgotPasswordRequest;
import com.jlpt.dto.request.StaffSetupPasswordRequest;
import com.jlpt.dto.response.LoginApiResponse;
import com.jlpt.service.AdminUserService;
import com.jlpt.service.AuthService;
import com.jlpt.service.StaffPasswordResetService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/staff/auth")
@RequiredArgsConstructor
public class StaffAuthController {

    private final AuthService authService;
    private final StaffPasswordResetService staffPasswordResetService;
    private final AdminUserService adminUserService;

    @PostMapping("/setup-password")
    public ResponseEntity<ApiResponse<Void>> setupPassword(@Valid @RequestBody StaffSetupPasswordRequest request) {
        adminUserService.setupStaffPassword(request);
        return ResponseEntity.ok(
                ApiResponse.success("Tài khoản đã được kích hoạt thành công. Vui lòng đăng nhập.", null));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(
            @Valid @RequestBody StaffForgotPasswordRequest request, HttpServletRequest httpRequest) {
        staffPasswordResetService.requestReset(request, httpRequest.getRemoteAddr());
        return ResponseEntity.ok(
                ApiResponse.success("Yêu cầu đã gửi đến quản trị viên. Vui lòng chờ email xác nhận.", null));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginApiResponse>> login(
            @Valid @RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        LoginApiResponse response = authService.loginStaff(request, httpRequest.getRemoteAddr());
        String message = Boolean.TRUE.equals(response.getRequirePasswordChange())
                ? "Đăng nhập thành công. Bạn phải đặt mật khẩu mới trước khi tiếp tục."
                : "Đăng nhập thành công";
        return ResponseEntity.ok(ApiResponse.success(message, response));
    }

    @PostMapping("/change-temp-password")
    public ResponseEntity<ApiResponse<Void>> changeTempPassword(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @Valid @RequestBody ChangeTempPasswordRequest request) {
        staffPasswordResetService.changeTempPassword(extractBearerToken(authorization), request);
        return ResponseEntity.ok(ApiResponse.success("Đặt mật khẩu mới thành công. Vui lòng đăng nhập lại.", null));
    }

    private String extractBearerToken(String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return "";
        }
        return authorization.substring(7);
    }
}
