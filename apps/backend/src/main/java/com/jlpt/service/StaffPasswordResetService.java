/* (c) JLPT E-Learning Platform */
package com.jlpt.service;

import com.jlpt.dto.request.ChangeTempPasswordRequest;
import com.jlpt.dto.request.IssueTempPasswordRequest;
import com.jlpt.dto.request.StaffForgotPasswordRequest;
import com.jlpt.dto.response.IssueTempPasswordResponse;
import com.jlpt.dto.response.StaffResetRequestResponse;
import com.jlpt.entity.AdminAuditLog;
import com.jlpt.entity.AdminUser;
import com.jlpt.entity.AuthToken;
import com.jlpt.entity.StaffPasswordResetRequest;
import com.jlpt.entity.StaffUser;
import com.jlpt.exception.BusinessException;
import com.jlpt.exception.ResourceNotFoundException;
import com.jlpt.repository.AdminAuditLogRepository;
import com.jlpt.repository.AdminUserRepository;
import com.jlpt.repository.AuthTokenRepository;
import com.jlpt.repository.StaffPasswordResetRequestRepository;
import com.jlpt.repository.StaffUserRepository;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class StaffPasswordResetService {

    private static final int MAX_REQUESTS_PER_HOUR = 3;
    private static final int TEMP_PASSWORD_LENGTH = 12;
    private static final String UPPER = "ABCDEFGHJKLMNPQRSTUVWXYZ";
    private static final String LOWER = "abcdefghijkmnopqrstuvwxyz";
    private static final String DIGITS = "23456789";
    private static final String SPECIALS = "!@#$%";
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final StaffUserRepository staffUserRepository;
    private final AdminUserRepository adminUserRepository;
    private final StaffPasswordResetRequestRepository resetRequestRepository;
    private final AuthTokenRepository authTokenRepository;
    private final AdminAuditLogRepository auditLogRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    @Transactional
    public void requestReset(StaffForgotPasswordRequest request, String ip) {
        String email = request.getEmail().trim().toLowerCase();
        staffUserRepository.findByEmail(email).ifPresent(staff -> createRequestForActiveStaff(staff, ip));
    }

    @Transactional(readOnly = true)
    public List<StaffResetRequestResponse> listRequests(String status) {
        StaffPasswordResetRequest.ResetStatus resetStatus = parseStatus(status);
        return resetRequestRepository.findByStatusOrderByRequestedAtDesc(resetStatus).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public IssueTempPasswordResponse issueTempPassword(
            String adminEmail, Long staffId, IssueTempPasswordRequest request) {
        AdminUser admin = adminUserRepository
                .findByEmail(adminEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy Admin"));
        StaffPasswordResetRequest resetRequest = resetRequestRepository
                .findById(request.getRequestId())
                .orElseThrow(() -> new BusinessException(
                        404, "RESET_REQUEST_NOT_FOUND", "Không tìm thấy yêu cầu đặt lại mật khẩu"));
        StaffUser staff = staffUserRepository
                .findById(staffId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy nhân viên"));

        validateIssueRequest(resetRequest, staffId);

        String tempPassword = generateTempPassword();
        LocalDateTime now = LocalDateTime.now();
        staff.setPasswordHash(passwordEncoder.encode(tempPassword));
        staff.setMustChangePassword(true);
        staffUserRepository.save(staff);

        authTokenRepository.revokeActiveByStaffIdAndTokenTypes(
                staffId,
                List.of(
                        AuthToken.TokenType.SESSION,
                        AuthToken.TokenType.REFRESH,
                        AuthToken.TokenType.LIMITED_SESSION),
                now);
        emailService.sendStaffTempPassword(staff.getEmail(), tempPassword);

        resetRequest.setStatus(StaffPasswordResetRequest.ResetStatus.COMPLETED);
        resetRequest.setCompletedAt(now);
        resetRequest.setCompletedBy(admin.getId());
        resetRequestRepository.save(resetRequest);

        audit(admin, "issue_temp_password", "staff_users", staffId, "Issued temporary password for staff reset");
        log.info("[StaffPasswordResetService] Admin issued temporary password staffId={}", staffId);

        return IssueTempPasswordResponse.builder()
                .staffId(staffId)
                .staffEmail(staff.getEmail())
                .completedAt(now)
                .build();
    }

    @Transactional
    public void changeTempPassword(String limitedToken, ChangeTempPasswordRequest request) {
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new BusinessException(400, "PASSWORD_MISMATCH", "Mật khẩu xác nhận không khớp");
        }
        validateStrongPassword(request.getNewPassword());

        AuthToken token = authTokenRepository
                .findByTokenValueAndTokenType(limitedToken, AuthToken.TokenType.LIMITED_SESSION)
                .orElseThrow(() -> new BusinessException(401, "UNAUTHORIZED", "Yêu cầu đăng nhập"));
        validateLimitedToken(token);

        StaffUser staff = staffUserRepository
                .findById(token.getStaffId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy nhân viên"));
        if (!Boolean.TRUE.equals(staff.getMustChangePassword())) {
            throw new BusinessException(400, "PASSWORD_CHANGE_NOT_REQUIRED", "Tài khoản không cần đổi mật khẩu tạm");
        }
        if (passwordEncoder.matches(request.getNewPassword(), staff.getPasswordHash())) {
            throw new BusinessException(
                    422, "SAME_PASSWORD", "Mật khẩu mới không được giống mật khẩu tạm thời.");
        }

        LocalDateTime now = LocalDateTime.now();
        staff.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        staff.setMustChangePassword(false);
        staffUserRepository.save(staff);

        token.setRevokedAt(now);
        authTokenRepository.save(token);
        log.info("[StaffPasswordResetService] Staff changed temporary password staffId={}", staff.getId());
    }

    private void createRequestForActiveStaff(StaffUser staff, String ip) {
        if (staff.getStatus() != StaffUser.StaffStatus.ACTIVE) {
            return;
        }
        LocalDateTime now = LocalDateTime.now();
        long recentRequests = resetRequestRepository.countByStaffIdAndRequestedAtAfter(
                staff.getId(), now.minusHours(1));
        if (recentRequests >= MAX_REQUESTS_PER_HOUR) {
            throw new BusinessException(
                    429, "TOO_MANY_REQUESTS", "Quá nhiều yêu cầu. Vui lòng thử lại sau.");
        }

        StaffPasswordResetRequest resetRequest = StaffPasswordResetRequest.builder()
                .staffId(staff.getId())
                .status(StaffPasswordResetRequest.ResetStatus.PENDING)
                .expiresAt(now.plusHours(24))
                .requestIp(ip)
                .build();
        resetRequestRepository.save(resetRequest);
        emailService.notifyAdminPasswordReset(staff.getFullName(), staff.getEmail());
        log.info("[StaffPasswordResetService] Staff password reset requested staffId={}", staff.getId());
    }

    private void validateIssueRequest(StaffPasswordResetRequest resetRequest, Long staffId) {
        if (!resetRequest.getStaffId().equals(staffId)) {
            throw new BusinessException(404, "RESET_REQUEST_NOT_FOUND", "Không tìm thấy yêu cầu đặt lại mật khẩu");
        }
        if (resetRequest.getStatus() != StaffPasswordResetRequest.ResetStatus.PENDING) {
            throw new BusinessException(400, "RESET_REQUEST_ALREADY_PROCESSED", "Yêu cầu này đã được xử lý.");
        }
        if (resetRequest.getExpiresAt().isBefore(LocalDateTime.now())) {
            resetRequest.setStatus(StaffPasswordResetRequest.ResetStatus.EXPIRED);
            resetRequestRepository.save(resetRequest);
            throw new BusinessException(
                    400,
                    "RESET_REQUEST_EXPIRED",
                    "Yêu cầu đặt lại mật khẩu đã hết hạn. Staff cần gửi yêu cầu mới.");
        }
    }

    private void validateLimitedToken(AuthToken token) {
        if (token.getRevokedAt() != null || token.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BusinessException(401, "UNAUTHORIZED", "Yêu cầu đăng nhập");
        }
    }

    private void validateStrongPassword(String password) {
        boolean hasUpper = password.chars().anyMatch(Character::isUpperCase);
        boolean hasDigit = password.chars().anyMatch(Character::isDigit);
        if (password.length() < 8 || !hasUpper || !hasDigit) {
            throw new BusinessException(
                    422, "WEAK_PASSWORD", "Mật khẩu quá yếu: cần >= 8 ký tự, 1 hoa, 1 số");
        }
    }

    private String generateTempPassword() {
        List<Character> chars = new ArrayList<>();
        chars.add(randomChar(UPPER));
        chars.add(randomChar(LOWER));
        chars.add(randomChar(DIGITS));
        chars.add(randomChar(SPECIALS));
        String all = UPPER + LOWER + DIGITS + SPECIALS;
        while (chars.size() < TEMP_PASSWORD_LENGTH) {
            chars.add(randomChar(all));
        }
        Collections.shuffle(chars, SECURE_RANDOM);
        StringBuilder password = new StringBuilder(TEMP_PASSWORD_LENGTH);
        chars.forEach(password::append);
        return password.toString();
    }

    private char randomChar(String source) {
        return source.charAt(SECURE_RANDOM.nextInt(source.length()));
    }

    private StaffPasswordResetRequest.ResetStatus parseStatus(String status) {
        if (status == null || status.isBlank()) {
            return StaffPasswordResetRequest.ResetStatus.PENDING;
        }
        try {
            return StaffPasswordResetRequest.ResetStatus.valueOf(status.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new BusinessException(400, "INVALID_RESET_STATUS", "Trạng thái yêu cầu không hợp lệ");
        }
    }

    private StaffResetRequestResponse toResponse(StaffPasswordResetRequest resetRequest) {
        StaffUser staff = staffUserRepository.findById(resetRequest.getStaffId()).orElse(null);
        return StaffResetRequestResponse.builder()
                .requestId(resetRequest.getId())
                .staffId(resetRequest.getStaffId())
                .staffName(staff != null ? staff.getFullName() : null)
                .staffEmail(staff != null ? staff.getEmail() : null)
                .requestedAt(resetRequest.getRequestedAt())
                .expiresAt(resetRequest.getExpiresAt())
                .status(resetRequest.getStatus().getValue())
                .build();
    }

    private void audit(AdminUser admin, String action, String targetTable, Long targetId, String description) {
        auditLogRepository.save(AdminAuditLog.builder()
                .adminActor(admin)
                .action(action)
                .targetTable(targetTable)
                .targetId(targetId)
                .description(description)
                .build());
    }
}
