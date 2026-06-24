/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.auth.service;

import com.jlpt.feature.auth.dto.LoginApiResponse;
import com.jlpt.shared.audit.AdminAuditLog;
import com.jlpt.feature.admin.entity.AdminUser;
import com.jlpt.feature.auth.entity.AuthToken;
import com.jlpt.shared.exception.BusinessException;
import com.jlpt.shared.audit.AdminAuditLogRepository;
import com.jlpt.feature.admin.repository.AdminUserRepository;
import com.jlpt.feature.auth.repository.AuthTokenRepository;
import com.jlpt.shared.security.JwtProvider;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminAuthService {

    private static final int TOKEN_BYTES = 32;
    private static final int MAX_LOGIN_ATTEMPTS = 5;
    private static final int LOCK_DURATION_MINUTES = 15;

    private final AdminUserRepository adminUserRepository;
    private final AuthTokenRepository authTokenRepository;
    private final AdminAuditLogRepository adminAuditLogRepository;
    private final JwtProvider jwtProvider;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public LoginApiResponse processAdminLogin(AdminUser admin, String rawPassword, String ip) {
        if (admin.getLockedUntil() != null && admin.getLockedUntil().isAfter(LocalDateTime.now())) {
            long minutesLeft = ChronoUnit.MINUTES.between(LocalDateTime.now(), admin.getLockedUntil()) + 1;
            audit(
                    admin,
                    "ADMIN_LOGIN_BLOCKED_LOCKED",
                    "admin_users",
                    admin.getId(),
                    ip,
                    "Account locked, email=" + admin.getEmail());
            throw new BusinessException(
                    429,
                    "TOO_MANY_REQUESTS",
                    "Tài khoản tạm thời bị khóa. Vui lòng thử lại sau " + minutesLeft + " phút");
        }

        if (admin.getStatus() == AdminUser.AdminStatus.SUSPENDED) {
            log.warn("[AdminAuthService] Login rejected — suspended email={}", admin.getEmail());
            audit(admin, "ADMIN_LOGIN_REJECTED_SUSPENDED", "admin_users", admin.getId(), ip, null);
            throw new BusinessException(
                    403, "ACCOUNT_SUSPENDED", "Tài khoản bị đình chỉ. Lý do: " + admin.getSuspendReason());
        }

        if (!passwordEncoder.matches(rawPassword, admin.getPasswordHash())) {
            int attempts = admin.getLoginAttempts() + 1;
            admin.setLoginAttempts(attempts);
            if (attempts >= MAX_LOGIN_ATTEMPTS) {
                admin.setLockedUntil(LocalDateTime.now().plusMinutes(LOCK_DURATION_MINUTES));
                adminUserRepository.save(admin);
                log.warn("[AdminAuthService] Account locked email={} ip={}", admin.getEmail(), ip);
                audit(
                        admin,
                        "ADMIN_ACCOUNT_LOCKED",
                        "admin_users",
                        admin.getId(),
                        ip,
                        "Locked after " + attempts + " failed attempts");
                throw new BusinessException(
                        429,
                        "TOO_MANY_REQUESTS",
                        "Tài khoản tạm thời bị khóa. Vui lòng thử lại sau " + LOCK_DURATION_MINUTES + " phút");
            }
            adminUserRepository.save(admin);
            log.warn("[AdminAuthService] Invalid password email={} ip={} attempts={}", admin.getEmail(), ip, attempts);
            audit(
                    admin,
                    "ADMIN_LOGIN_FAILED",
                    "admin_users",
                    admin.getId(),
                    ip,
                    "Bad credentials, attempt=" + attempts);
            throw new BusinessException(401, "INVALID_CREDENTIALS", "Email hoặc mật khẩu không đúng");
        }

        admin.setLoginAttempts(0);
        admin.setLastLoginAt(LocalDateTime.now());
        adminUserRepository.save(admin);

        log.info("[AdminAuthService] Admin login success adminId={} ip={}", admin.getId(), ip);
        audit(admin, "ADMIN_LOGIN_SUCCESS", "admin_users", admin.getId(), ip, "Login success");

        String accessToken = jwtProvider.generateAdminAccessToken(admin.getId(), admin.getEmail());
        String refreshToken = generateToken();
        authTokenRepository.save(AuthToken.builder()
                .actorType(AuthToken.ActorType.ADMIN)
                .adminId(admin.getId())
                .tokenType(AuthToken.TokenType.REFRESH)
                .tokenValue(refreshToken)
                .ipAddress(ip)
                .expiresAt(LocalDateTime.now().plusDays(7))
                .build());

        return LoginApiResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .role("ADMIN")
                .build();
    }

    private String generateToken() {
        byte[] bytes = new byte[TOKEN_BYTES];
        new SecureRandom().nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private void audit(AdminUser admin, String action, String targetTable, Long targetId, String ip, String desc) {
        AdminAuditLog log = AdminAuditLog.builder()
                .adminActor(admin)
                .action(action)
                .targetTable(targetTable)
                .targetId(targetId)
                .ipAddress(ip)
                .description(desc)
                .build();
        adminAuditLogRepository.save(log);
    }
}
