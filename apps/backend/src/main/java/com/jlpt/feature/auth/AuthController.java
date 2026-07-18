/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.auth;

import com.jlpt.feature.auth.dto.request.CheckAccountTypeRequest;
import com.jlpt.feature.auth.dto.request.ForgotPasswordRequest;
import com.jlpt.feature.auth.dto.request.GoogleTokenRequest;
import com.jlpt.feature.auth.dto.request.LoginRequest;
import com.jlpt.feature.auth.dto.request.LogoutRequest;
import com.jlpt.feature.auth.dto.request.RefreshTokenRequest;
import com.jlpt.feature.auth.dto.request.RegisterRequest;
import com.jlpt.feature.auth.dto.request.ResendVerificationRequest;
import com.jlpt.feature.auth.dto.request.ResetPasswordRequest;
import com.jlpt.feature.auth.dto.request.VerifyEmailRequest;
import com.jlpt.feature.auth.dto.response.AccountTypeResponse;
import com.jlpt.feature.auth.dto.response.AuthResponse;
import com.jlpt.feature.auth.dto.response.LoginApiResponse;
import com.jlpt.feature.auth.dto.response.RefreshTokenResponse;
import com.jlpt.feature.student.dto.response.StudentResponse;
import com.jlpt.shared.common.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationService authenticationService;
    private final RegistrationService registrationService;
    private final PasswordResetService passwordResetService;

    @PostMapping("/check-account-type")
    public ResponseEntity<ApiResponse<AccountTypeResponse>> checkAccountType(
            @Valid @RequestBody CheckAccountTypeRequest request, HttpServletRequest httpRequest) {
        AccountTypeResponse response =
                authenticationService.checkAccountType(request.getEmail(), httpRequest.getRemoteAddr());
        return ResponseEntity.ok(ApiResponse.success("OK", response));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginApiResponse>> login(
            @Valid @RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        String ip = httpRequest.getRemoteAddr();
        LoginApiResponse response = authenticationService.login(request, ip);
        return ResponseEntity.ok(ApiResponse.success("Đăng nhập thành công", response));
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<StudentResponse>> register(@Valid @RequestBody RegisterRequest request) {
        StudentResponse response = registrationService.register(request);
        return ResponseEntity.status(201)
                .body(ApiResponse.created(
                        "Đăng ký thành công. Vui lòng kiểm tra email để xác minh tài khoản.", response));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<RefreshTokenResponse>> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        RefreshTokenResponse response = authenticationService.refresh(request);
        return ResponseEntity.ok(ApiResponse.success("OK", response));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(@Valid @RequestBody LogoutRequest request) {
        authenticationService.logout(request);
        return ResponseEntity.ok(ApiResponse.success("Đăng xuất thành công.", null));
    }

    @PostMapping("/verify-email")
    public ResponseEntity<ApiResponse<Void>> verifyEmail(@Valid @RequestBody VerifyEmailRequest request) {
        registrationService.verifyEmail(request);
        return ResponseEntity.ok(ApiResponse.success("Xác minh email thành công. Bạn có thể đăng nhập.", null));
    }

    @PostMapping("/resend-verification")
    public ResponseEntity<ApiResponse<Void>> resendVerification(@Valid @RequestBody ResendVerificationRequest request) {
        registrationService.resendVerification(request);
        return ResponseEntity.ok(ApiResponse.success("Nếu email tồn tại, bạn sẽ nhận được link xác minh.", null));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        passwordResetService.forgotPassword(request);
        return ResponseEntity.ok(
                ApiResponse.success("Nếu email tồn tại, bạn sẽ nhận được link đặt lại mật khẩu.", null));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<Void>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        passwordResetService.resetPassword(request);
        return ResponseEntity.ok(ApiResponse.success("Đặt lại mật khẩu thành công.", null));
    }

    @PostMapping("/google")
    public ResponseEntity<ApiResponse<AuthResponse>> googleLogin(@Valid @RequestBody GoogleTokenRequest request) {
        AuthResponse response = authenticationService.loginWithGoogle(request);
        return ResponseEntity.ok(ApiResponse.success("Đăng nhập bằng Google thành công.", response));
    }
}
