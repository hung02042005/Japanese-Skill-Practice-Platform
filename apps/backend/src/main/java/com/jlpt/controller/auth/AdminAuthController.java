/* (c) JLPT E-Learning Platform */
package com.jlpt.controller.auth;

import com.jlpt.common.ApiResponse;
import com.jlpt.dto.request.AdminLoginRequest;
import com.jlpt.dto.request.Verify2FaRequest;
import com.jlpt.dto.response.AdminAuthResponse;
import com.jlpt.service.AdminAuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/auth")
@RequiredArgsConstructor
public class AdminAuthController {

    private final AdminAuthService adminAuthService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AdminAuthResponse>> login(
            @Valid @RequestBody AdminLoginRequest request, HttpServletRequest httpRequest) {
        String ip = httpRequest.getRemoteAddr();
        AdminAuthResponse response = adminAuthService.login(request, ip);
        return ResponseEntity.ok(ApiResponse.success("Vui lòng nhập mã 2FA", response));
    }

    @PostMapping("/verify-2fa")
    public ResponseEntity<ApiResponse<AdminAuthResponse>> verify2Fa(@Valid @RequestBody Verify2FaRequest request) {
        AdminAuthResponse response = adminAuthService.verify2Fa(request);
        return ResponseEntity.ok(ApiResponse.success("Đăng nhập Admin thành công", response));
    }
}
