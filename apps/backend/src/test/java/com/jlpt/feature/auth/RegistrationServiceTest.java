/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.auth;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.jlpt.feature.admin.MaintenanceModeService;
import com.jlpt.feature.auth.dto.request.RegisterRequest;
import com.jlpt.feature.auth.dto.request.ResendVerificationRequest;
import com.jlpt.feature.auth.dto.request.VerifyEmailRequest;
import com.jlpt.feature.auth.event.SendVerificationEmailEvent;
import com.jlpt.feature.student.StudentUser;
import com.jlpt.feature.student.StudentUserRepository;
import com.jlpt.feature.student.dto.response.StudentResponse;
import com.jlpt.shared.exception.BusinessException;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Unit tests cho RegistrationService — đăng ký + xác minh email OTP.
 */
@ExtendWith(MockitoExtension.class)
class RegistrationServiceTest {

    @Mock
    private StudentUserRepository studentUserRepository;

    @Mock
    private AuthTokenRepository authTokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private MaintenanceModeService maintenanceModeService;

    @Mock
    private StudentResponseMapper studentResponseMapper;

    @InjectMocks
    private RegistrationService registrationService;

    private StudentUser pendingUser;

    @BeforeEach
    void setUp() {
        pendingUser = StudentUser.builder()
                .id(1L)
                .email("user@test.com")
                .fullName("Test User")
                .status(StudentUser.StudentStatus.PENDING)
                .build();
    }

    // ── register ─────────────────────────────────────────────────────────────

    @Test
    void register_maintenanceMode_throwsBusinessException() {
        when(maintenanceModeService.isEnabled()).thenReturn(true);

        RegisterRequest req = buildRegisterRequest("user@test.com", "pass123", "pass123");

        BusinessException ex = assertThrows(BusinessException.class, () -> registrationService.register(req));

        assertEquals(503, ex.getStatus());
        assertEquals("MAINTENANCE_MODE", ex.getErrorCode());
    }

    @Test
    void register_passwordMismatch_throwsBusinessException() {
        when(maintenanceModeService.isEnabled()).thenReturn(false);
        RegisterRequest req = buildRegisterRequest("user@test.com", "pass123", "different");

        BusinessException ex = assertThrows(BusinessException.class, () -> registrationService.register(req));

        assertEquals(400, ex.getStatus());
        assertEquals("PASSWORD_MISMATCH", ex.getErrorCode());
    }

    @Test
    void register_emailAlreadyExists_throwsBusinessException() {
        when(maintenanceModeService.isEnabled()).thenReturn(false);
        when(studentUserRepository.existsByEmail("user@test.com")).thenReturn(true);

        RegisterRequest req = buildRegisterRequest("user@test.com", "pass123", "pass123");

        BusinessException ex = assertThrows(BusinessException.class, () -> registrationService.register(req));

        assertEquals(409, ex.getStatus());
        assertEquals("EMAIL_EXISTS", ex.getErrorCode());
    }

    @Test
    void register_success_savesUserAndPublishesEvent() {
        when(maintenanceModeService.isEnabled()).thenReturn(false);
        when(studentUserRepository.existsByEmail("user@test.com")).thenReturn(false);
        when(passwordEncoder.encode("pass123")).thenReturn("hashed");
        when(studentUserRepository.save(any(StudentUser.class))).thenReturn(pendingUser);
        when(authTokenRepository.save(any(AuthToken.class))).thenAnswer(i -> i.getArgument(0));
        when(studentResponseMapper.toResponse(any()))
                .thenReturn(StudentResponse.builder().build());

        RegisterRequest req = buildRegisterRequest("user@test.com", "pass123", "pass123");
        registrationService.register(req);

        verify(studentUserRepository).save(any(StudentUser.class));
        verify(authTokenRepository).save(any(AuthToken.class));
        verify(eventPublisher).publishEvent(any(SendVerificationEmailEvent.class));
    }

    // ── verifyEmail ──────────────────────────────────────────────────────────

    @Test
    void verifyEmail_alreadyActive_idempotentCleanup() {
        StudentUser activeUser = StudentUser.builder()
                .id(1L)
                .email("user@test.com")
                .status(StudentUser.StudentStatus.ACTIVE)
                .build();
        when(studentUserRepository.findByEmail("user@test.com")).thenReturn(Optional.of(activeUser));

        VerifyEmailRequest req = new VerifyEmailRequest();
        req.setEmail("user@test.com");
        req.setOtpCode("123456");

        // Should not throw — idempotent
        assertDoesNotThrow(() -> registrationService.verifyEmail(req));
        verify(authTokenRepository).deleteByStudentIdAndTokenType(1L, AuthToken.TokenType.EMAIL_VERIFICATION);
    }

    @Test
    void verifyEmail_userNotFound_throwsBusinessException() {
        when(studentUserRepository.findByEmail(any())).thenReturn(Optional.empty());

        VerifyEmailRequest req = new VerifyEmailRequest();
        req.setEmail("notfound@test.com");
        req.setOtpCode("123456");

        BusinessException ex = assertThrows(BusinessException.class, () -> registrationService.verifyEmail(req));

        assertEquals(400, ex.getStatus());
    }

    @Test
    void verifyEmail_accountNotPending_throwsBusinessException() {
        StudentUser suspended = StudentUser.builder()
                .id(1L)
                .email("user@test.com")
                .status(StudentUser.StudentStatus.SUSPENDED)
                .build();
        when(studentUserRepository.findByEmail("user@test.com")).thenReturn(Optional.of(suspended));

        VerifyEmailRequest req = new VerifyEmailRequest();
        req.setEmail("user@test.com");
        req.setOtpCode("123456");

        BusinessException ex = assertThrows(BusinessException.class, () -> registrationService.verifyEmail(req));

        assertEquals(400, ex.getStatus());
        assertEquals("ACCOUNT_NOT_VERIFIABLE", ex.getErrorCode());
    }

    @Test
    void verifyEmail_noTokenFound_throwsOtpExpired() {
        when(studentUserRepository.findByEmail("user@test.com")).thenReturn(Optional.of(pendingUser));
        when(authTokenRepository.findFirstByStudentIdAndTokenTypeOrderByCreatedAtDesc(
                        1L, AuthToken.TokenType.EMAIL_VERIFICATION))
                .thenReturn(Optional.empty());

        VerifyEmailRequest req = new VerifyEmailRequest();
        req.setEmail("user@test.com");
        req.setOtpCode("123456");

        BusinessException ex = assertThrows(BusinessException.class, () -> registrationService.verifyEmail(req));

        assertEquals("OTP_EXPIRED", ex.getErrorCode());
    }

    @Test
    void verifyEmail_expiredToken_throwsOtpExpired() {
        AuthToken expiredToken = AuthToken.builder()
                .tokenValue("123456")
                .expiresAt(LocalDateTime.now().minusMinutes(1))
                .build();
        when(studentUserRepository.findByEmail("user@test.com")).thenReturn(Optional.of(pendingUser));
        when(authTokenRepository.findFirstByStudentIdAndTokenTypeOrderByCreatedAtDesc(
                        1L, AuthToken.TokenType.EMAIL_VERIFICATION))
                .thenReturn(Optional.of(expiredToken));

        VerifyEmailRequest req = new VerifyEmailRequest();
        req.setEmail("user@test.com");
        req.setOtpCode("123456");

        BusinessException ex = assertThrows(BusinessException.class, () -> registrationService.verifyEmail(req));

        assertEquals("OTP_EXPIRED", ex.getErrorCode());
    }

    @Test
    void verifyEmail_wrongOtp_throwsInvalidOtp() {
        AuthToken validToken = AuthToken.builder()
                .tokenValue("999999")
                .expiresAt(LocalDateTime.now().plusMinutes(5))
                .build();
        when(studentUserRepository.findByEmail("user@test.com")).thenReturn(Optional.of(pendingUser));
        when(authTokenRepository.findFirstByStudentIdAndTokenTypeOrderByCreatedAtDesc(
                        1L, AuthToken.TokenType.EMAIL_VERIFICATION))
                .thenReturn(Optional.of(validToken));

        VerifyEmailRequest req = new VerifyEmailRequest();
        req.setEmail("user@test.com");
        req.setOtpCode("000000");

        BusinessException ex = assertThrows(BusinessException.class, () -> registrationService.verifyEmail(req));

        assertEquals("INVALID_OTP", ex.getErrorCode());
    }

    @Test
    void verifyEmail_correctOtp_activatesUserAndDeletesToken() {
        AuthToken validToken = AuthToken.builder()
                .tokenValue("123456")
                .expiresAt(LocalDateTime.now().plusMinutes(5))
                .build();
        when(studentUserRepository.findByEmail("user@test.com")).thenReturn(Optional.of(pendingUser));
        when(authTokenRepository.findFirstByStudentIdAndTokenTypeOrderByCreatedAtDesc(
                        1L, AuthToken.TokenType.EMAIL_VERIFICATION))
                .thenReturn(Optional.of(validToken));
        when(studentUserRepository.save(any())).thenReturn(pendingUser);

        VerifyEmailRequest req = new VerifyEmailRequest();
        req.setEmail("user@test.com");
        req.setOtpCode("123456");

        registrationService.verifyEmail(req);

        assertEquals(StudentUser.StudentStatus.ACTIVE, pendingUser.getStatus());
        assertNotNull(pendingUser.getEmailVerifiedAt());
        verify(authTokenRepository).deleteByStudentIdAndTokenType(1L, AuthToken.TokenType.EMAIL_VERIFICATION);
    }

    // ── resendVerification ───────────────────────────────────────────────────

    @Test
    void resendVerification_emailNotFound_silentlyDoesNothing() {
        when(studentUserRepository.findByEmail(any())).thenReturn(Optional.empty());

        ResendVerificationRequest req = new ResendVerificationRequest();
        req.setEmail("notfound@test.com");

        assertDoesNotThrow(() -> registrationService.resendVerification(req));
        verify(authTokenRepository, never()).save(any());
    }

    @Test
    void resendVerification_userAlreadyActive_silentlyDoesNothing() {
        StudentUser activeUser = StudentUser.builder()
                .id(1L)
                .email("user@test.com")
                .status(StudentUser.StudentStatus.ACTIVE)
                .build();
        when(studentUserRepository.findByEmail("user@test.com")).thenReturn(Optional.of(activeUser));

        ResendVerificationRequest req = new ResendVerificationRequest();
        req.setEmail("user@test.com");

        assertDoesNotThrow(() -> registrationService.resendVerification(req));
        verify(authTokenRepository, never()).save(any());
    }

    @Test
    void resendVerification_recentToken_throwsRateLimitException() {
        when(studentUserRepository.findByEmail("user@test.com")).thenReturn(Optional.of(pendingUser));
        AuthToken recentToken = AuthToken.builder()
                .createdAt(LocalDateTime.now().minusSeconds(30))
                .build();
        when(authTokenRepository.findFirstByStudentIdAndTokenTypeOrderByCreatedAtDesc(
                        1L, AuthToken.TokenType.EMAIL_VERIFICATION))
                .thenReturn(Optional.of(recentToken));

        ResendVerificationRequest req = new ResendVerificationRequest();
        req.setEmail("user@test.com");

        assertThrows(BusinessException.class, () -> registrationService.resendVerification(req));
    }

    @Test
    void resendVerification_expiredToken_sendsNewOtp() {
        when(studentUserRepository.findByEmail("user@test.com")).thenReturn(Optional.of(pendingUser));
        AuthToken oldToken = AuthToken.builder()
                .createdAt(LocalDateTime.now().minusSeconds(120))
                .build();
        when(authTokenRepository.findFirstByStudentIdAndTokenTypeOrderByCreatedAtDesc(
                        1L, AuthToken.TokenType.EMAIL_VERIFICATION))
                .thenReturn(Optional.of(oldToken));
        when(authTokenRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        ResendVerificationRequest req = new ResendVerificationRequest();
        req.setEmail("user@test.com");

        registrationService.resendVerification(req);

        verify(authTokenRepository).deleteByStudentIdAndTokenType(1L, AuthToken.TokenType.EMAIL_VERIFICATION);
        verify(authTokenRepository).save(any(AuthToken.class));
        verify(eventPublisher).publishEvent(any(SendVerificationEmailEvent.class));
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private RegisterRequest buildRegisterRequest(String email, String password, String confirm) {
        RegisterRequest req = new RegisterRequest();
        req.setEmail(email);
        req.setFullName("Test User");
        req.setPassword(password);
        req.setConfirmPassword(confirm);
        return req;
    }
}
