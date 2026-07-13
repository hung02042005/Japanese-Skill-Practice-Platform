/* (c) JLPT E-Learning Platform */
package com.jlpt.shared.security;

import com.jlpt.shared.email.EmailService;
import com.jlpt.shared.exception.BusinessException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Sinh và xác thực mã OTP 6 số. Hiện dùng cho luồng đổi email
 * ({@code StudentProfileService.requestEmailChange/confirmEmailChange}); xác minh
 * đăng ký/đăng nhập vẫn dùng link UUID token qua AuthToken (xem {@code RegistrationService}).
 *
 * Lưu trạng thái in-memory: bị reset khi container restart, không share giữa nhiều
 * instance nếu scale ngang — chấp nhận được ở quy mô hiện tại (single instance),
 * xem comment tương tự trong AuthenticationService.checkAccountTypeAttempts.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OtpVerificationService {

    private static final int OTP_LENGTH = 6;
    private static final long OTP_EXPIRY_MINUTES = 5;
    private static final long RESEND_COOLDOWN_SECONDS = 60;

    private final EmailService emailService;
    private final SecureRandom secureRandom = new SecureRandom();
    private final Map<String, OtpEntry> otpStore = new ConcurrentHashMap<>();

    public void generateAndSend(String email) {
        String normalizedEmail = email.trim().toLowerCase();
        OtpEntry existing = otpStore.get(normalizedEmail);
        if (existing != null
                && existing.createdAt().plusSeconds(RESEND_COOLDOWN_SECONDS).isAfter(LocalDateTime.now())) {
            throw new BusinessException(429, "TOO_MANY_REQUESTS", "Vui lòng đợi 60 giây trước khi yêu cầu mã mới");
        }

        String code = generateCode();
        otpStore.put(
                normalizedEmail,
                new OtpEntry(code, LocalDateTime.now(), LocalDateTime.now().plusMinutes(OTP_EXPIRY_MINUTES)));
        log.info("[OtpVerificationService] OTP generated for: {}", normalizedEmail);
        emailService.sendOtpEmail(normalizedEmail, code);
    }

    public boolean verify(String email, String code) {
        String normalizedEmail = email.trim().toLowerCase();
        OtpEntry entry = otpStore.get(normalizedEmail);
        if (entry == null || entry.expiresAt().isBefore(LocalDateTime.now())) {
            otpStore.remove(normalizedEmail);
            return false;
        }
        boolean matches = entry.code().equals(code);
        if (matches) {
            otpStore.remove(normalizedEmail);
        }
        return matches;
    }

    private String generateCode() {
        int value = secureRandom.nextInt(1_000_000);
        return String.format("%0" + OTP_LENGTH + "d", value);
    }

    private record OtpEntry(String code, LocalDateTime createdAt, LocalDateTime expiresAt) {}
}
