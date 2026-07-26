/* (c) JLPT E-Learning Platform */
package com.jlpt.shared.security;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.jlpt.shared.email.EmailService;
import com.jlpt.shared.exception.BusinessException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit tests cho OtpVerificationService.
 */
@ExtendWith(MockitoExtension.class)
class OtpVerificationServiceTest {

    @Mock
    private EmailService emailService;

    @InjectMocks
    private OtpVerificationService otpVerificationService;

    @Test
    void generateAndSend_recentCooldown_throwsBusinessException() {
        otpVerificationService.generateAndSend("test@example.com");

        BusinessException ex =
                assertThrows(BusinessException.class, () -> otpVerificationService.generateAndSend("test@example.com"));

        assertEquals(429, ex.getStatus());
        assertEquals("TOO_MANY_REQUESTS", ex.getErrorCode());
    }

    @Test
    void generateAndSend_success_sendsOtpEmail() {
        otpVerificationService.generateAndSend("new@example.com");

        verify(emailService).sendOtpEmail(eq("new@example.com"), anyString());
    }

    @Test
    void verify_nonExistentOrExpired_returnsFalse() {
        boolean result = otpVerificationService.verify("notfound@example.com", "123456");
        assertFalse(result);
    }

    @Test
    void verify_wrongCode_returnsFalse() {
        otpVerificationService.generateAndSend("user@example.com");

        boolean result = otpVerificationService.verify("user@example.com", "000000");
        assertFalse(result);
    }
}
