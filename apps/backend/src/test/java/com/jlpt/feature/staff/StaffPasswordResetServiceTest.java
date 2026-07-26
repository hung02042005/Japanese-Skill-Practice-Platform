/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.staff;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.jlpt.feature.admin.AdminAuditLogRepository;
import com.jlpt.feature.admin.AdminUser;
import com.jlpt.feature.admin.AdminUserRepository;
import com.jlpt.feature.auth.AuthToken;
import com.jlpt.feature.auth.AuthTokenRepository;
import com.jlpt.feature.auth.dto.request.IssueTempPasswordRequest;
import com.jlpt.feature.auth.dto.response.IssueTempPasswordResponse;
import com.jlpt.feature.staff.dto.request.ChangeTempPasswordRequest;
import com.jlpt.feature.staff.dto.request.StaffForgotPasswordRequest;
import com.jlpt.shared.email.EmailService;
import com.jlpt.shared.exception.BusinessException;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Unit tests cho StaffPasswordResetService.
 */
@ExtendWith(MockitoExtension.class)
class StaffPasswordResetServiceTest {

    @Mock
    private StaffUserRepository staffUserRepository;

    @Mock
    private AdminUserRepository adminUserRepository;

    @Mock
    private StaffPasswordResetRequestRepository resetRequestRepository;

    @Mock
    private AuthTokenRepository authTokenRepository;

    @Mock
    private AdminAuditLogRepository auditLogRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private StaffPasswordResetService staffPasswordResetService;

    private StaffUser staff;

    @BeforeEach
    void setUp() {
        staff = StaffUser.builder()
                .id(1L)
                .email("staff@test.com")
                .fullName("Staff Member")
                .status(StaffUser.StaffStatus.ACTIVE)
                .passwordHash("temp_hash")
                .mustChangePassword(true)
                .build();
    }

    // ── requestReset ─────────────────────────────────────────────────────────

    @Test
    void requestReset_emailNotFound_silentlyIgnores() {
        when(staffUserRepository.findByEmail("unknown@test.com")).thenReturn(Optional.empty());

        StaffForgotPasswordRequest req = new StaffForgotPasswordRequest();
        req.setEmail("unknown@test.com");

        assertDoesNotThrow(() -> staffPasswordResetService.requestReset(req, "127.0.0.1"));
        verify(resetRequestRepository, never()).save(any());
    }

    @Test
    void requestReset_activeStaff_createsResetRequest() {
        when(staffUserRepository.findByEmail("staff@test.com")).thenReturn(Optional.of(staff));
        when(resetRequestRepository.countByStaffIdAndRequestedAtAfter(eq(1L), any()))
                .thenReturn(0L);

        StaffForgotPasswordRequest req = new StaffForgotPasswordRequest();
        req.setEmail("staff@test.com");

        staffPasswordResetService.requestReset(req, "127.0.0.1");

        verify(resetRequestRepository).save(any(StaffPasswordResetRequest.class));
        verify(emailService).notifyAdminPasswordReset("Staff Member", "staff@test.com");
    }

    // ── issueTempPassword ────────────────────────────────────────────────────

    @Test
    void issueTempPassword_success_updatesStaffAndSendsEmail() {
        AdminUser admin = AdminUser.builder().id(99L).email("admin@test.com").build();
        StaffPasswordResetRequest resetReq = StaffPasswordResetRequest.builder()
                .id(10L)
                .staffId(1L)
                .status(StaffPasswordResetRequest.ResetStatus.PENDING)
                .expiresAt(LocalDateTime.now().plusHours(1))
                .build();

        when(adminUserRepository.findByEmail("admin@test.com")).thenReturn(Optional.of(admin));
        when(resetRequestRepository.findById(10L)).thenReturn(Optional.of(resetReq));
        when(staffUserRepository.findById(1L)).thenReturn(Optional.of(staff));
        when(passwordEncoder.encode(any())).thenReturn("new_temp_hash");

        IssueTempPasswordRequest req = new IssueTempPasswordRequest();
        req.setRequestId(10L);

        IssueTempPasswordResponse res = staffPasswordResetService.issueTempPassword("admin@test.com", 1L, req);

        assertNotNull(res);
        assertEquals(1L, res.getStaffId());
        assertTrue(staff.getMustChangePassword());
        verify(emailService).sendStaffTempPassword(eq("staff@test.com"), anyString());
        verify(auditLogRepository).save(any());
    }

    // ── changeTempPassword ───────────────────────────────────────────────────

    @Test
    void changeTempPassword_weakPassword_throwsBusinessException() {
        ChangeTempPasswordRequest req = new ChangeTempPasswordRequest();
        req.setNewPassword("weak");
        req.setConfirmPassword("weak");

        BusinessException ex =
                assertThrows(BusinessException.class, () -> staffPasswordResetService.changeTempPassword("token", req));
        assertEquals(422, ex.getStatus());
        assertEquals("WEAK_PASSWORD", ex.getErrorCode());
    }

    @Test
    void changeTempPassword_success_updatesPassword() {
        AuthToken token = AuthToken.builder()
                .staffId(1L)
                .expiresAt(LocalDateTime.now().plusHours(1))
                .build();
        when(authTokenRepository.findByTokenValueAndTokenType("valid_token", AuthToken.TokenType.LIMITED_SESSION))
                .thenReturn(Optional.of(token));
        when(staffUserRepository.findById(1L)).thenReturn(Optional.of(staff));
        when(passwordEncoder.matches("StrongP@ss1", "temp_hash")).thenReturn(false);
        when(passwordEncoder.encode("StrongP@ss1")).thenReturn("final_hash");

        ChangeTempPasswordRequest req = new ChangeTempPasswordRequest();
        req.setNewPassword("StrongP@ss1");
        req.setConfirmPassword("StrongP@ss1");

        staffPasswordResetService.changeTempPassword("valid_token", req);

        assertEquals("final_hash", staff.getPasswordHash());
        assertFalse(staff.getMustChangePassword());
        assertNotNull(token.getRevokedAt());
    }
}
