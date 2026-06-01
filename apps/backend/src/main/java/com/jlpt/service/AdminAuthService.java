/* (c) JLPT E-Learning Platform */
package com.jlpt.service;

import com.jlpt.dto.request.VerifyMfaRequest;
import com.jlpt.dto.response.AdminVerifyMfaResponse;
import com.jlpt.dto.response.LoginApiResponse;
import com.jlpt.entity.AdminAuditLog;
import com.jlpt.entity.AdminUser;
import com.jlpt.entity.AuthToken;
import com.jlpt.exception.BusinessException;
import com.jlpt.repository.AdminAuditLogRepository;
import com.jlpt.repository.AdminUserRepository;
import com.jlpt.repository.AuthTokenRepository;
import com.jlpt.security.JwtProvider;
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

    private static final int MFA_TOKEN_BYTES = 32;
    private static final int MFA_EXPIRY_MINUTES = 10;
    private static final int MAX_LOGIN_ATTEMPTS = 5;
    private static final int LOCK_DURATION_MINUTES = 15;
    private static final int MAX_MFA_ATTEMPTS = 5;

    private final AdminUserRepository adminUserRepository;
    private final AuthTokenRepository authTokenRepository;
    private final AdminAuditLogRepository adminAuditLogRepository;
    private final JwtProvider jwtProvider;
    private final PasswordEncoder passwordEncoder;
    private final TotpService totpService;

    /**
     * Step 1 — validate credentials and issue a 2FA temp token (UC-35 steps 3-10).
     * Always returns MFA challenge; 2FA cannot be bypassed (BR-35-01).
     */
    @Transactional
    public LoginApiResponse processAdminLogin(AdminUser admin, String rawPassword, String ip) {
        // Check account lock (BR-35-04: check BEFORE password compare → no attempt increment while locked)
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

        // Check account status
        if (admin.getStatus() == AdminUser.AdminStatus.SUSPENDED) {
            log.warn("[AdminAuthService] Login rejected — suspended email={}", admin.getEmail());
            audit(admin, "ADMIN_LOGIN_REJECTED_SUSPENDED", "admin_users", admin.getId(), ip, null);
            throw new BusinessException(
                    403, "ACCOUNT_SUSPENDED", "Tài khoản bị đình chỉ. Lý do: " + admin.getSuspendReason());
        }

        // Verify password (bcrypt cost ≥ 12 per CONSTITUTION §3.1)
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

        // Reset attempts + update login meta
        admin.setLoginAttempts(0);
        admin.setLastLoginAt(LocalDateTime.now());
        admin.setLastLoginIp(ip);
        adminUserRepository.save(admin);

        // ALWAYS bypass 2FA for admin and issue JWT directly
        log.info("[AdminAuthService] Bypassing 2FA — issuing JWT directly adminId={} ip={}", admin.getId(), ip);
        audit(admin, "ADMIN_LOGIN_DIRECT", "admin_users", admin.getId(), ip, "Direct login (2FA bypassed)");

        String accessToken = jwtProvider.generateAdminAccessToken(admin.getId(), admin.getEmail());
        String refreshToken = generateMfaToken();
        authTokenRepository.save(AuthToken.builder()
                .actorType(AuthToken.ActorType.ADMIN)
                .adminId(admin.getId())
                .tokenType(AuthToken.TokenType.REFRESH)
                .tokenValue(refreshToken)
                .ipAddress(ip)
                .expiresAt(LocalDateTime.now().plusDays(7))
                .build());

        return LoginApiResponse.builder()
                .requiresTwoFactor(false)
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .role("ADMIN")
                .build();
    }

    /**
     * Step 2 — verify TOTP code and issue final JWT tokens (UC-35 steps 14-20).
     */
    @Transactional
    public AdminVerifyMfaResponse verifyMfa(VerifyMfaRequest request, String ip) {
        // Load and validate mfaToken
        AuthToken mfaTokenEntity = authTokenRepository
                .findByTokenValueAndTokenType(request.getMfaToken(), AuthToken.TokenType.TFA_TEMP)
                .orElseThrow(() -> new BusinessException(
                        401, "INVALID_MFA_TOKEN", "Phiên xác thực đã hết hạn. Vui lòng đăng nhập lại."));

        if (mfaTokenEntity.getRevokedAt() != null) {
            throw new BusinessException(401, "INVALID_MFA_TOKEN", "Phiên xác thực đã hết hạn. Vui lòng đăng nhập lại.");
        }
        if (mfaTokenEntity.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BusinessException(401, "INVALID_MFA_TOKEN", "Phiên xác thực đã hết hạn. Vui lòng đăng nhập lại.");
        }

        // BR-35-05: max 5 wrong TOTP attempts
        if (mfaTokenEntity.getMfaAttempts() >= MAX_MFA_ATTEMPTS) {
            mfaTokenEntity.setRevokedAt(LocalDateTime.now());
            authTokenRepository.save(mfaTokenEntity);
            throw new BusinessException(
                    429, "MFA_TOO_MANY_ATTEMPTS", "Quá nhiều lần nhập sai mã xác thực. Vui lòng đăng nhập lại từ đầu.");
        }

        AdminUser admin = adminUserRepository
                .findById(mfaTokenEntity.getAdminId())
                .orElseThrow(() -> new BusinessException(
                        401, "INVALID_MFA_TOKEN", "Phiên xác thực đã hết hạn. Vui lòng đăng nhập lại."));

        // Verify TOTP (BR-35-02: decrypt AES-256, BR-35-04: ±1 window)
        if (!totpService.verifyTotp(admin.getTwoFactorSecret(), request.getTotpCode())) {
            int newAttempts = mfaTokenEntity.getMfaAttempts() + 1;
            mfaTokenEntity.setMfaAttempts(newAttempts);

            if (newAttempts >= MAX_MFA_ATTEMPTS) {
                // Revoke on 5th failure
                mfaTokenEntity.setRevokedAt(LocalDateTime.now());
                authTokenRepository.save(mfaTokenEntity);
                log.warn("[AdminAuthService] MFA token revoked after max attempts adminId={} ip={}", admin.getId(), ip);
                audit(
                        admin,
                        "ADMIN_2FA_REVOKED",
                        "auth_tokens",
                        mfaTokenEntity.getId(),
                        ip,
                        "MFA token revoked after " + newAttempts + " failed attempts");
                throw new BusinessException(
                        429,
                        "MFA_TOO_MANY_ATTEMPTS",
                        "Quá nhiều lần nhập sai mã xác thực. Vui lòng đăng nhập lại từ đầu.");
            }

            authTokenRepository.save(mfaTokenEntity);
            log.warn("[AdminAuthService] Invalid TOTP adminId={} ip={} attempts={}", admin.getId(), ip, newAttempts);
            audit(
                    admin,
                    "ADMIN_2FA_FAILED",
                    "auth_tokens",
                    mfaTokenEntity.getId(),
                    ip,
                    "Wrong TOTP attempt=" + newAttempts);
            throw new BusinessException(
                    400, "INVALID_MFA", "Mã xác thực không chính xác. Vui lòng kiểm tra lại ứng dụng Authenticator.");
        }

        // Revoke mfaToken immediately after successful verification (BR-35-03)
        mfaTokenEntity.setRevokedAt(LocalDateTime.now());
        authTokenRepository.save(mfaTokenEntity);

        // Issue JWT access token (15 min, role=ADMIN per BR-35-07)
        String accessToken = jwtProvider.generateAdminAccessToken(admin.getId(), admin.getEmail());

        // Issue refresh token (7 days)
        String refreshToken = generateMfaToken(); // URL-safe random reused for refresh
        AuthToken refreshEntity = AuthToken.builder()
                .actorType(AuthToken.ActorType.ADMIN)
                .adminId(admin.getId())
                .tokenType(AuthToken.TokenType.REFRESH)
                .tokenValue(refreshToken)
                .ipAddress(ip)
                .expiresAt(LocalDateTime.now().plusDays(7))
                .build();
        authTokenRepository.save(refreshEntity);

        log.info("[AdminAuthService] 2FA verified — JWT issued adminId={} ip={}", admin.getId(), ip);
        audit(admin, "ADMIN_2FA_SUCCESS", "admin_users", admin.getId(), ip, "Login complete via 2FA");

        return AdminVerifyMfaResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .admin(AdminVerifyMfaResponse.AdminInfo.builder()
                        .adminId(admin.getId())
                        .fullName(admin.getFullName())
                        .email(admin.getEmail())
                        .build())
                .build();
    }

    private String generateMfaToken() {
        byte[] bytes = new byte[MFA_TOKEN_BYTES];
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
