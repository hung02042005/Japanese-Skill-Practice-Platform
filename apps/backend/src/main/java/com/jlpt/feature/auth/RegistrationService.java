/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.auth;

import com.jlpt.feature.admin.MaintenanceModeService;
import com.jlpt.feature.auth.dto.request.RegisterRequest;
import com.jlpt.feature.auth.dto.request.ResendVerificationRequest;
import com.jlpt.feature.auth.dto.request.VerifyEmailRequest;
import com.jlpt.feature.auth.event.SendVerificationEmailEvent;
import com.jlpt.feature.student.StudentUser;
import com.jlpt.feature.student.StudentUserRepository;
import com.jlpt.feature.student.dto.response.StudentResponse;
import com.jlpt.shared.exception.BusinessException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Đăng ký tài khoản học viên và xác minh email bằng mã OTP 6 số. */
@Service
@RequiredArgsConstructor
@Slf4j
public class RegistrationService {

    private static final int EMAIL_OTP_LENGTH = 6;
    private static final long EMAIL_OTP_EXPIRY_MINUTES = 10;
    private static final int MAX_EMAIL_OTP_ATTEMPTS = 5;

    private final StudentUserRepository studentUserRepository;
    private final AuthTokenRepository authTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final ApplicationEventPublisher eventPublisher;
    private final MaintenanceModeService maintenanceModeService;
    private final StudentResponseMapper studentResponseMapper;

    private final SecureRandom secureRandom = new SecureRandom();

    // Đếm số lần nhập sai OTP xác minh email, khoá thêm brute-force 6 số (1 triệu khả năng).
    // In-memory: bị reset khi container restart, không share giữa nhiều instance nếu scale
    // ngang — reset khi có token OTP mới (resend/register). Xem trade-off tương tự trong
    // AuthenticationService.checkAccountTypeAttempts.
    private final Map<Long, Integer> emailOtpAttempts = new ConcurrentHashMap<>();

    @Transactional
    public StudentResponse register(RegisterRequest request) {
        if (maintenanceModeService.isEnabled()) {
            throw new BusinessException(503, "MAINTENANCE_MODE", "Hệ thống đang bảo trì. Vui lòng quay lại sau.");
        }
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new BusinessException(400, "PASSWORD_MISMATCH", "Mật khẩu xác nhận không khớp");
        }

        if (studentUserRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException(409, "EMAIL_EXISTS", "Email đã được sử dụng");
        }

        StudentUser newUser = StudentUser.builder()
                .email(request.getEmail())
                .fullName(request.getFullName())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .status(StudentUser.StudentStatus.PENDING)
                .build();

        StudentUser savedUser = studentUserRepository.save(newUser);

        String otpCode = generateEmailOtpCode();
        AuthToken tokenEntity = AuthToken.builder()
                .actorType(AuthToken.ActorType.STUDENT)
                .studentId(savedUser.getId())
                .tokenType(AuthToken.TokenType.EMAIL_VERIFICATION)
                .tokenValue(otpCode)
                .expiresAt(LocalDateTime.now().plusMinutes(EMAIL_OTP_EXPIRY_MINUTES))
                .build();
        authTokenRepository.save(tokenEntity);

        log.info(
                "[RegistrationService] Registration successful for {}, sending verification OTP event.",
                savedUser.getEmail());
        eventPublisher.publishEvent(new SendVerificationEmailEvent(savedUser.getEmail(), otpCode));

        return studentResponseMapper.toResponse(savedUser);
    }

    @Transactional
    public void verifyEmail(VerifyEmailRequest request) {
        StudentUser user = studentUserRepository
                .findByEmail(request.getEmail().trim().toLowerCase())
                .orElseThrow(() -> new BusinessException(400, "INVALID_OTP", "Mã xác minh không đúng"));

        // BUG-06 FIX: Kiểm tra trạng thái trước khi cho phép verify.
        // Nếu đã ACTIVE → coi như thành công (idempotent), xóa token thừa.
        // Nếu SUSPENDED/DELETED → không cho phép verify lại.
        if (user.getStatus() == StudentUser.StudentStatus.ACTIVE) {
            log.info(
                    "[RegistrationService] Email already verified for userId={}, cleaning up stale tokens.",
                    user.getId());
            authTokenRepository.deleteByStudentIdAndTokenType(user.getId(), AuthToken.TokenType.EMAIL_VERIFICATION);
            emailOtpAttempts.remove(user.getId());
            return;
        }
        if (user.getStatus() != StudentUser.StudentStatus.PENDING) {
            throw new BusinessException(
                    400, "ACCOUNT_NOT_VERIFIABLE", "Tài khoản không thể xác minh ở trạng thái hiện tại");
        }

        AuthToken tokenEntity = authTokenRepository
                .findFirstByStudentIdAndTokenTypeOrderByCreatedAtDesc(
                        user.getId(), AuthToken.TokenType.EMAIL_VERIFICATION)
                .orElseThrow(() ->
                        new BusinessException(400, "OTP_EXPIRED", "Mã xác minh đã hết hạn, vui lòng yêu cầu gửi lại"));

        if (tokenEntity.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BusinessException(400, "OTP_EXPIRED", "Mã xác minh đã hết hạn, vui lòng yêu cầu gửi lại");
        }

        // Giới hạn số lần nhập sai để chống brute-force mã 6 số (1 triệu khả năng).
        int attempts = emailOtpAttempts.merge(user.getId(), 1, Integer::sum);
        if (attempts > MAX_EMAIL_OTP_ATTEMPTS) {
            authTokenRepository.deleteByStudentIdAndTokenType(user.getId(), AuthToken.TokenType.EMAIL_VERIFICATION);
            emailOtpAttempts.remove(user.getId());
            throw new BusinessException(
                    429, "TOO_MANY_ATTEMPTS", "Nhập sai quá nhiều lần. Vui lòng yêu cầu gửi lại mã mới");
        }

        if (!tokenEntity.getTokenValue().equals(request.getOtpCode().trim())) {
            throw new BusinessException(400, "INVALID_OTP", "Mã xác minh không đúng");
        }

        user.setStatus(StudentUser.StudentStatus.ACTIVE);
        user.setEmailVerifiedAt(LocalDateTime.now());
        studentUserRepository.save(user);

        // BUG-05 FIX: Xóa TẤT CẢ EMAIL_VERIFICATION token của student này,
        // không chỉ token vừa dùng — tránh sót token cũ có thể tái sử dụng.
        authTokenRepository.deleteByStudentIdAndTokenType(user.getId(), AuthToken.TokenType.EMAIL_VERIFICATION);
        emailOtpAttempts.remove(user.getId());
        log.info("[RegistrationService] Email verified successfully for userId={}", user.getId());
    }

    @Transactional
    public void resendVerification(ResendVerificationRequest request) {
        // BUG-04 FIX: Không lộ thông tin người dùng (User Enumeration).
        // Dùng ifPresent + filter để chỉ xử lý khi email hợp lệ VÀ đang PENDING.
        // Mọi trường hợp khác đều im lặng trả về 200 OK (controller không thay đổi).
        studentUserRepository
                .findByEmail(request.getEmail().trim().toLowerCase())
                .filter(u -> u.getStatus() == StudentUser.StudentStatus.PENDING)
                .ifPresent(user -> {
                    // Rate limit: kiểm tra token cuối cùng trong DB
                    Optional<AuthToken> lastToken =
                            authTokenRepository.findFirstByStudentIdAndTokenTypeOrderByCreatedAtDesc(
                                    user.getId(), AuthToken.TokenType.EMAIL_VERIFICATION);
                    if (lastToken.isPresent()
                            && lastToken.get().getCreatedAt().plusSeconds(60).isAfter(LocalDateTime.now())) {
                        throw new BusinessException(
                                429, "TOO_MANY_REQUESTS", "Vui lòng đợi 60 giây trước khi yêu cầu gửi lại email");
                    }

                    authTokenRepository.deleteByStudentIdAndTokenType(
                            user.getId(), AuthToken.TokenType.EMAIL_VERIFICATION);
                    emailOtpAttempts.remove(user.getId());

                    String otpCode = generateEmailOtpCode();
                    AuthToken tokenEntity = AuthToken.builder()
                            .actorType(AuthToken.ActorType.STUDENT)
                            .studentId(user.getId())
                            .tokenType(AuthToken.TokenType.EMAIL_VERIFICATION)
                            .tokenValue(otpCode)
                            .expiresAt(LocalDateTime.now().plusMinutes(EMAIL_OTP_EXPIRY_MINUTES))
                            .build();
                    authTokenRepository.save(tokenEntity);

                    log.info("[RegistrationService] Resending verification OTP to: {}", user.getEmail());
                    eventPublisher.publishEvent(new SendVerificationEmailEvent(user.getEmail(), otpCode));
                });
    }

    private String generateEmailOtpCode() {
        int value = secureRandom.nextInt(1_000_000);
        return String.format("%0" + EMAIL_OTP_LENGTH + "d", value);
    }
}
