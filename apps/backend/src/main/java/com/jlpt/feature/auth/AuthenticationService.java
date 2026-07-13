/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.auth;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.jlpt.feature.admin.AdminAuthService;
import com.jlpt.feature.admin.AdminUser;
import com.jlpt.feature.admin.AdminUserRepository;
import com.jlpt.feature.admin.MaintenanceModeService;
import com.jlpt.feature.auth.dto.request.GoogleTokenRequest;
import com.jlpt.feature.auth.dto.request.LoginRequest;
import com.jlpt.feature.auth.dto.request.LogoutRequest;
import com.jlpt.feature.auth.dto.request.RefreshTokenRequest;
import com.jlpt.feature.auth.dto.response.AccountTypeResponse;
import com.jlpt.feature.auth.dto.response.AuthResponse;
import com.jlpt.feature.auth.dto.response.LoginApiResponse;
import com.jlpt.feature.auth.dto.response.RefreshTokenResponse;
import com.jlpt.feature.staff.StaffUser;
import com.jlpt.feature.staff.StaffUserRepository;
import com.jlpt.feature.student.StudentUser;
import com.jlpt.feature.student.StudentUserRepository;
import com.jlpt.shared.exception.BusinessException;
import com.jlpt.shared.security.JwtProvider;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Đăng nhập/đăng xuất, refresh token, Google OAuth, và tra loại tài khoản. */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationService {

    private static final int CHECK_ACCOUNT_TYPE_LIMIT = 10;
    private static final Duration CHECK_ACCOUNT_TYPE_WINDOW = Duration.ofMinutes(1);

    private final AuthenticationManager authenticationManager;
    private final JwtProvider jwtProvider;
    private final StudentUserRepository studentUserRepository;
    private final StaffUserRepository staffUserRepository;
    private final AdminUserRepository adminUserRepository;
    private final AuthTokenRepository authTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final AdminAuthService adminAuthService;
    private final MaintenanceModeService maintenanceModeService;
    private final StudentResponseMapper studentResponseMapper;

    // Rate limit in-memory: bị reset khi container restart, và không share state giữa
    // nhiều instance nếu backend scale ngang (mỗi instance có giới hạn riêng thay vì
    // giới hạn chung). Chấp nhận được ở quy mô hiện tại (single instance); nếu scale
    // ngang hoặc cần chống brute-force nghiêm ngặt hơn, chuyển sang Redis (lưu ý:
    // backend hiện CHƯA có spring-boot-starter-data-redis/RedisTemplate dù docker-compose
    // đã chạy sẵn container redis — cần thêm dependency + config, không chỉ đổi Map).
    private final Map<String, Deque<LocalDateTime>> checkAccountTypeAttempts = new ConcurrentHashMap<>();

    @Value("${google.client-id}")
    private String googleClientId;

    @Value("${jwt.access-expiration-ms:900000}")
    private long accessExpirationMs;

    @Value("${jwt.refresh-expiration-ms:604800000}")
    private long refreshExpirationMs;

    @Transactional(readOnly = true)
    public AccountTypeResponse checkAccountType(String email) {
        return resolveAccountType(email);
    }

    @Transactional(readOnly = true)
    public AccountTypeResponse checkAccountType(String email, String ip) {
        enforceCheckAccountTypeRateLimit(ip);
        return resolveAccountType(email);
    }

    private AccountTypeResponse resolveAccountType(String email) {
        String normalizedEmail = email.trim().toLowerCase();
        String accountType = "unknown";
        if (staffUserRepository.findByEmail(normalizedEmail).isPresent()) {
            accountType = "staff";
        } else if (studentUserRepository.findByEmail(normalizedEmail).isPresent()) {
            accountType = "student";
        }
        return AccountTypeResponse.builder().accountType(accountType).build();
    }

    private void enforceCheckAccountTypeRateLimit(String ip) {
        String key = ip == null || ip.isBlank() ? "unknown" : ip;
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime threshold = now.minus(CHECK_ACCOUNT_TYPE_WINDOW);
        Deque<LocalDateTime> attempts = checkAccountTypeAttempts.computeIfAbsent(key, ignored -> new ArrayDeque<>());
        synchronized (attempts) {
            while (!attempts.isEmpty() && attempts.peekFirst().isBefore(threshold)) {
                attempts.removeFirst();
            }
            if (attempts.size() >= CHECK_ACCOUNT_TYPE_LIMIT) {
                throw new BusinessException(429, "TOO_MANY_REQUESTS", "Quá nhiều yêu cầu. Vui lòng thử lại sau.");
            }
            attempts.addLast(now);
        }
    }

    /**
     * Unified login endpoint for all roles.
     * Lookup order: admin_users → staff_users → student_users.
     */
    @Transactional
    public LoginApiResponse login(LoginRequest request, String ip) {
        Optional<AdminUser> adminOpt = adminUserRepository.findByEmail(request.getEmail());
        if (adminOpt.isPresent()) {
            return adminAuthService.processAdminLogin(adminOpt.get(), request.getPassword(), ip);
        }

        Optional<StaffUser> staffOpt = staffUserRepository.findByEmail(request.getEmail());
        if (staffOpt.isPresent()) {
            return handleStaffLogin(staffOpt.get(), request.getPassword(), ip);
        }

        Optional<StudentUser> studentOpt = studentUserRepository.findByEmail(request.getEmail());
        if (studentOpt.isPresent()) {
            return handleStudentLogin(studentOpt.get(), request.getPassword(), ip);
        }

        // BR-35-09: generic error regardless of which table was checked
        throw new BusinessException(401, "INVALID_CREDENTIALS", "Email hoặc mật khẩu không đúng");
    }

    @Transactional
    public LoginApiResponse loginStaff(LoginRequest request, String ip) {
        StaffUser staff = staffUserRepository
                .findByEmail(request.getEmail())
                .orElseThrow(() -> new BusinessException(401, "INVALID_CREDENTIALS", "Email hoặc mật khẩu không đúng"));
        return handleStaffLogin(staff, request.getPassword(), ip);
    }

    private LoginApiResponse handleStaffLogin(StaffUser staff, String rawPassword, String ip) {
        if (staff.getLockedUntil() != null && staff.getLockedUntil().isAfter(LocalDateTime.now())) {
            throw new BusinessException(429, "TOO_MANY_REQUESTS", "Quá nhiều lần thử. Vui lòng thử lại sau.");
        }
        if (staff.getStatus() == StaffUser.StaffStatus.SUSPENDED) {
            throw new BusinessException(403, "ACCOUNT_SUSPENDED", "Tài khoản bị đình chỉ: " + staff.getSuspendReason());
        }
        if (!passwordEncoder.matches(rawPassword, staff.getPasswordHash())) {
            staff.setLoginAttempts(staff.getLoginAttempts() + 1);
            if (staff.getLoginAttempts() >= 5) {
                staff.setLockedUntil(LocalDateTime.now().plusMinutes(15));
            }
            staffUserRepository.save(staff);
            throw new BusinessException(401, "INVALID_CREDENTIALS", "Email hoặc mật khẩu không đúng");
        }
        staff.setLoginAttempts(0);
        staff.setLastLoginAt(LocalDateTime.now());
        staffUserRepository.save(staff);

        if (Boolean.TRUE.equals(staff.getMustChangePassword())) {
            String limitedToken = jwtProvider.generateLimitedSessionToken(staff.getId(), staff.getEmail());
            authTokenRepository.save(AuthToken.builder()
                    .actorType(AuthToken.ActorType.STAFF)
                    .staffId(staff.getId())
                    .tokenType(AuthToken.TokenType.LIMITED_SESSION)
                    .tokenValue(limitedToken)
                    .ipAddress(ip)
                    .expiresAt(LocalDateTime.now().plusMinutes(30))
                    .build());

            log.info(
                    "[AuthenticationService] Staff temporary password login requires password change staffId={}",
                    staff.getId());
            return LoginApiResponse.builder()
                    .requirePasswordChange(true)
                    .accessToken(limitedToken)
                    .role("STAFF")
                    .staffRole(staff.getStaffRole().getValue())
                    .build();
        }

        String accessToken = jwtProvider.generateStaffAccessToken(staff.getId(), staff.getEmail());
        String refreshToken = UUID.randomUUID().toString();
        authTokenRepository.save(AuthToken.builder()
                .actorType(AuthToken.ActorType.STAFF)
                .staffId(staff.getId())
                .tokenType(AuthToken.TokenType.REFRESH)
                .tokenValue(refreshToken)
                .expiresAt(LocalDateTime.now().plusDays(7))
                .build());

        log.info("[AuthenticationService] Staff login success email={}", staff.getEmail());
        return LoginApiResponse.builder()
                .requirePasswordChange(false)
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .role("STAFF")
                .staffRole(staff.getStaffRole().getValue())
                .build();
    }

    private LoginApiResponse handleStudentLogin(StudentUser user, String rawPassword, String ip) {
        if (maintenanceModeService.isEnabled()) {
            throw new BusinessException(503, "MAINTENANCE_MODE", "Hệ thống đang bảo trì. Vui lòng quay lại sau.");
        }
        if (user.getLockedUntil() != null && user.getLockedUntil().isAfter(LocalDateTime.now())) {
            throw new BusinessException(429, "TOO_MANY_REQUESTS", "Quá nhiều lần thử. Vui lòng thử lại sau.");
        }
        if (user.getStatus() == StudentUser.StudentStatus.SUSPENDED) {
            throw new BusinessException(403, "ACCOUNT_SUSPENDED", "Tài khoản bị tạm khóa: " + user.getSuspendReason());
        }
        if (user.getStatus() == StudentUser.StudentStatus.PENDING) {
            throw new BusinessException(403, "EMAIL_NOT_VERIFIED", "Vui lòng xác minh email trước khi đăng nhập");
        }
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(user.getEmail(), rawPassword));
            user.setLoginAttempts(0);
            user.setLockedUntil(null);
            user.setLastLoginAt(LocalDateTime.now());
            studentUserRepository.save(user);

            String accessToken = jwtProvider.generateAccessToken(authentication);
            String refreshToken = jwtProvider.generateRefreshToken(authentication);
            authTokenRepository.save(AuthToken.builder()
                    .actorType(AuthToken.ActorType.STUDENT)
                    .studentId(user.getId())
                    .tokenType(AuthToken.TokenType.REFRESH)
                    .tokenValue(refreshToken)
                    .expiresAt(LocalDateTime.now().plusDays(7))
                    .build());

            log.info("[AuthenticationService] Student login success email={}", user.getEmail());
            return LoginApiResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .role("STUDENT")
                    .user(studentResponseMapper.toResponse(user))
                    .build();
        } catch (BadCredentialsException ex) {
            user.setLoginAttempts(user.getLoginAttempts() + 1);
            if (user.getLoginAttempts() >= 5) {
                user.setLockedUntil(LocalDateTime.now().plusMinutes(15));
                log.warn("[AuthenticationService] Student account locked email={}", user.getEmail());
            }
            studentUserRepository.save(user);
            throw new BusinessException(401, "INVALID_CREDENTIALS", "Email hoặc mật khẩu không đúng");
        }
    }

    @Transactional
    public RefreshTokenResponse refresh(RefreshTokenRequest request) {
        AuthToken tokenEntity = authTokenRepository
                .findByTokenValue(request.getRefreshToken())
                .orElseThrow(() -> new BusinessException(401, "INVALID_TOKEN", "Refresh token không hợp lệ"));

        if (tokenEntity.getRevokedAt() != null) {
            throw new BusinessException(401, "TOKEN_REVOKED", "Refresh token đã bị thu hồi");
        }

        if (tokenEntity.getExpiresAt().isBefore(LocalDateTime.now())) {
            authTokenRepository.delete(tokenEntity);
            throw new BusinessException(401, "TOKEN_EXPIRED", "Refresh token đã hết hạn");
        }

        String email = resolveEmailFromToken(tokenEntity);

        String newAccessToken;
        if (tokenEntity.getActorType() == AuthToken.ActorType.STAFF) {
            newAccessToken = jwtProvider.generateStaffAccessToken(tokenEntity.getStaffId(), email);
        } else if (tokenEntity.getActorType() == AuthToken.ActorType.ADMIN) {
            newAccessToken = jwtProvider.generateAdminAccessToken(tokenEntity.getAdminId(), email);
        } else {
            newAccessToken = jwtProvider.generateTokenFromUsername(email, accessExpirationMs);
        }
        String newRefreshToken = jwtProvider.generateTokenFromUsername(email, refreshExpirationMs);

        tokenEntity.setTokenValue(newRefreshToken);
        tokenEntity.setExpiresAt(LocalDateTime.now().plusDays(7));
        authTokenRepository.save(tokenEntity);

        return RefreshTokenResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .build();
    }

    @Transactional
    public void logout(LogoutRequest request) {
        // Here you would invalidate the refresh token in the database
        authTokenRepository.findByTokenValue(request.getRefreshToken()).ifPresent(authTokenRepository::delete);
    }

    @Transactional
    public AuthResponse loginWithGoogle(GoogleTokenRequest request) {
        if (maintenanceModeService.isEnabled()) {
            throw new BusinessException(503, "MAINTENANCE_MODE", "Hệ thống đang bảo trì. Vui lòng quay lại sau.");
        }
        GoogleIdToken.Payload payload = verifyGoogleToken(request.getIdToken());

        String email = payload.getEmail();
        String name = (String) payload.get("name");
        String googleSub = payload.getSubject();
        String avatarUrl = (String) payload.get("picture");

        Optional<StudentUser> existing = studentUserRepository.findByEmail(email);

        StudentUser user;
        if (existing.isPresent()) {
            user = existing.get();
            // Link Google provider if not already linked
            if (user.getOauthProvider() == null) {
                user.setOauthProvider(StudentUser.OauthProvider.GOOGLE);
                user.setOauthProviderId(googleSub);
                user.setOauthProviderEmail(email);
                user.setOauthLinkedAt(LocalDateTime.now());
                studentUserRepository.save(user);
            }
            // Check account is not suspended
            if (user.getStatus() == StudentUser.StudentStatus.SUSPENDED) {
                throw new BusinessException(
                        403, "ACCOUNT_SUSPENDED", "Tài khoản bị tạm khóa: " + user.getSuspendReason());
            }
        } else {
            // Auto-create new account from Google
            user = StudentUser.builder()
                    .email(email)
                    .fullName(name != null ? name : email)
                    .avatarUrl(avatarUrl)
                    .status(StudentUser.StudentStatus.ACTIVE)
                    .emailVerifiedAt(LocalDateTime.now())
                    .oauthProvider(StudentUser.OauthProvider.GOOGLE)
                    .oauthProviderId(googleSub)
                    .oauthProviderEmail(email)
                    .oauthLinkedAt(LocalDateTime.now())
                    .build();
            user = studentUserRepository.save(user);
            log.info("[AuthenticationService] New account created via Google for: {}", email);
        }

        user.setLastLoginAt(LocalDateTime.now());
        studentUserRepository.save(user);

        // Generate JWT tokens directly (no password involved)
        String accessToken = jwtProvider.generateTokenFromUsername(user.getEmail(), accessExpirationMs);
        String refreshToken = jwtProvider.generateTokenFromUsername(user.getEmail(), refreshExpirationMs);

        AuthToken tokenEntity = AuthToken.builder()
                .actorType(AuthToken.ActorType.STUDENT)
                .studentId(user.getId())
                .tokenType(AuthToken.TokenType.REFRESH)
                .tokenValue(refreshToken)
                .expiresAt(LocalDateTime.now().plusDays(7))
                .build();
        authTokenRepository.save(tokenEntity);

        log.info("[AuthenticationService] Google login success for: {}", email);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .student(studentResponseMapper.toResponse(user))
                .build();
    }

    private GoogleIdToken.Payload verifyGoogleToken(String idToken) {
        try {
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                            new NetHttpTransport(), new GsonFactory())
                    .setAudience(Collections.singletonList(googleClientId))
                    .build();

            GoogleIdToken googleIdToken = verifier.verify(idToken);
            if (googleIdToken == null) {
                throw new BusinessException(401, "INVALID_GOOGLE_TOKEN", "Google ID Token không hợp lệ");
            }
            return googleIdToken.getPayload();
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("[AuthenticationService] Google token verification failed: {}", e.getMessage());
            throw new BusinessException(401, "INVALID_GOOGLE_TOKEN", "Xác thực Google thất bại");
        }
    }

    private String resolveEmailFromToken(AuthToken token) {
        return switch (token.getActorType()) {
            case STUDENT -> studentUserRepository
                    .findById(token.getStudentId())
                    .orElseThrow(() -> new BusinessException(404, "USER_NOT_FOUND", "Người dùng không tồn tại"))
                    .getEmail();
            case STAFF -> staffUserRepository
                    .findById(token.getStaffId())
                    .orElseThrow(() -> new BusinessException(404, "USER_NOT_FOUND", "Người dùng không tồn tại"))
                    .getEmail();
            case ADMIN -> adminUserRepository
                    .findById(token.getAdminId())
                    .orElseThrow(() -> new BusinessException(404, "USER_NOT_FOUND", "Người dùng không tồn tại"))
                    .getEmail();
        };
    }
}
