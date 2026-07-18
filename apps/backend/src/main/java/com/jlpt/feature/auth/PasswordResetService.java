/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.auth;

import com.jlpt.feature.auth.dto.request.ChangePasswordRequest;
import com.jlpt.feature.auth.dto.request.ForgotPasswordRequest;
import com.jlpt.feature.auth.dto.request.ResetPasswordRequest;
import com.jlpt.feature.auth.event.SendPasswordResetEmailEvent;
import com.jlpt.feature.student.StudentUser;
import com.jlpt.feature.student.StudentUserRepository;
import com.jlpt.shared.exception.BusinessException;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Quên mật khẩu (link qua email), đặt lại mật khẩu, và đổi mật khẩu khi đã đăng nhập. */
@Service
@RequiredArgsConstructor
@Slf4j
public class PasswordResetService {

    private final StudentUserRepository studentUserRepository;
    private final AuthTokenRepository authTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public void forgotPassword(ForgotPasswordRequest request) {
        // BUG-02 FIX: Dùng event pattern (AFTER_COMMIT) nhất quán với register/resend.
        //   → Email chỉ gửi SAU KHI transaction commit → tránh gửi email khi DB rollback.
        // BUG-03 FIX: Không lộ thông tin người dùng (User Enumeration).
        //   → ifPresent: nếu email không tồn tại, không throw 404, luôn return 200 OK.
        studentUserRepository
                .findByEmail(request.getEmail().trim().toLowerCase())
                .ifPresent(user -> {
                    // Rate limit: kiểm tra token cuối cùng trong DB, nhất quán với resendVerification.
                    Optional<AuthToken> lastToken =
                            authTokenRepository.findFirstByStudentIdAndTokenTypeOrderByCreatedAtDesc(
                                    user.getId(), AuthToken.TokenType.PASSWORD_RESET);
                    if (lastToken.isPresent()
                            && lastToken.get().getCreatedAt().plusSeconds(60).isAfter(LocalDateTime.now())) {
                        throw new BusinessException(
                                429, "TOO_MANY_REQUESTS", "Vui lòng đợi 60 giây trước khi yêu cầu đặt lại mật khẩu");
                    }

                    authTokenRepository.deleteByStudentIdAndTokenType(user.getId(), AuthToken.TokenType.PASSWORD_RESET);

                    String resetToken = UUID.randomUUID().toString();
                    AuthToken tokenEntity = AuthToken.builder()
                            .actorType(AuthToken.ActorType.STUDENT)
                            .studentId(user.getId())
                            .tokenType(AuthToken.TokenType.PASSWORD_RESET)
                            .tokenValue(resetToken)
                            .expiresAt(LocalDateTime.now().plusHours(1))
                            .build();
                    authTokenRepository.save(tokenEntity);

                    log.info("[PasswordResetService] Password reset token created for: {}", user.getEmail());
                    eventPublisher.publishEvent(new SendPasswordResetEmailEvent(user.getEmail(), resetToken));
                });
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new BusinessException(400, "PASSWORD_MISMATCH", "Mật khẩu xác nhận không khớp");
        }

        AuthToken tokenEntity = authTokenRepository
                .findByTokenValueAndTokenType(request.getToken(), AuthToken.TokenType.PASSWORD_RESET)
                .orElseThrow(() -> new BusinessException(400, "INVALID_TOKEN", "Mã đặt lại mật khẩu không hợp lệ"));

        if (tokenEntity.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BusinessException(400, "TOKEN_EXPIRED", "Mã đặt lại mật khẩu đã hết hạn");
        }

        StudentUser user = studentUserRepository
                .findById(tokenEntity.getStudentId())
                .orElseThrow(() -> new BusinessException(404, "USER_NOT_FOUND", "Người dùng không tồn tại"));

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        studentUserRepository.save(user);

        authTokenRepository.delete(tokenEntity);
    }

    @Transactional
    public void changePassword(Long studentId, ChangePasswordRequest request) {
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new BusinessException(400, "PASSWORD_MISMATCH", "Mật khẩu xác nhận không khớp");
        }

        StudentUser user = studentUserRepository
                .findById(studentId)
                .orElseThrow(() -> new BusinessException(404, "USER_NOT_FOUND", "Người dùng không tồn tại"));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            throw new BusinessException(400, "INVALID_PASSWORD", "Mật khẩu hiện tại không đúng");
        }

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        studentUserRepository.save(user);
    }
}
