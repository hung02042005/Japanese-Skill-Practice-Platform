/* (c) JLPT E-Learning Platform */
package com.jlpt.service;

import com.jlpt.dto.request.AdminLoginRequest;
import com.jlpt.dto.request.Verify2FaRequest;
import com.jlpt.dto.response.AdminAuthResponse;
import com.jlpt.dto.response.AdminProfileResponse;
import com.jlpt.entity.AdminUser;
import com.jlpt.entity.AuthToken;
import com.jlpt.exception.BusinessException;
import com.jlpt.repository.AdminUserRepository;
import com.jlpt.repository.AuthTokenRepository;
import com.jlpt.security.JwtProvider;
import dev.samstevens.totp.code.DefaultCodeGenerator;
import dev.samstevens.totp.code.DefaultCodeVerifier;
import dev.samstevens.totp.time.SystemTimeProvider;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminAuthService {

    private final AdminUserRepository adminUserRepository;
    private final AuthTokenRepository authTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    @Transactional
    public AdminAuthResponse login(AdminLoginRequest request, String ip) {
        AdminUser admin = adminUserRepository
                .findByEmail(request.getEmail())
                .orElseThrow(() -> new BusinessException(401, "INVALID_CREDENTIALS", "Email hoặc mật khẩu không đúng"));

        if (admin.getLockedUntil() != null && admin.getLockedUntil().isAfter(LocalDateTime.now())) {
            throw new BusinessException(429, "TOO_MANY_REQUESTS", "Quá nhiều lần thử. Vui lòng thử lại sau.");
        }
        if (admin.getStatus() == AdminUser.AdminStatus.SUSPENDED
                || admin.getStatus() == AdminUser.AdminStatus.DELETED) {
            throw new BusinessException(403, "ACCOUNT_SUSPENDED", "Tài khoản bị vô hiệu hóa.");
        }

        if (!passwordEncoder.matches(request.getPassword(), admin.getPasswordHash())) {
            admin.setLoginAttempts(admin.getLoginAttempts() + 1);
            if (admin.getLoginAttempts() >= 5) {
                admin.setLockedUntil(LocalDateTime.now().plusMinutes(15));
                adminUserRepository.save(admin);
                log.warn("[AdminAuthService] Account locked for admin: {}", admin.getEmail());
                throw new BusinessException(429, "TOO_MANY_REQUESTS", "Tài khoản tạm khóa do sai mật khẩu 5 lần.");
            }
            adminUserRepository.save(admin);
            throw new BusinessException(401, "INVALID_CREDENTIALS", "Email hoặc mật khẩu không đúng");
        }

        // Authentication success
        admin.setLoginAttempts(0);
        admin.setLockedUntil(null);
        admin.setLastLoginAt(LocalDateTime.now());
        admin.setLastLoginIp(ip);
        adminUserRepository.save(admin);

        // Require 2FA!
        if (Boolean.TRUE.equals(admin.getTwoFactorEnabled())) {
            String tempToken = UUID.randomUUID().toString();
            AuthToken tokenEntity = AuthToken.builder()
                    .actorType(AuthToken.ActorType.ADMIN)
                    .adminId(admin.getId())
                    .tokenType(AuthToken.TokenType.TFA_TEMP)
                    .tokenValue(tempToken)
                    .expiresAt(LocalDateTime.now().plusMinutes(10))
                    .build();
            authTokenRepository.save(tokenEntity);

            return AdminAuthResponse.builder()
                    .requires2Fa(true)
                    .tempToken(tempToken)
                    .build();
        } else {
            // For security, rule says "bắt buộc 2FA - không bypass". If not enabled, we should force them to set it up!
            // But for now, if it's somehow false, we will reject login.
            throw new BusinessException(403, "2FA_REQUIRED", "Tài khoản Admin bắt buộc phải bật 2FA.");
        }
    }

    @Transactional
    public AdminAuthResponse verify2Fa(Verify2FaRequest request) {
        AuthToken tokenEntity = authTokenRepository
                .findByTokenValue(request.getTempToken())
                .orElseThrow(() -> new BusinessException(401, "INVALID_TOKEN", "Temp token không hợp lệ"));

        if (tokenEntity.getTokenType() != AuthToken.TokenType.TFA_TEMP) {
            throw new BusinessException(401, "INVALID_TOKEN", "Loại token không đúng");
        }
        if (tokenEntity.getExpiresAt().isBefore(LocalDateTime.now())) {
            authTokenRepository.delete(tokenEntity); // Or soft delete
            throw new BusinessException(401, "TOKEN_EXPIRED", "Temp token đã hết hạn");
        }

        AdminUser admin = adminUserRepository
                .findById(tokenEntity.getAdminId())
                .orElseThrow(() -> new BusinessException(404, "USER_NOT_FOUND", "Người dùng không tồn tại"));

        DefaultCodeVerifier verifier = new DefaultCodeVerifier(new DefaultCodeGenerator(), new SystemTimeProvider());
        boolean isValid = verifier.isValidCode(admin.getTwoFactorSecret(), request.getCode());

        if (!isValid) {
            throw new BusinessException(400, "INVALID_2FA_CODE", "Mã 2FA không chính xác");
        }

        // Clean up temp token
        tokenEntity.setRevokedAt(LocalDateTime.now());
        authTokenRepository.save(tokenEntity);

        String accessToken =
                jwtProvider.generateTokenFromUsername(admin.getEmail(), AuthToken.ActorType.ADMIN, 900000L);
        String refreshToken =
                jwtProvider.generateTokenFromUsername(admin.getEmail(), AuthToken.ActorType.ADMIN, 604800000L);

        AuthToken refreshTokenEntity = AuthToken.builder()
                .actorType(AuthToken.ActorType.ADMIN)
                .adminId(admin.getId())
                .tokenType(AuthToken.TokenType.REFRESH)
                .tokenValue(refreshToken)
                .expiresAt(LocalDateTime.now().plusDays(7))
                .build();
        authTokenRepository.save(refreshTokenEntity);

        log.info("[AdminAuthService] 2FA verified and login success for admin: {}", admin.getEmail());

        return AdminAuthResponse.builder()
                .requires2Fa(false)
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .admin(AdminProfileResponse.builder()
                        .adminId(admin.getId())
                        .email(admin.getEmail())
                        .fullName(admin.getFullName())
                        .createdAt(admin.getCreatedAt())
                        .build())
                .build();
    }
}
