/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.auth;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.jlpt.feature.auth.dto.request.ChangePasswordRequest;
import com.jlpt.feature.auth.dto.request.ForgotPasswordRequest;
import com.jlpt.feature.auth.dto.request.ResetPasswordRequest;
import com.jlpt.feature.auth.event.SendPasswordResetEmailEvent;
import com.jlpt.feature.student.StudentUser;
import com.jlpt.feature.student.StudentUserRepository;
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
 * Unit tests cho PasswordResetService.
 */
@ExtendWith(MockitoExtension.class)
class PasswordResetServiceTest {

    @Mock
    private StudentUserRepository studentUserRepository;

    @Mock
    private AuthTokenRepository authTokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private PasswordResetService passwordResetService;

    private StudentUser student;

    @BeforeEach
    void setUp() {
        student = StudentUser.builder()
                .id(1L)
                .email("student@test.com")
                .passwordHash("old_hash")
                .build();
    }

    // ── forgotPassword ────────────────────────────────────────────────────────

    @Test
    void forgotPassword_emailNotFound_silentlyIgnores() {
        when(studentUserRepository.findByEmail("unknown@test.com")).thenReturn(Optional.empty());

        ForgotPasswordRequest req = new ForgotPasswordRequest();
        req.setEmail("unknown@test.com");

        assertDoesNotThrow(() -> passwordResetService.forgotPassword(req));
        verify(authTokenRepository, never()).save(any());
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    void forgotPassword_recentTokenExists_throwsRateLimit() {
        when(studentUserRepository.findByEmail("student@test.com")).thenReturn(Optional.of(student));
        AuthToken recentToken = AuthToken.builder()
                .createdAt(LocalDateTime.now().minusSeconds(30))
                .build();
        when(authTokenRepository.findFirstByStudentIdAndTokenTypeOrderByCreatedAtDesc(
                        1L, AuthToken.TokenType.PASSWORD_RESET))
                .thenReturn(Optional.of(recentToken));

        ForgotPasswordRequest req = new ForgotPasswordRequest();
        req.setEmail("student@test.com");

        BusinessException ex = assertThrows(BusinessException.class, () -> passwordResetService.forgotPassword(req));
        assertEquals(429, ex.getStatus());
        assertEquals("TOO_MANY_REQUESTS", ex.getErrorCode());
    }

    @Test
    void forgotPassword_success_createsTokenAndPublishesEvent() {
        when(studentUserRepository.findByEmail("student@test.com")).thenReturn(Optional.of(student));
        when(authTokenRepository.findFirstByStudentIdAndTokenTypeOrderByCreatedAtDesc(
                        1L, AuthToken.TokenType.PASSWORD_RESET))
                .thenReturn(Optional.empty());

        ForgotPasswordRequest req = new ForgotPasswordRequest();
        req.setEmail("student@test.com");

        passwordResetService.forgotPassword(req);

        verify(authTokenRepository).deleteByStudentIdAndTokenType(1L, AuthToken.TokenType.PASSWORD_RESET);
        verify(authTokenRepository).save(any(AuthToken.class));
        verify(eventPublisher).publishEvent(any(SendPasswordResetEmailEvent.class));
    }

    // ── resetPassword ─────────────────────────────────────────────────────────

    @Test
    void resetPassword_mismatchPassword_throwsBusinessException() {
        ResetPasswordRequest req = new ResetPasswordRequest();
        req.setNewPassword("newpass123");
        req.setConfirmPassword("different");

        BusinessException ex = assertThrows(BusinessException.class, () -> passwordResetService.resetPassword(req));
        assertEquals(400, ex.getStatus());
        assertEquals("PASSWORD_MISMATCH", ex.getErrorCode());
    }

    @Test
    void resetPassword_invalidToken_throwsBusinessException() {
        when(authTokenRepository.findByTokenValueAndTokenType("invalid_token", AuthToken.TokenType.PASSWORD_RESET))
                .thenReturn(Optional.empty());

        ResetPasswordRequest req = new ResetPasswordRequest();
        req.setToken("invalid_token");
        req.setNewPassword("newpass123");
        req.setConfirmPassword("newpass123");

        BusinessException ex = assertThrows(BusinessException.class, () -> passwordResetService.resetPassword(req));
        assertEquals(400, ex.getStatus());
        assertEquals("INVALID_TOKEN", ex.getErrorCode());
    }

    @Test
    void resetPassword_expiredToken_throwsBusinessException() {
        AuthToken token = AuthToken.builder()
                .studentId(1L)
                .tokenValue("expired_token")
                .expiresAt(LocalDateTime.now().minusMinutes(5))
                .build();
        when(authTokenRepository.findByTokenValueAndTokenType("expired_token", AuthToken.TokenType.PASSWORD_RESET))
                .thenReturn(Optional.of(token));

        ResetPasswordRequest req = new ResetPasswordRequest();
        req.setToken("expired_token");
        req.setNewPassword("newpass123");
        req.setConfirmPassword("newpass123");

        BusinessException ex = assertThrows(BusinessException.class, () -> passwordResetService.resetPassword(req));
        assertEquals(400, ex.getStatus());
        assertEquals("TOKEN_EXPIRED", ex.getErrorCode());
    }

    @Test
    void resetPassword_userNotFound_throwsBusinessException() {
        AuthToken token = AuthToken.builder()
                .studentId(99L)
                .tokenValue("valid_token")
                .expiresAt(LocalDateTime.now().plusMinutes(30))
                .build();
        when(authTokenRepository.findByTokenValueAndTokenType("valid_token", AuthToken.TokenType.PASSWORD_RESET))
                .thenReturn(Optional.of(token));
        when(studentUserRepository.findById(99L)).thenReturn(Optional.empty());

        ResetPasswordRequest req = new ResetPasswordRequest();
        req.setToken("valid_token");
        req.setNewPassword("newpass123");
        req.setConfirmPassword("newpass123");

        BusinessException ex = assertThrows(BusinessException.class, () -> passwordResetService.resetPassword(req));
        assertEquals(404, ex.getStatus());
        assertEquals("USER_NOT_FOUND", ex.getErrorCode());
    }

    @Test
    void resetPassword_success_updatesPasswordAndDeletesToken() {
        AuthToken token = AuthToken.builder()
                .studentId(1L)
                .tokenValue("valid_token")
                .expiresAt(LocalDateTime.now().plusMinutes(30))
                .build();
        when(authTokenRepository.findByTokenValueAndTokenType("valid_token", AuthToken.TokenType.PASSWORD_RESET))
                .thenReturn(Optional.of(token));
        when(studentUserRepository.findById(1L)).thenReturn(Optional.of(student));
        when(passwordEncoder.encode("newpass123")).thenReturn("new_hash");

        ResetPasswordRequest req = new ResetPasswordRequest();
        req.setToken("valid_token");
        req.setNewPassword("newpass123");
        req.setConfirmPassword("newpass123");

        passwordResetService.resetPassword(req);

        assertEquals("new_hash", student.getPasswordHash());
        verify(studentUserRepository).save(student);
        verify(authTokenRepository).delete(token);
    }

    // ── changePassword ────────────────────────────────────────────────────────

    @Test
    void changePassword_mismatchPassword_throwsBusinessException() {
        ChangePasswordRequest req = new ChangePasswordRequest();
        req.setNewPassword("newpass123");
        req.setConfirmPassword("different");

        BusinessException ex =
                assertThrows(BusinessException.class, () -> passwordResetService.changePassword(1L, req));
        assertEquals(400, ex.getStatus());
        assertEquals("PASSWORD_MISMATCH", ex.getErrorCode());
    }

    @Test
    void changePassword_userNotFound_throwsBusinessException() {
        when(studentUserRepository.findById(99L)).thenReturn(Optional.empty());

        ChangePasswordRequest req = new ChangePasswordRequest();
        req.setCurrentPassword("oldpass");
        req.setNewPassword("newpass123");
        req.setConfirmPassword("newpass123");

        BusinessException ex =
                assertThrows(BusinessException.class, () -> passwordResetService.changePassword(99L, req));
        assertEquals(404, ex.getStatus());
        assertEquals("USER_NOT_FOUND", ex.getErrorCode());
    }

    @Test
    void changePassword_wrongCurrentPassword_throwsBusinessException() {
        when(studentUserRepository.findById(1L)).thenReturn(Optional.of(student));
        when(passwordEncoder.matches("wrongpass", "old_hash")).thenReturn(false);

        ChangePasswordRequest req = new ChangePasswordRequest();
        req.setCurrentPassword("wrongpass");
        req.setNewPassword("newpass123");
        req.setConfirmPassword("newpass123");

        BusinessException ex =
                assertThrows(BusinessException.class, () -> passwordResetService.changePassword(1L, req));
        assertEquals(400, ex.getStatus());
        assertEquals("INVALID_PASSWORD", ex.getErrorCode());
    }

    @Test
    void changePassword_success_encodesAndSavesPassword() {
        when(studentUserRepository.findById(1L)).thenReturn(Optional.of(student));
        when(passwordEncoder.matches("oldpass", "old_hash")).thenReturn(true);
        when(passwordEncoder.encode("newpass123")).thenReturn("new_hash");

        ChangePasswordRequest req = new ChangePasswordRequest();
        req.setCurrentPassword("oldpass");
        req.setNewPassword("newpass123");
        req.setConfirmPassword("newpass123");

        passwordResetService.changePassword(1L, req);

        assertEquals("new_hash", student.getPasswordHash());
        verify(studentUserRepository).save(student);
    }
}
