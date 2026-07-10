/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.admin;

import com.jlpt.feature.admin.dto.request.SuspendUserRequest;
import com.jlpt.feature.admin.dto.response.ActivateUserResponse;
import com.jlpt.feature.admin.dto.response.AdminDetailResponse;
import com.jlpt.feature.admin.dto.response.RestoreUserResponse;
import com.jlpt.feature.admin.dto.response.SoftDeleteUserResponse;
import com.jlpt.feature.admin.dto.response.SuspendUserResponse;
import com.jlpt.feature.admin.dto.response.UserSummaryResponse;
import com.jlpt.feature.auth.AuthToken;
import com.jlpt.feature.auth.AuthTokenRepository;
import com.jlpt.feature.staff.StaffUser;
import com.jlpt.feature.staff.StaffUserRepository;
import com.jlpt.feature.staff.dto.request.ChangeStaffRoleRequest;
import com.jlpt.feature.staff.dto.request.CreateStaffRequest;
import com.jlpt.feature.staff.dto.request.UpdateStaffInfoRequest;
import com.jlpt.feature.staff.dto.response.ChangeStaffRoleResponse;
import com.jlpt.feature.staff.dto.response.CreateStaffResponse;
import com.jlpt.feature.staff.dto.response.StaffDetailResponse;
import com.jlpt.feature.student.StudentUser;
import com.jlpt.feature.student.StudentUserRepository;
import com.jlpt.feature.student.dto.request.UpdateStudentRequest;
import com.jlpt.feature.student.dto.response.StudentDetailResponse;
import com.jlpt.shared.email.EmailService;
import com.jlpt.shared.exception.BadRequestException;
import com.jlpt.shared.exception.BusinessException;
import com.jlpt.shared.exception.BusinessRuleException;
import com.jlpt.shared.exception.DuplicateResourceException;
import com.jlpt.shared.exception.ForbiddenException;
import com.jlpt.shared.exception.ResourceNotFoundException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminUserService {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final StudentUserRepository studentUserRepository;
    private final StaffUserRepository staffUserRepository;
    private final AdminUserRepository adminUserRepository;
    private final AuthTokenRepository authTokenRepository;
    private final AdminAuditLogRepository auditLogRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    // ── UC-37-01: List users with filters ──────────────────────────────────

    @Transactional(readOnly = true)
    public Page<UserSummaryResponse> listUsers(
            String type, String q, String status, String jlptLevel, String staffRole, int page, int size) {

        String normalizedType = normalizeType(type);
        String searchPattern = StringUtils.hasText(q) ? "%" + q + "%" : null;
        String normalizedStatus = StringUtils.hasText(status) ? status.toLowerCase() : null;
        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "created_at"));

        return switch (normalizedType) {
            case "student" -> {
                String level = StringUtils.hasText(jlptLevel) ? jlptLevel.toUpperCase() : null;
                yield studentUserRepository
                        .findAllAdminFiltered(searchPattern, normalizedStatus, level, pageable)
                        .map(this::toStudentSummary);
            }
            case "staff" -> {
                String role = StringUtils.hasText(staffRole) ? staffRole.toLowerCase() : null;
                yield staffUserRepository
                        .findAllAdminFiltered(searchPattern, normalizedStatus, role, pageable)
                        .map(this::toStaffSummary);
            }
            case "admin" -> adminUserRepository
                    .findAllAdminFiltered(searchPattern, normalizedStatus, pageable)
                    .map(this::toAdminSummary);
            default -> throw new BadRequestException("Loại người dùng không hợp lệ");
        };
    }

    // ── UC-37-02: Get user detail ───────────────────────────────────────────

    @Transactional(readOnly = true)
    public Object getUserDetail(String type, Long userId) {
        return switch (normalizeType(type)) {
            case "student" -> {
                StudentUser s = studentUserRepository
                        .findById(userId)
                        .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng"));
                yield toStudentDetail(s);
            }
            case "staff" -> {
                StaffUser st = staffUserRepository
                        .findById(userId)
                        .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng"));
                yield toStaffDetail(st);
            }
            case "admin" -> {
                AdminUser a = adminUserRepository
                        .findById(userId)
                        .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng"));
                yield toAdminDetail(a);
            }
            default -> throw new BadRequestException("Loại người dùng không hợp lệ");
        };
    }

    // ── UC-37-03: Create Staff ──────────────────────────────────────────────

    @Transactional
    public CreateStaffResponse createStaff(String adminEmail, CreateStaffRequest request) {
        AdminUser actor = resolveAdmin(adminEmail);

        String email = request.getEmail().trim().toLowerCase();
        if (studentUserRepository.existsByEmail(email)
                || staffUserRepository.existsByEmail(email)
                || adminUserRepository.existsByEmail(email)) {
            throw new DuplicateResourceException("Email đã được sử dụng trong hệ thống");
        }

        StaffUser.StaffRole role =
                StaffUser.StaffRole.valueOf(request.getStaffRole().toUpperCase());
        StaffUser staff = StaffUser.builder()
                .email(email)
                .fullName(request.getFullName().trim())
                .staffRole(role)
                .status(StaffUser.StaffStatus.PENDING)
                .build();
        staff = staffUserRepository.save(staff);

        String inviteToken = generateUrlSafeToken(32);
        authTokenRepository.save(AuthToken.builder()
                .actorType(AuthToken.ActorType.STAFF)
                .staffId(staff.getId())
                .tokenType(AuthToken.TokenType.EMAIL_VERIFICATION)
                .tokenValue(inviteToken)
                .expiresAt(LocalDateTime.now().plusHours(24))
                .build());

        emailService.sendStaffInvitationEmail(email, inviteToken);

        auditLog(actor, "create_staff", "staff_users", staff.getId(), "Tạo tài khoản Staff: " + email);

        log.info("[AdminUserService] Admin {} created staff id={} email={}", adminEmail, staff.getId(), email);
        return CreateStaffResponse.builder()
                .staffId(staff.getId())
                .fullName(staff.getFullName())
                .email(staff.getEmail())
                .staffRole(staff.getStaffRole().getValue())
                .status(staff.getStatus().getValue())
                .build();
    }

    // ── Staff invitation setup password ────────────────────────────────────

    @Transactional
    public void setupStaffPassword(com.jlpt.feature.staff.dto.request.StaffSetupPasswordRequest request) {
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new BusinessException(400, "PASSWORD_MISMATCH", "Mật khẩu xác nhận không khớp");
        }

        AuthToken token = authTokenRepository
                .findByTokenValueAndTokenType(request.getToken(), AuthToken.TokenType.EMAIL_VERIFICATION)
                .orElseThrow(() -> new BusinessException(
                        400, "INVALID_TOKEN", "Link kích hoạt không hợp lệ hoặc đã được sử dụng"));

        if (token.getActorType() != AuthToken.ActorType.STAFF) {
            throw new BusinessException(400, "INVALID_TOKEN", "Link kích hoạt không hợp lệ");
        }

        if (token.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BusinessException(
                    400, "TOKEN_EXPIRED", "Link kích hoạt đã hết hạn. Vui lòng liên hệ Admin để được cấp lại.");
        }

        StaffUser staff = staffUserRepository
                .findById(token.getStaffId())
                .orElseThrow(() -> new BusinessException(404, "USER_NOT_FOUND", "Tài khoản không tồn tại"));

        if (staff.getStatus() != StaffUser.StaffStatus.PENDING) {
            throw new BusinessException(400, "ALREADY_ACTIVATED", "Tài khoản đã được kích hoạt trước đó");
        }

        staff.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        staff.setStatus(StaffUser.StaffStatus.ACTIVE);
        staffUserRepository.save(staff);

        authTokenRepository.delete(token);

        log.info(
                "[AdminUserService] Staff id={} email={} completed setup-password and is now ACTIVE",
                staff.getId(),
                staff.getEmail());
    }

    // ── UC-37-04: Edit user info ────────────────────────────────────────────

    @Transactional
    public Object updateUser(String adminEmail, String type, Long userId, Object request) {
        AdminUser actor = resolveAdmin(adminEmail);
        checkSelfModification(actor.getId(), type, userId);

        return switch (normalizeType(type)) {
            case "student" -> {
                StudentUser s = studentUserRepository
                        .findById(userId)
                        .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng"));
                UpdateStudentRequest req = (UpdateStudentRequest) request;
                StringBuilder changes = new StringBuilder();
                if (StringUtils.hasText(req.getFullName())) {
                    changes.append("fullName: ")
                            .append(s.getFullName())
                            .append("→")
                            .append(req.getFullName())
                            .append("; ");
                    s.setFullName(req.getFullName().trim());
                }
                if (req.getPhone() != null) {
                    s.setPhone(req.getPhone().isBlank() ? null : req.getPhone().trim());
                }
                if (StringUtils.hasText(req.getTargetJlptLevel())) {
                    s.setTargetJlptLevel(StudentUser.JlptLevel.valueOf(
                            req.getTargetJlptLevel().toUpperCase()));
                }
                s = studentUserRepository.save(s);
                auditLog(actor, "update_user", "student_users", userId, changes.toString());
                yield toStudentDetail(s);
            }
            case "staff" -> {
                StaffUser st = staffUserRepository
                        .findById(userId)
                        .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng"));
                UpdateStaffInfoRequest req = (UpdateStaffInfoRequest) request;
                if (StringUtils.hasText(req.getFullName())) {
                    st.setFullName(req.getFullName().trim());
                }
                st = staffUserRepository.save(st);
                auditLog(actor, "update_user", "staff_users", userId, "fullName updated");
                yield toStaffDetail(st);
            }
            case "admin" -> throw new ForbiddenException(
                    "Không thể thực hiện thao tác này lên tài khoản của chính mình");
            default -> throw new BadRequestException("Loại người dùng không hợp lệ");
        };
    }

    // ── UC-37-05: Suspend user ──────────────────────────────────────────────

    @Transactional
    public SuspendUserResponse suspendUser(String adminEmail, String type, Long userId, SuspendUserRequest request) {
        AdminUser actor = resolveAdmin(adminEmail);
        checkSelfModification(actor.getId(), type, userId);

        LocalDateTime now = LocalDateTime.now();

        return switch (normalizeType(type)) {
            case "student" -> {
                StudentUser s = studentUserRepository
                        .findById(userId)
                        .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng"));
                if (s.getStatus() == StudentUser.StudentStatus.SUSPENDED
                        || s.getStatus() == StudentUser.StudentStatus.DELETED) {
                    throw new DuplicateResourceException("Tài khoản đã ở trạng thái này rồi");
                }
                s.setStatus(StudentUser.StudentStatus.SUSPENDED);
                s.setSuspendReason(request.getReason());
                studentUserRepository.save(s);
                authTokenRepository.revokeAllActiveByStudentId(userId, now);
                auditLog(actor, "suspend_user", "student_users", userId, request.getReason());
                yield SuspendUserResponse.builder()
                        .userId(userId)
                        .userType("student")
                        .status("suspended")
                        .suspendReason(request.getReason())
                        .suspendedAt(now)
                        .build();
            }
            case "staff" -> {
                StaffUser st = staffUserRepository
                        .findById(userId)
                        .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng"));
                if (st.getStatus() == StaffUser.StaffStatus.SUSPENDED
                        || st.getStatus() == StaffUser.StaffStatus.DELETED) {
                    throw new DuplicateResourceException("Tài khoản đã ở trạng thái này rồi");
                }
                st.setStatus(StaffUser.StaffStatus.SUSPENDED);
                st.setSuspendReason(request.getReason());
                staffUserRepository.save(st);
                authTokenRepository.revokeAllActiveByStaffId(userId, now);
                auditLog(actor, "suspend_user", "staff_users", userId, request.getReason());
                yield SuspendUserResponse.builder()
                        .userId(userId)
                        .userType("staff")
                        .status("suspended")
                        .suspendReason(request.getReason())
                        .suspendedAt(now)
                        .build();
            }
            case "admin" -> {
                AdminUser a = adminUserRepository
                        .findById(userId)
                        .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng"));
                if (a.getStatus() == AdminUser.AdminStatus.SUSPENDED
                        || a.getStatus() == AdminUser.AdminStatus.DELETED) {
                    throw new DuplicateResourceException("Tài khoản đã ở trạng thái này rồi");
                }
                a.setStatus(AdminUser.AdminStatus.SUSPENDED);
                a.setSuspendReason(request.getReason());
                adminUserRepository.save(a);
                authTokenRepository.revokeAllActiveByAdminId(userId, now);
                auditLog(actor, "suspend_user", "admin_users", userId, request.getReason());
                yield SuspendUserResponse.builder()
                        .userId(userId)
                        .userType("admin")
                        .status("suspended")
                        .suspendReason(request.getReason())
                        .suspendedAt(now)
                        .build();
            }
            default -> throw new BadRequestException("Loại người dùng không hợp lệ");
        };
    }

    // ── UC-37-06: Activate user ─────────────────────────────────────────────

    @Transactional
    public ActivateUserResponse activateUser(String adminEmail, String type, Long userId) {
        AdminUser actor = resolveAdmin(adminEmail);
        checkSelfModification(actor.getId(), type, userId);
        LocalDateTime now = LocalDateTime.now();

        return switch (normalizeType(type)) {
            case "student" -> {
                StudentUser s = studentUserRepository
                        .findById(userId)
                        .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng"));
                if (s.getStatus() != StudentUser.StudentStatus.SUSPENDED) {
                    throw new DuplicateResourceException("Tài khoản đã ở trạng thái này rồi");
                }
                s.setStatus(StudentUser.StudentStatus.ACTIVE);
                s.setSuspendReason(null);
                studentUserRepository.save(s);
                auditLog(actor, "activate_user", "student_users", userId, null);
                yield ActivateUserResponse.builder()
                        .userId(userId)
                        .userType("student")
                        .status("active")
                        .activatedAt(now)
                        .build();
            }
            case "staff" -> {
                StaffUser st = staffUserRepository
                        .findById(userId)
                        .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng"));
                if (st.getStatus() != StaffUser.StaffStatus.SUSPENDED) {
                    throw new DuplicateResourceException("Tài khoản đã ở trạng thái này rồi");
                }
                st.setStatus(StaffUser.StaffStatus.ACTIVE);
                st.setSuspendReason(null);
                staffUserRepository.save(st);
                auditLog(actor, "activate_user", "staff_users", userId, null);
                yield ActivateUserResponse.builder()
                        .userId(userId)
                        .userType("staff")
                        .status("active")
                        .activatedAt(now)
                        .build();
            }
            case "admin" -> {
                AdminUser a = adminUserRepository
                        .findById(userId)
                        .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng"));
                if (a.getStatus() != AdminUser.AdminStatus.SUSPENDED) {
                    throw new DuplicateResourceException("Tài khoản đã ở trạng thái này rồi");
                }
                a.setStatus(AdminUser.AdminStatus.ACTIVE);
                a.setSuspendReason(null);
                adminUserRepository.save(a);
                auditLog(actor, "activate_user", "admin_users", userId, null);
                yield ActivateUserResponse.builder()
                        .userId(userId)
                        .userType("admin")
                        .status("active")
                        .activatedAt(now)
                        .build();
            }
            default -> throw new BadRequestException("Loại người dùng không hợp lệ");
        };
    }

    // ── UC-37-07: Admin-initiated password reset ────────────────────────────

    @Transactional
    public void resetPassword(String adminEmail, String type, Long userId) {
        AdminUser actor = resolveAdmin(adminEmail);
        LocalDateTime now = LocalDateTime.now();
        String targetEmail;

        switch (normalizeType(type)) {
            case "student" -> {
                StudentUser s = studentUserRepository
                        .findById(userId)
                        .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng"));
                if (s.getStatus() == StudentUser.StudentStatus.DELETED) {
                    throw new ResourceNotFoundException("Không tìm thấy người dùng");
                }
                authTokenRepository.revokeAllActiveByStudentId(userId, now);
                targetEmail = s.getEmail();
                String token = generateUrlSafeToken(32);
                authTokenRepository.save(AuthToken.builder()
                        .actorType(AuthToken.ActorType.STUDENT)
                        .studentId(userId)
                        .tokenType(AuthToken.TokenType.PASSWORD_RESET)
                        .tokenValue(token)
                        .expiresAt(now.plusMinutes(15))
                        .build());
                emailService.sendPasswordResetEmail(targetEmail, token);
            }
            case "staff" -> {
                StaffUser st = staffUserRepository
                        .findById(userId)
                        .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng"));
                if (st.getStatus() == StaffUser.StaffStatus.DELETED) {
                    throw new ResourceNotFoundException("Không tìm thấy người dùng");
                }
                authTokenRepository.revokeAllActiveByStaffId(userId, now);
                targetEmail = st.getEmail();
                String token = generateUrlSafeToken(32);
                authTokenRepository.save(AuthToken.builder()
                        .actorType(AuthToken.ActorType.STAFF)
                        .staffId(userId)
                        .tokenType(AuthToken.TokenType.PASSWORD_RESET)
                        .tokenValue(token)
                        .expiresAt(now.plusMinutes(15))
                        .build());
                emailService.sendPasswordResetEmail(targetEmail, token);
            }
            case "admin" -> {
                AdminUser a = adminUserRepository
                        .findById(userId)
                        .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng"));
                if (a.getStatus() == AdminUser.AdminStatus.DELETED) {
                    throw new ResourceNotFoundException("Không tìm thấy người dùng");
                }
                authTokenRepository.revokeAllActiveByAdminId(userId, now);
                targetEmail = a.getEmail();
                String token = generateUrlSafeToken(32);
                authTokenRepository.save(AuthToken.builder()
                        .actorType(AuthToken.ActorType.ADMIN)
                        .adminId(userId)
                        .tokenType(AuthToken.TokenType.PASSWORD_RESET)
                        .tokenValue(token)
                        .expiresAt(now.plusMinutes(15))
                        .build());
                emailService.sendPasswordResetEmail(targetEmail, token);
            }
            default -> throw new BadRequestException("Loại người dùng không hợp lệ");
        }

        auditLog(actor, "reset_password_initiated", type + "_users", userId, null);
        log.info("[AdminUserService] Admin {} initiated password reset for {} id={}", adminEmail, type, userId);
    }

    // ── UC-37-08: Soft delete user ──────────────────────────────────────────

    @Transactional
    public SoftDeleteUserResponse softDeleteUser(String adminEmail, String type, Long userId) {
        AdminUser actor = resolveAdmin(adminEmail);
        checkSelfModification(actor.getId(), type, userId);
        LocalDateTime now = LocalDateTime.now();

        return switch (normalizeType(type)) {
            case "student" -> {
                StudentUser s = studentUserRepository
                        .findById(userId)
                        .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng"));
                if (s.getStatus() == StudentUser.StudentStatus.DELETED) {
                    throw new DuplicateResourceException("Tài khoản đã ở trạng thái này rồi");
                }
                s.setStatus(StudentUser.StudentStatus.DELETED);
                studentUserRepository.save(s);
                authTokenRepository.revokeAllActiveByStudentId(userId, now);
                auditLog(actor, "soft_delete_user", "student_users", userId, null);
                yield SoftDeleteUserResponse.builder()
                        .userId(userId)
                        .userType("student")
                        .status("deleted")
                        .deletedAt(now)
                        .build();
            }
            case "staff" -> {
                StaffUser st = staffUserRepository
                        .findById(userId)
                        .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng"));
                if (st.getStatus() == StaffUser.StaffStatus.DELETED) {
                    throw new DuplicateResourceException("Tài khoản đã ở trạng thái này rồi");
                }
                st.setStatus(StaffUser.StaffStatus.DELETED);
                staffUserRepository.save(st);
                authTokenRepository.revokeAllActiveByStaffId(userId, now);
                auditLog(actor, "soft_delete_user", "staff_users", userId, null);
                yield SoftDeleteUserResponse.builder()
                        .userId(userId)
                        .userType("staff")
                        .status("deleted")
                        .deletedAt(now)
                        .build();
            }
            case "admin" -> throw new BusinessRuleException(
                    "Không thể xóa tài khoản Admin đang hoạt động qua giao diện này");
            default -> throw new BadRequestException("Loại người dùng không hợp lệ");
        };
    }

    // ── UC-37-08B: Restore deleted user ─────────────────────────────────────

    @Transactional
    public RestoreUserResponse restoreUser(String adminEmail, String type, Long userId) {
        AdminUser actor = resolveAdmin(adminEmail);
        checkSelfModification(actor.getId(), type, userId);
        LocalDateTime now = LocalDateTime.now();

        return switch (normalizeType(type)) {
            case "student" -> {
                StudentUser s = studentUserRepository
                        .findById(userId)
                        .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng"));
                if (s.getStatus() != StudentUser.StudentStatus.DELETED) {
                    throw new BusinessRuleException("Tài khoản không ở trạng thái đã xóa");
                }
                s.setStatus(StudentUser.StudentStatus.ACTIVE);
                studentUserRepository.save(s);
                auditLog(actor, "restore_user", "student_users", userId, null);
                yield RestoreUserResponse.builder()
                        .userId(userId)
                        .userType("student")
                        .status("active")
                        .restoredAt(now)
                        .build();
            }
            case "staff" -> {
                StaffUser st = staffUserRepository
                        .findById(userId)
                        .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng"));
                if (st.getStatus() != StaffUser.StaffStatus.DELETED) {
                    throw new BusinessRuleException("Tài khoản không ở trạng thái đã xóa");
                }
                st.setStatus(StaffUser.StaffStatus.ACTIVE);
                staffUserRepository.save(st);
                auditLog(actor, "restore_user", "staff_users", userId, null);
                yield RestoreUserResponse.builder()
                        .userId(userId)
                        .userType("staff")
                        .status("active")
                        .restoredAt(now)
                        .build();
            }
            case "admin" -> throw new BusinessRuleException("Không áp dụng cho Admin");
            default -> throw new BadRequestException("Loại người dùng không hợp lệ");
        };
    }

    // ── UC-37-09: Change Staff role ─────────────────────────────────────────

    @Transactional
    public ChangeStaffRoleResponse changeStaffRole(String adminEmail, Long staffId, ChangeStaffRoleRequest request) {
        AdminUser actor = resolveAdmin(adminEmail);
        StaffUser staff = staffUserRepository
                .findById(staffId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy nhân viên"));

        StaffUser.StaffRole newRole =
                StaffUser.StaffRole.valueOf(request.getStaffRole().toUpperCase());
        String oldRoleValue = staff.getStaffRole().getValue();

        staff.setStaffRole(newRole);
        staffUserRepository.save(staff);

        auditLog(actor, "change_staff_role", "staff_users", staffId, oldRoleValue + " → " + newRole.getValue());

        log.info(
                "[AdminUserService] Admin {} changed staffId={} role {} → {}",
                adminEmail,
                staffId,
                oldRoleValue,
                newRole.getValue());

        return ChangeStaffRoleResponse.builder()
                .staffId(staffId)
                .oldRole(oldRoleValue)
                .newRole(newRole.getValue())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    // ── Private helpers ─────────────────────────────────────────────────────

    private String normalizeType(String type) {
        if (!StringUtils.hasText(type)) throw new BadRequestException("Loại người dùng không hợp lệ");
        return type.toLowerCase();
    }

    private AdminUser resolveAdmin(String email) {
        return adminUserRepository
                .findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy Admin"));
    }

    /** BR-37-01: Admin cannot modify their own account. */
    private void checkSelfModification(Long actorAdminId, String type, Long targetId) {
        if ("admin".equalsIgnoreCase(type) && actorAdminId.equals(targetId)) {
            throw new ForbiddenException("Không thể thực hiện thao tác này lên tài khoản của chính mình");
        }
    }

    private void auditLog(AdminUser actor, String action, String targetTable, Long targetId, String description) {
        auditLogRepository.save(AdminAuditLog.builder()
                .adminActor(actor)
                .action(action)
                .targetTable(targetTable)
                .targetId(targetId)
                .description(description)
                .build());
    }

    private String generateUrlSafeToken(int bytes) {
        byte[] raw = new byte[bytes];
        SECURE_RANDOM.nextBytes(raw);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(raw);
    }

    // ── DTO mappers ─────────────────────────────────────────────────────────

    private UserSummaryResponse toStudentSummary(StudentUser s) {
        return UserSummaryResponse.builder()
                .userId(s.getId())
                .userType("student")
                .fullName(s.getFullName())
                .email(s.getEmail())
                .status(s.getStatus().getValue())
                .currentJlptLevel(
                        s.getCurrentJlptLevel() != null
                                ? s.getCurrentJlptLevel().name()
                                : null)
                .currentStreak(s.getCurrentStreak())
                .createdAt(s.getCreatedAt())
                .build();
    }

    private UserSummaryResponse toStaffSummary(StaffUser st) {
        return UserSummaryResponse.builder()
                .userId(st.getId())
                .userType("staff")
                .fullName(st.getFullName())
                .email(st.getEmail())
                .status(st.getStatus().getValue())
                .staffRole(st.getStaffRole().getValue())
                .createdAt(st.getCreatedAt())
                .build();
    }

    private UserSummaryResponse toAdminSummary(AdminUser a) {
        return UserSummaryResponse.builder()
                .userId(a.getId())
                .userType("admin")
                .fullName(a.getFullName())
                .email(a.getEmail())
                .status(a.getStatus().getValue())
                .createdAt(a.getCreatedAt())
                .build();
    }

    private StudentDetailResponse toStudentDetail(StudentUser s) {
        return StudentDetailResponse.builder()
                .studentId(s.getId())
                .fullName(s.getFullName())
                .email(s.getEmail())
                .phone(s.getPhone())
                .avatarUrl(s.getAvatarUrl())
                .status(s.getStatus().getValue())
                .suspendReason(s.getSuspendReason())
                .currentJlptLevel(
                        s.getCurrentJlptLevel() != null
                                ? s.getCurrentJlptLevel().name()
                                : null)
                .targetJlptLevel(
                        s.getTargetJlptLevel() != null ? s.getTargetJlptLevel().name() : null)
                .currentStreak(s.getCurrentStreak())
                .longestStreak(s.getLongestStreak())
                .lastLoginAt(s.getLastLoginAt())
                .createdAt(s.getCreatedAt())
                .build();
    }

    private StaffDetailResponse toStaffDetail(StaffUser st) {
        return StaffDetailResponse.builder()
                .staffId(st.getId())
                .fullName(st.getFullName())
                .email(st.getEmail())
                .staffRole(st.getStaffRole().getValue())
                .status(st.getStatus().getValue())
                .suspendReason(st.getSuspendReason())
                .lastLoginAt(st.getLastLoginAt())
                .createdAt(st.getCreatedAt())
                .build();
    }

    private AdminDetailResponse toAdminDetail(AdminUser a) {
        return AdminDetailResponse.builder()
                .adminId(a.getId())
                .fullName(a.getFullName())
                .email(a.getEmail())
                .status(a.getStatus().getValue())
                .lastLoginAt(a.getLastLoginAt())
                .createdAt(a.getCreatedAt())
                .build();
    }
}
