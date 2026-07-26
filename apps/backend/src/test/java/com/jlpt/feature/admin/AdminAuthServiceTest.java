/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.admin;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.jlpt.feature.auth.AuthToken;
import com.jlpt.feature.auth.AuthTokenRepository;
import com.jlpt.feature.auth.dto.response.LoginApiResponse;
import com.jlpt.shared.exception.BusinessException;
import com.jlpt.shared.security.JwtProvider;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Unit tests cho AdminAuthService — đăng nhập admin với brute-force protection.
 */
@ExtendWith(MockitoExtension.class)
class AdminAuthServiceTest {

    private static final String CORRECT_PASSWORD = "correct-pass";
    private static final String WRONG_PASSWORD = "wrong-pass";
    private static final String HASHED_PASSWORD = "$2a$10$hashed";
    private static final String IP = "127.0.0.1";

    @Mock
    private AdminUserRepository adminUserRepository;

    @Mock
    private AuthTokenRepository authTokenRepository;

    @Mock
    private AdminAuditLogRepository adminAuditLogRepository;

    @Mock
    private JwtProvider jwtProvider;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AdminAuthService adminAuthService;

    private AdminUser admin;

    @BeforeEach
    void setUp() {
        admin = AdminUser.builder()
                .id(1L)
                .email("admin@sakuji.com")
                .passwordHash(HASHED_PASSWORD)
                .status(AdminUser.AdminStatus.ACTIVE)
                .loginAttempts(0)
                .build();
    }

    // ── Account locked ───────────────────────────────────────────────────────

    @Test
    void processAdminLogin_accountLocked_throwsBusinessException() {
        admin.setLockedUntil(LocalDateTime.now().plusMinutes(10));

        BusinessException ex = assertThrows(
                BusinessException.class, () -> adminAuthService.processAdminLogin(admin, CORRECT_PASSWORD, IP));

        assertEquals(429, ex.getStatus());
        assertEquals("TOO_MANY_REQUESTS", ex.getErrorCode());
        verify(adminAuditLogRepository).save(any(AdminAuditLog.class));
        verify(passwordEncoder, never()).matches(any(), any());
    }

    // ── Account suspended ────────────────────────────────────────────────────

    @Test
    void processAdminLogin_accountSuspended_throwsBusinessException() {
        admin.setStatus(AdminUser.AdminStatus.SUSPENDED);
        admin.setSuspendReason("Vi phạm chính sách");

        BusinessException ex = assertThrows(
                BusinessException.class, () -> adminAuthService.processAdminLogin(admin, CORRECT_PASSWORD, IP));

        assertEquals(403, ex.getStatus());
        assertEquals("ACCOUNT_SUSPENDED", ex.getErrorCode());
    }

    // ── Wrong password — increments attempts ─────────────────────────────────

    @Test
    void processAdminLogin_wrongPassword_incrementsAttempts() {
        when(passwordEncoder.matches(WRONG_PASSWORD, HASHED_PASSWORD)).thenReturn(false);

        assertThrows(BusinessException.class, () -> adminAuthService.processAdminLogin(admin, WRONG_PASSWORD, IP));

        assertEquals(1, admin.getLoginAttempts());
        verify(adminUserRepository).save(admin);
        verify(adminAuditLogRepository).save(any(AdminAuditLog.class));
    }

    // ── Max attempts reached → lock ──────────────────────────────────────────

    @Test
    void processAdminLogin_maxAttemptsReached_locksAccountAndThrows() {
        admin.setLoginAttempts(4); // next attempt is the 5th → lock
        when(passwordEncoder.matches(WRONG_PASSWORD, HASHED_PASSWORD)).thenReturn(false);

        BusinessException ex = assertThrows(
                BusinessException.class, () -> adminAuthService.processAdminLogin(admin, WRONG_PASSWORD, IP));

        assertEquals(429, ex.getStatus());
        assertNotNull(admin.getLockedUntil(), "Account should be locked");
        verify(adminUserRepository).save(admin);
        verify(adminAuditLogRepository, atLeastOnce()).save(any(AdminAuditLog.class));
    }

    // ── Success login ────────────────────────────────────────────────────────

    @Test
    void processAdminLogin_correctPassword_resetsAttemptsAndReturnsToken() {
        when(passwordEncoder.matches(CORRECT_PASSWORD, HASHED_PASSWORD)).thenReturn(true);
        when(jwtProvider.generateAdminAccessToken(anyLong(), anyString())).thenReturn("access-token-abc");
        when(authTokenRepository.save(any(AuthToken.class))).thenAnswer(i -> i.getArgument(0));

        LoginApiResponse response = adminAuthService.processAdminLogin(admin, CORRECT_PASSWORD, IP);

        assertEquals(0, admin.getLoginAttempts());
        assertNotNull(admin.getLastLoginAt());
        assertEquals("access-token-abc", response.getAccessToken());
        assertEquals("ADMIN", response.getRole());
        assertNotNull(response.getRefreshToken());
        verify(adminUserRepository).save(admin);
        verify(authTokenRepository).save(any(AuthToken.class));
        verify(adminAuditLogRepository).save(any(AdminAuditLog.class));
    }
}
