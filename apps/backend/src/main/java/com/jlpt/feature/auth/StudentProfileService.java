/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.auth;

import com.jlpt.feature.admin.AdminUserRepository;
import com.jlpt.feature.auth.dto.request.ConfirmEmailChangeRequest;
import com.jlpt.feature.auth.dto.request.RequestEmailChangeRequest;
import com.jlpt.feature.staff.StaffUserRepository;
import com.jlpt.feature.student.StudentUser;
import com.jlpt.feature.student.StudentUserRepository;
import com.jlpt.feature.student.dto.request.OnboardingRequest;
import com.jlpt.feature.student.dto.request.UpdateProfileRequest;
import com.jlpt.feature.student.dto.response.StudentResponse;
import com.jlpt.shared.common.JlptLevels;
import com.jlpt.shared.exception.BusinessException;
import com.jlpt.shared.security.OtpVerificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Hồ sơ học viên: xem/sửa thông tin, onboarding, avatar, và đổi email (xác thực qua OTP). */
@Service
@RequiredArgsConstructor
@Slf4j
public class StudentProfileService {

    private final StudentUserRepository studentUserRepository;
    private final StaffUserRepository staffUserRepository;
    private final AdminUserRepository adminUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final OtpVerificationService otpVerificationService;
    private final StudentResponseMapper studentResponseMapper;

    public StudentResponse getProfile(Long studentId) {
        StudentUser user = studentUserRepository
                .findById(studentId)
                .orElseThrow(() -> new BusinessException(404, "USER_NOT_FOUND", "Người dùng không tồn tại"));
        return studentResponseMapper.toResponse(user);
    }

    @Transactional
    public StudentResponse updateProfile(Long studentId, UpdateProfileRequest request) {
        StudentUser user = studentUserRepository
                .findById(studentId)
                .orElseThrow(() -> new BusinessException(404, "USER_NOT_FOUND", "Người dùng không tồn tại"));

        user.setFullName(request.getFullName());
        user.setPhone(request.getPhone());

        // Chỉ ghi avatarUrl khi client thực sự gửi — tránh xoá mất avatar đã upload
        // (Profile gọi upload avatar trước rồi mới updateProfile mà không kèm avatarUrl).
        if (request.getAvatarUrl() != null) {
            user.setAvatarUrl(request.getAvatarUrl());
        }

        if (request.getTargetJlptLevel() != null) {
            user.setTargetJlptLevel(StudentUser.JlptLevel.valueOf(request.getTargetJlptLevel()));
        }

        return studentResponseMapper.toResponse(studentUserRepository.save(user));
    }

    /**
     * Onboarding: lưu mục tiêu JLPT của học viên (jlptGoal → target_jlpt_level).
     * dailyMinutes/focusSkills hiện chưa có cột lưu nên được bỏ qua (xem OnboardingRequest).
     */
    @Transactional
    public StudentResponse submitOnboarding(Long studentId, OnboardingRequest request) {
        StudentUser user = studentUserRepository
                .findById(studentId)
                .orElseThrow(() -> new BusinessException(404, "USER_NOT_FOUND", "Người dùng không tồn tại"));

        // Cấp độ chọn ở onboarding vừa là mục tiêu (target) vừa là cấp đang học (current)
        // → Dashboard hiển thị đúng cấp độ người dùng vừa chọn.
        StudentUser.JlptLevel level = JlptLevels.parseRequired(request.getJlptGoal());
        user.setTargetJlptLevel(level);
        user.setCurrentJlptLevel(level);
        return studentResponseMapper.toResponse(studentUserRepository.save(user));
    }

    /** Cập nhật URL ảnh đại diện (đã được {@code AvatarStorageService} lưu ra /uploads). */
    @Transactional
    public StudentResponse updateAvatar(Long studentId, String avatarUrl) {
        StudentUser user = studentUserRepository
                .findById(studentId)
                .orElseThrow(() -> new BusinessException(404, "USER_NOT_FOUND", "Người dùng không tồn tại"));

        user.setAvatarUrl(avatarUrl);
        return studentResponseMapper.toResponse(studentUserRepository.save(user));
    }

    /** Gửi OTP đến email mới sau khi xác nhận mật khẩu hiện tại. Chưa cập nhật email. */
    @Transactional(readOnly = true)
    public void requestEmailChange(Long studentId, RequestEmailChangeRequest request) {
        StudentUser user = studentUserRepository
                .findById(studentId)
                .orElseThrow(() -> new BusinessException(404, "USER_NOT_FOUND", "Người dùng không tồn tại"));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            throw new BusinessException(400, "INVALID_PASSWORD", "Mật khẩu hiện tại không đúng");
        }

        String newEmail = request.getNewEmail().trim().toLowerCase();
        if (newEmail.equals(user.getEmail())) {
            throw new BusinessException(400, "SAME_EMAIL", "Email mới phải khác email hiện tại");
        }
        if (studentUserRepository.existsByEmail(newEmail)
                || staffUserRepository.existsByEmail(newEmail)
                || adminUserRepository.existsByEmail(newEmail)) {
            throw new BusinessException(409, "EMAIL_EXISTS", "Email đã được sử dụng");
        }

        otpVerificationService.generateAndSend(newEmail);
        log.info("[StudentProfileService] Email change OTP requested studentId={} newEmail={}", studentId, newEmail);
    }

    /** Xác thực OTP gửi tới email mới rồi mới cập nhật StudentUser.email. */
    @Transactional
    public StudentResponse confirmEmailChange(Long studentId, ConfirmEmailChangeRequest request) {
        StudentUser user = studentUserRepository
                .findById(studentId)
                .orElseThrow(() -> new BusinessException(404, "USER_NOT_FOUND", "Người dùng không tồn tại"));

        String newEmail = request.getNewEmail().trim().toLowerCase();
        if (!otpVerificationService.verify(newEmail, request.getOtpCode())) {
            throw new BusinessException(400, "INVALID_OTP", "Mã OTP không đúng hoặc đã hết hạn");
        }
        // Kiểm tra lại tại thời điểm xác nhận, tránh race condition giữa lúc gửi OTP và lúc xác nhận.
        if (studentUserRepository.existsByEmail(newEmail)) {
            throw new BusinessException(409, "EMAIL_EXISTS", "Email đã được sử dụng");
        }

        user.setEmail(newEmail);
        StudentUser saved = studentUserRepository.save(user);
        log.info("[StudentProfileService] Email changed studentId={} newEmail={}", studentId, newEmail);
        return studentResponseMapper.toResponse(saved);
    }
}
