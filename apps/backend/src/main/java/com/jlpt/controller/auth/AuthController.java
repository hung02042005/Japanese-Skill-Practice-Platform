/* (c) JLPT E-Learning Platform */
package com.jlpt.controller.auth;

import com.jlpt.common.ApiResponse;
import com.jlpt.dto.request.*;
import com.jlpt.dto.response.*;
import com.jlpt.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

// UC-35: shared login endpoint; admin 2FA challenge and verify-mfa live here

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginApiResponse>> login(
            @Valid @RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        String ip = httpRequest.getRemoteAddr();
        LoginApiResponse response = authService.login(request, ip);
        String message = Boolean.TRUE.equals(response.getRequiresTwoFactor())
                ? "Xác thực thành công. Vui lòng nhập mã xác thực 2 yếu tố."
                : "Đăng nhập thành công";
        return ResponseEntity.ok(ApiResponse.success(message, response));
    }

    @PostMapping("/verify-mfa")
    public ResponseEntity<ApiResponse<AdminVerifyMfaResponse>> verifyMfa(
            @Valid @RequestBody VerifyMfaRequest request, HttpServletRequest httpRequest) {
        String ip = httpRequest.getRemoteAddr();
        AdminVerifyMfaResponse response = authService.verifyMfa(request, ip);
        return ResponseEntity.ok(ApiResponse.success("Xác thực 2 yếu tố thành công. Chào mừng trở lại.", response));
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<StudentResponse>> register(@Valid @RequestBody RegisterRequest request) {
        StudentResponse response = authService.register(request);
        return ResponseEntity.status(201)
                .body(ApiResponse.success(
                        "Đăng ký thành công. Vui lòng kiểm tra email để xác minh tài khoản.", response));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<RefreshTokenResponse>> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        RefreshTokenResponse response = authService.refresh(request);
        return ResponseEntity.ok(ApiResponse.success("OK", response));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(@Valid @RequestBody LogoutRequest request) {
        authService.logout(request);
        return ResponseEntity.ok(ApiResponse.success("Đăng xuất thành công.", null));
    }

    @PostMapping("/verify-email")
    public ResponseEntity<ApiResponse<Void>> verifyEmail(@Valid @RequestBody VerifyEmailRequest request) {
        authService.verifyEmail(request);
        return ResponseEntity.ok(ApiResponse.success("Xác minh email thành công. Bạn có thể đăng nhập.", null));
    }

    @PostMapping("/resend-verification")
    public ResponseEntity<ApiResponse<Void>> resendVerification(@Valid @RequestBody ResendVerificationRequest request) {
        authService.resendVerification(request);
        return ResponseEntity.ok(ApiResponse.success("Nếu email tồn tại, bạn sẽ nhận được link xác minh.", null));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        authService.forgotPassword(request);
        return ResponseEntity.ok(
                ApiResponse.success("Nếu email tồn tại, bạn sẽ nhận được link đặt lại mật khẩu.", null));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<Void>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
        return ResponseEntity.ok(ApiResponse.success("Đặt lại mật khẩu thành công.", null));
    }

    @PostMapping("/google")
    public ResponseEntity<ApiResponse<AuthResponse>> googleLogin(@Valid @RequestBody GoogleTokenRequest request) {
        AuthResponse response = authService.loginWithGoogle(request);
        return ResponseEntity.ok(ApiResponse.success("Đăng nhập bằng Google thành công.", response));
    }
}
