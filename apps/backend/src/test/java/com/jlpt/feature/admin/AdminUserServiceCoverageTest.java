/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.admin;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.*;

import com.jlpt.feature.admin.dto.request.SuspendUserRequest;
import com.jlpt.feature.admin.dto.response.ActivateUserResponse;
import com.jlpt.feature.admin.dto.response.AdminDetailResponse;
import com.jlpt.feature.admin.dto.response.RestoreUserResponse;
import com.jlpt.feature.admin.dto.response.SuspendUserResponse;
import com.jlpt.feature.admin.dto.response.UserSummaryResponse;
import com.jlpt.feature.auth.AuthToken;
import com.jlpt.feature.auth.AuthTokenRepository;
import com.jlpt.feature.staff.StaffUser;
import com.jlpt.feature.staff.StaffUserRepository;
import com.jlpt.feature.staff.dto.request.ChangeStaffRoleRequest;
import com.jlpt.feature.staff.dto.request.StaffSetupPasswordRequest;
import com.jlpt.feature.staff.dto.request.UpdateStaffInfoRequest;
import com.jlpt.feature.staff.dto.response.ChangeStaffRoleResponse;
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
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Bổ sung độ phủ cho AdminUserService: các nhánh chưa được {@link AdminUserServiceTest} chạm tới —
 * listUsers/getUserDetail theo cả 3 loại tài khoản, setupStaffPassword, updateUser, resetPassword,
 * và các nhánh staff/admin của suspend/activate/restore.
 */
@ExtendWith(MockitoExtension.class)
class AdminUserServiceCoverageTest {

    private static final String ADMIN_EMAIL = "admin@sakuji.com";
    private static final Long ADMIN_ID = 99L;
    private static final Long STUDENT_ID = 1L;
    private static final Long STAFF_ID = 2L;

    @Mock
    private StudentUserRepository studentUserRepository;

    @Mock
    private StaffUserRepository staffUserRepository;

    @Mock
    private AdminUserRepository adminUserRepository;

    @Mock
    private AuthTokenRepository authTokenRepository;

    @Mock
    private AdminAuditLogRepository auditLogRepository;

    @Mock
    private EmailService emailService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AdminUserService adminUserService;

    private AdminUser actingAdmin;
    private StudentUser student;
    private StaffUser staff;

    @BeforeEach
    void setUp() {
        actingAdmin = AdminUser.builder()
                .id(ADMIN_ID)
                .email(ADMIN_EMAIL)
                .fullName("Root Admin")
                .status(AdminUser.AdminStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .build();

        student = StudentUser.builder()
                .id(STUDENT_ID)
                .email("student@example.com")
                .fullName("Student One")
                .phone("0900000000")
                .avatarUrl("/uploads/avatar/1.png")
                .status(StudentUser.StudentStatus.ACTIVE)
                .currentJlptLevel(StudentUser.JlptLevel.N5)
                .targetJlptLevel(StudentUser.JlptLevel.N4)
                .currentStreak(3)
                .longestStreak(7)
                .createdAt(LocalDateTime.now())
                .build();

        staff = StaffUser.builder()
                .id(STAFF_ID)
                .email("staff@example.com")
                .fullName("Staff One")
                .staffRole(StaffUser.StaffRole.STAFF)
                .status(StaffUser.StaffStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .build();
    }

    /** resolveAdmin() chỉ được gọi ở các API ghi — stub riêng để không vướng strict-stubs. */
    private void stubActingAdmin() {
        when(adminUserRepository.findByEmail(ADMIN_EMAIL)).thenReturn(Optional.of(actingAdmin));
    }

    // ── listUsers ────────────────────────────────────────────────────────────

    @Test
    void listUsers_student_appliesFiltersAndMapsSummary() {
        when(studentUserRepository.findAllAdminFiltered(eq("%an%"), eq("active"), eq("N3"), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(student)));

        Page<UserSummaryResponse> result = adminUserService.listUsers("student", "an", "ACTIVE", "n3", null, 0, 20);

        assertEquals(1, result.getTotalElements());
        UserSummaryResponse row = result.getContent().get(0);
        assertEquals(STUDENT_ID, row.getUserId());
        assertEquals("student", row.getUserType());
        assertEquals("active", row.getStatus());
        assertEquals("N5", row.getCurrentJlptLevel());
        assertEquals(3, row.getCurrentStreak());
    }

    @Test
    void listUsers_studentWithoutFilters_passesNullsToRepository() {
        student.setCurrentJlptLevel(null);
        when(studentUserRepository.findAllAdminFiltered(isNull(), isNull(), isNull(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(student)));

        Page<UserSummaryResponse> result = adminUserService.listUsers("STUDENT", "  ", "", null, null, 0, 20);

        assertNull(result.getContent().get(0).getCurrentJlptLevel());
    }

    @Test
    void listUsers_staff_lowercasesStaffRoleFilter() {
        when(staffUserRepository.findAllAdminFiltered(isNull(), isNull(), eq("staff_manager"), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(staff)));

        Page<UserSummaryResponse> result =
                adminUserService.listUsers("staff", null, null, null, "STAFF_MANAGER", 0, 20);

        UserSummaryResponse row = result.getContent().get(0);
        assertEquals("staff", row.getUserType());
        assertEquals("staff", row.getStaffRole());
        assertEquals("staff@example.com", row.getEmail());
    }

    @Test
    void listUsers_staffWithoutRoleFilter_passesNull() {
        when(staffUserRepository.findAllAdminFiltered(isNull(), isNull(), isNull(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(staff)));

        assertEquals(
                1,
                adminUserService
                        .listUsers("staff", null, null, null, " ", 0, 20)
                        .getTotalElements());
    }

    @Test
    void listUsers_admin_mapsAdminSummary() {
        when(adminUserRepository.findAllAdminFiltered(isNull(), isNull(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(actingAdmin)));

        UserSummaryResponse row = adminUserService
                .listUsers("admin", null, null, null, null, 0, 20)
                .getContent()
                .get(0);

        assertEquals("admin", row.getUserType());
        assertEquals(ADMIN_ID, row.getUserId());
        assertEquals("active", row.getStatus());
    }

    @Test
    void listUsers_unknownType_throwsBadRequest() {
        assertThrows(
                BadRequestException.class, () -> adminUserService.listUsers("teacher", null, null, null, null, 0, 20));
    }

    @Test
    void listUsers_blankType_throwsBadRequest() {
        assertThrows(BadRequestException.class, () -> adminUserService.listUsers("  ", null, null, null, null, 0, 20));
    }

    // ── getUserDetail ────────────────────────────────────────────────────────

    @Test
    void getUserDetail_student_mapsAllFields() {
        when(studentUserRepository.findById(STUDENT_ID)).thenReturn(Optional.of(student));

        StudentDetailResponse detail = (StudentDetailResponse) adminUserService.getUserDetail("student", STUDENT_ID);

        assertEquals(STUDENT_ID, detail.getStudentId());
        assertEquals("student@example.com", detail.getEmail());
        assertEquals("0900000000", detail.getPhone());
        assertEquals("/uploads/avatar/1.png", detail.getAvatarUrl());
        assertEquals("N5", detail.getCurrentJlptLevel());
        assertEquals("N4", detail.getTargetJlptLevel());
        assertEquals(7, detail.getLongestStreak());
    }

    @Test
    void getUserDetail_studentWithoutLevels_mapsNulls() {
        student.setCurrentJlptLevel(null);
        student.setTargetJlptLevel(null);
        when(studentUserRepository.findById(STUDENT_ID)).thenReturn(Optional.of(student));

        StudentDetailResponse detail = (StudentDetailResponse) adminUserService.getUserDetail("student", STUDENT_ID);

        assertNull(detail.getCurrentJlptLevel());
        assertNull(detail.getTargetJlptLevel());
    }

    @Test
    void getUserDetail_studentMissing_throwsNotFound() {
        when(studentUserRepository.findById(STUDENT_ID)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> adminUserService.getUserDetail("student", STUDENT_ID));
    }

    @Test
    void getUserDetail_staff_mapsDetail() {
        when(staffUserRepository.findById(STAFF_ID)).thenReturn(Optional.of(staff));

        StaffDetailResponse detail = (StaffDetailResponse) adminUserService.getUserDetail("staff", STAFF_ID);

        assertEquals(STAFF_ID, detail.getStaffId());
        assertEquals("staff", detail.getStaffRole());
        assertEquals("active", detail.getStatus());
    }

    @Test
    void getUserDetail_staffMissing_throwsNotFound() {
        when(staffUserRepository.findById(STAFF_ID)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> adminUserService.getUserDetail("staff", STAFF_ID));
    }

    @Test
    void getUserDetail_admin_mapsDetail() {
        when(adminUserRepository.findById(ADMIN_ID)).thenReturn(Optional.of(actingAdmin));

        AdminDetailResponse detail = (AdminDetailResponse) adminUserService.getUserDetail("admin", ADMIN_ID);

        assertEquals(ADMIN_ID, detail.getAdminId());
        assertEquals(ADMIN_EMAIL, detail.getEmail());
        assertEquals("active", detail.getStatus());
    }

    @Test
    void getUserDetail_adminMissing_throwsNotFound() {
        when(adminUserRepository.findById(ADMIN_ID)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> adminUserService.getUserDetail("admin", ADMIN_ID));
    }

    @Test
    void getUserDetail_unknownType_throwsBadRequest() {
        assertThrows(BadRequestException.class, () -> adminUserService.getUserDetail("robot", 1L));
    }

    // ── setupStaffPassword ───────────────────────────────────────────────────

    private StaffSetupPasswordRequest setupRequest(String password, String confirm) {
        StaffSetupPasswordRequest request = new StaffSetupPasswordRequest();
        request.setToken("invite-token");
        request.setNewPassword(password);
        request.setConfirmPassword(confirm);
        return request;
    }

    private AuthToken inviteToken(LocalDateTime expiresAt) {
        return AuthToken.builder()
                .actorType(AuthToken.ActorType.STAFF)
                .staffId(STAFF_ID)
                .tokenType(AuthToken.TokenType.EMAIL_VERIFICATION)
                .tokenValue("invite-token")
                .expiresAt(expiresAt)
                .build();
    }

    @Test
    void setupStaffPassword_confirmMismatch_throwsPasswordMismatch() {
        BusinessException ex = assertThrows(
                BusinessException.class,
                () -> adminUserService.setupStaffPassword(setupRequest("Abc@123456", "Different@1")));

        assertEquals("PASSWORD_MISMATCH", ex.getErrorCode());
        verifyNoInteractions(authTokenRepository);
    }

    @Test
    void setupStaffPassword_tokenNotFound_throwsInvalidToken() {
        when(authTokenRepository.findByTokenValueAndTokenType("invite-token", AuthToken.TokenType.EMAIL_VERIFICATION))
                .thenReturn(Optional.empty());

        BusinessException ex = assertThrows(
                BusinessException.class,
                () -> adminUserService.setupStaffPassword(setupRequest("Abc@123456", "Abc@123456")));

        assertEquals("INVALID_TOKEN", ex.getErrorCode());
    }

    @Test
    void setupStaffPassword_tokenOfAnotherActor_throwsInvalidToken() {
        AuthToken token = inviteToken(LocalDateTime.now().plusHours(1));
        token.setActorType(AuthToken.ActorType.STUDENT);
        when(authTokenRepository.findByTokenValueAndTokenType("invite-token", AuthToken.TokenType.EMAIL_VERIFICATION))
                .thenReturn(Optional.of(token));

        BusinessException ex = assertThrows(
                BusinessException.class,
                () -> adminUserService.setupStaffPassword(setupRequest("Abc@123456", "Abc@123456")));

        assertEquals("INVALID_TOKEN", ex.getErrorCode());
    }

    @Test
    void setupStaffPassword_expiredToken_throwsTokenExpired() {
        when(authTokenRepository.findByTokenValueAndTokenType("invite-token", AuthToken.TokenType.EMAIL_VERIFICATION))
                .thenReturn(Optional.of(inviteToken(LocalDateTime.now().minusMinutes(1))));

        BusinessException ex = assertThrows(
                BusinessException.class,
                () -> adminUserService.setupStaffPassword(setupRequest("Abc@123456", "Abc@123456")));

        assertEquals("TOKEN_EXPIRED", ex.getErrorCode());
    }

    @Test
    void setupStaffPassword_staffMissing_throwsUserNotFound() {
        when(authTokenRepository.findByTokenValueAndTokenType("invite-token", AuthToken.TokenType.EMAIL_VERIFICATION))
                .thenReturn(Optional.of(inviteToken(LocalDateTime.now().plusHours(1))));
        when(staffUserRepository.findById(STAFF_ID)).thenReturn(Optional.empty());

        BusinessException ex = assertThrows(
                BusinessException.class,
                () -> adminUserService.setupStaffPassword(setupRequest("Abc@123456", "Abc@123456")));

        assertEquals("USER_NOT_FOUND", ex.getErrorCode());
    }

    @Test
    void setupStaffPassword_alreadyActivated_throwsAlreadyActivated() {
        when(authTokenRepository.findByTokenValueAndTokenType("invite-token", AuthToken.TokenType.EMAIL_VERIFICATION))
                .thenReturn(Optional.of(inviteToken(LocalDateTime.now().plusHours(1))));
        when(staffUserRepository.findById(STAFF_ID)).thenReturn(Optional.of(staff));

        BusinessException ex = assertThrows(
                BusinessException.class,
                () -> adminUserService.setupStaffPassword(setupRequest("Abc@123456", "Abc@123456")));

        assertEquals("ALREADY_ACTIVATED", ex.getErrorCode());
        verify(staffUserRepository, never()).save(any());
    }

    @Test
    void setupStaffPassword_pendingStaff_activatesAndConsumesToken() {
        staff.setStatus(StaffUser.StaffStatus.PENDING);
        AuthToken token = inviteToken(LocalDateTime.now().plusHours(1));
        when(authTokenRepository.findByTokenValueAndTokenType("invite-token", AuthToken.TokenType.EMAIL_VERIFICATION))
                .thenReturn(Optional.of(token));
        when(staffUserRepository.findById(STAFF_ID)).thenReturn(Optional.of(staff));
        when(passwordEncoder.encode("Abc@123456")).thenReturn("hashed");

        adminUserService.setupStaffPassword(setupRequest("Abc@123456", "Abc@123456"));

        assertEquals("hashed", staff.getPasswordHash());
        assertEquals(StaffUser.StaffStatus.ACTIVE, staff.getStatus());
        verify(staffUserRepository).save(staff);
        verify(authTokenRepository).delete(token);
    }

    // ── updateUser ───────────────────────────────────────────────────────────

    @Test
    void updateUser_student_updatesAllProvidedFieldsAndAudits() {
        stubActingAdmin();
        when(studentUserRepository.findById(STUDENT_ID)).thenReturn(Optional.of(student));
        when(studentUserRepository.save(student)).thenReturn(student);
        UpdateStudentRequest request = new UpdateStudentRequest();
        request.setFullName("  Nguyễn Văn A  ");
        request.setPhone("  0911222333  ");
        request.setTargetJlptLevel("n2");

        StudentDetailResponse detail =
                (StudentDetailResponse) adminUserService.updateUser(ADMIN_EMAIL, "student", STUDENT_ID, request);

        assertEquals("Nguyễn Văn A", detail.getFullName());
        assertEquals("0911222333", student.getPhone());
        assertEquals(StudentUser.JlptLevel.N2, student.getTargetJlptLevel());

        ArgumentCaptor<AdminAuditLog> logCaptor = ArgumentCaptor.forClass(AdminAuditLog.class);
        verify(auditLogRepository).save(logCaptor.capture());
        assertTrue(logCaptor.getValue().getDescription().contains("fullName: "));
    }

    @Test
    void updateUser_studentBlankPhone_clearsPhone() {
        stubActingAdmin();
        when(studentUserRepository.findById(STUDENT_ID)).thenReturn(Optional.of(student));
        when(studentUserRepository.save(student)).thenReturn(student);
        UpdateStudentRequest request = new UpdateStudentRequest();
        request.setPhone("   ");

        adminUserService.updateUser(ADMIN_EMAIL, "student", STUDENT_ID, request);

        assertNull(student.getPhone());
    }

    @Test
    void updateUser_studentEmptyRequest_keepsExistingValues() {
        stubActingAdmin();
        when(studentUserRepository.findById(STUDENT_ID)).thenReturn(Optional.of(student));
        when(studentUserRepository.save(student)).thenReturn(student);

        adminUserService.updateUser(ADMIN_EMAIL, "student", STUDENT_ID, new UpdateStudentRequest());

        assertEquals("Student One", student.getFullName());
        assertEquals("0900000000", student.getPhone());
        assertEquals(StudentUser.JlptLevel.N4, student.getTargetJlptLevel());
    }

    @Test
    void updateUser_studentMissing_throwsNotFound() {
        stubActingAdmin();
        when(studentUserRepository.findById(STUDENT_ID)).thenReturn(Optional.empty());
        UpdateStudentRequest request = new UpdateStudentRequest();

        assertThrows(
                ResourceNotFoundException.class,
                () -> adminUserService.updateUser(ADMIN_EMAIL, "student", STUDENT_ID, request));
    }

    @Test
    void updateUser_staff_trimsFullName() {
        stubActingAdmin();
        when(staffUserRepository.findById(STAFF_ID)).thenReturn(Optional.of(staff));
        when(staffUserRepository.save(staff)).thenReturn(staff);
        UpdateStaffInfoRequest request = new UpdateStaffInfoRequest();
        request.setFullName("  Trần Thị B  ");

        StaffDetailResponse detail =
                (StaffDetailResponse) adminUserService.updateUser(ADMIN_EMAIL, "staff", STAFF_ID, request);

        assertEquals("Trần Thị B", detail.getFullName());
        verify(auditLogRepository).save(any(AdminAuditLog.class));
    }

    @Test
    void updateUser_staffBlankName_keepsExistingName() {
        stubActingAdmin();
        when(staffUserRepository.findById(STAFF_ID)).thenReturn(Optional.of(staff));
        when(staffUserRepository.save(staff)).thenReturn(staff);

        adminUserService.updateUser(ADMIN_EMAIL, "staff", STAFF_ID, new UpdateStaffInfoRequest());

        assertEquals("Staff One", staff.getFullName());
    }

    @Test
    void updateUser_staffMissing_throwsNotFound() {
        stubActingAdmin();
        when(staffUserRepository.findById(STAFF_ID)).thenReturn(Optional.empty());
        UpdateStaffInfoRequest request = new UpdateStaffInfoRequest();

        assertThrows(
                ResourceNotFoundException.class,
                () -> adminUserService.updateUser(ADMIN_EMAIL, "staff", STAFF_ID, request));
    }

    @Test
    void updateUser_otherAdminAccount_throwsForbidden() {
        stubActingAdmin();

        assertThrows(
                ForbiddenException.class, () -> adminUserService.updateUser(ADMIN_EMAIL, "admin", 100L, new Object()));
    }

    @Test
    void updateUser_selfAdminAccount_throwsForbidden() {
        stubActingAdmin();

        assertThrows(
                ForbiddenException.class,
                () -> adminUserService.updateUser(ADMIN_EMAIL, "admin", ADMIN_ID, new Object()));
    }

    @Test
    void updateUser_unknownType_throwsBadRequest() {
        stubActingAdmin();

        assertThrows(
                BadRequestException.class, () -> adminUserService.updateUser(ADMIN_EMAIL, "robot", 1L, new Object()));
    }

    @Test
    void updateUser_actingAdminMissing_throwsNotFound() {
        when(adminUserRepository.findByEmail("ghost@sakuji.com")).thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> adminUserService.updateUser("ghost@sakuji.com", "student", STUDENT_ID, new Object()));
    }

    // ── suspend/activate/restore: nhánh staff & admin ────────────────────────

    @Test
    void suspendUser_staff_revokesTokens() {
        stubActingAdmin();
        when(staffUserRepository.findById(STAFF_ID)).thenReturn(Optional.of(staff));
        SuspendUserRequest request = new SuspendUserRequest();
        request.setReason("Nghỉ việc, khoá tài khoản");

        SuspendUserResponse response = adminUserService.suspendUser(ADMIN_EMAIL, "staff", STAFF_ID, request);

        assertEquals("staff", response.getUserType());
        assertEquals(StaffUser.StaffStatus.SUSPENDED, staff.getStatus());
        verify(authTokenRepository).revokeAllActiveByStaffId(eq(STAFF_ID), any());
    }

    @Test
    void suspendUser_staffAlreadyDeleted_throwsDuplicate() {
        stubActingAdmin();
        staff.setStatus(StaffUser.StaffStatus.DELETED);
        when(staffUserRepository.findById(STAFF_ID)).thenReturn(Optional.of(staff));
        SuspendUserRequest request = new SuspendUserRequest();
        request.setReason("Nghỉ việc, khoá tài khoản");

        assertThrows(
                DuplicateResourceException.class,
                () -> adminUserService.suspendUser(ADMIN_EMAIL, "staff", STAFF_ID, request));
    }

    @Test
    void suspendUser_otherAdmin_revokesTokens() {
        stubActingAdmin();
        AdminUser target = AdminUser.builder()
                .id(100L)
                .email("other@sakuji.com")
                .status(AdminUser.AdminStatus.ACTIVE)
                .build();
        when(adminUserRepository.findById(100L)).thenReturn(Optional.of(target));
        SuspendUserRequest request = new SuspendUserRequest();
        request.setReason("Tạm khoá theo yêu cầu");

        SuspendUserResponse response = adminUserService.suspendUser(ADMIN_EMAIL, "admin", 100L, request);

        assertEquals("admin", response.getUserType());
        assertEquals(AdminUser.AdminStatus.SUSPENDED, target.getStatus());
        verify(authTokenRepository).revokeAllActiveByAdminId(eq(100L), any());
    }

    @Test
    void suspendUser_adminAlreadySuspended_throwsDuplicate() {
        stubActingAdmin();
        AdminUser target = AdminUser.builder()
                .id(100L)
                .status(AdminUser.AdminStatus.SUSPENDED)
                .build();
        when(adminUserRepository.findById(100L)).thenReturn(Optional.of(target));
        SuspendUserRequest request = new SuspendUserRequest();
        request.setReason("Tạm khoá theo yêu cầu");

        assertThrows(
                DuplicateResourceException.class,
                () -> adminUserService.suspendUser(ADMIN_EMAIL, "admin", 100L, request));
    }

    @Test
    void suspendUser_unknownType_throwsBadRequest() {
        stubActingAdmin();
        SuspendUserRequest request = new SuspendUserRequest();
        request.setReason("Tạm khoá theo yêu cầu");

        assertThrows(BadRequestException.class, () -> adminUserService.suspendUser(ADMIN_EMAIL, "robot", 1L, request));
    }

    @Test
    void activateUser_staff_clearsSuspendReason() {
        stubActingAdmin();
        staff.setStatus(StaffUser.StaffStatus.SUSPENDED);
        staff.setSuspendReason("cũ");
        when(staffUserRepository.findById(STAFF_ID)).thenReturn(Optional.of(staff));

        ActivateUserResponse response = adminUserService.activateUser(ADMIN_EMAIL, "staff", STAFF_ID);

        assertEquals("staff", response.getUserType());
        assertEquals(StaffUser.StaffStatus.ACTIVE, staff.getStatus());
        assertNull(staff.getSuspendReason());
    }

    @Test
    void activateUser_staffNotSuspended_throwsDuplicate() {
        stubActingAdmin();
        when(staffUserRepository.findById(STAFF_ID)).thenReturn(Optional.of(staff));

        assertThrows(
                DuplicateResourceException.class, () -> adminUserService.activateUser(ADMIN_EMAIL, "staff", STAFF_ID));
    }

    @Test
    void activateUser_otherAdmin_setsActive() {
        stubActingAdmin();
        AdminUser target = AdminUser.builder()
                .id(100L)
                .status(AdminUser.AdminStatus.SUSPENDED)
                .suspendReason("cũ")
                .build();
        when(adminUserRepository.findById(100L)).thenReturn(Optional.of(target));

        ActivateUserResponse response = adminUserService.activateUser(ADMIN_EMAIL, "admin", 100L);

        assertEquals("admin", response.getUserType());
        assertEquals(AdminUser.AdminStatus.ACTIVE, target.getStatus());
        assertNull(target.getSuspendReason());
    }

    @Test
    void activateUser_adminNotSuspended_throwsDuplicate() {
        stubActingAdmin();
        AdminUser target = AdminUser.builder()
                .id(100L)
                .status(AdminUser.AdminStatus.ACTIVE)
                .build();
        when(adminUserRepository.findById(100L)).thenReturn(Optional.of(target));

        assertThrows(DuplicateResourceException.class, () -> adminUserService.activateUser(ADMIN_EMAIL, "admin", 100L));
    }

    @Test
    void activateUser_studentMissing_throwsNotFound() {
        stubActingAdmin();
        when(studentUserRepository.findById(STUDENT_ID)).thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> adminUserService.activateUser(ADMIN_EMAIL, "student", STUDENT_ID));
    }

    @Test
    void activateUser_unknownType_throwsBadRequest() {
        stubActingAdmin();

        assertThrows(BadRequestException.class, () -> adminUserService.activateUser(ADMIN_EMAIL, "robot", 1L));
    }

    @Test
    void softDeleteUser_studentMissing_throwsNotFound() {
        stubActingAdmin();
        when(studentUserRepository.findByIdIncludingDeleted(STUDENT_ID)).thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> adminUserService.softDeleteUser(ADMIN_EMAIL, "student", STUDENT_ID));
    }

    @Test
    void softDeleteUser_unknownType_throwsBadRequest() {
        stubActingAdmin();

        assertThrows(BadRequestException.class, () -> adminUserService.softDeleteUser(ADMIN_EMAIL, "robot", 1L));
    }

    @Test
    void restoreUser_staff_setsActive() {
        stubActingAdmin();
        staff.setStatus(StaffUser.StaffStatus.DELETED);
        when(staffUserRepository.findByIdIncludingDeleted(STAFF_ID)).thenReturn(Optional.of(staff));

        RestoreUserResponse response = adminUserService.restoreUser(ADMIN_EMAIL, "staff", STAFF_ID);

        assertEquals("staff", response.getUserType());
        assertEquals(StaffUser.StaffStatus.ACTIVE, staff.getStatus());
        verify(auditLogRepository).save(any(AdminAuditLog.class));
    }

    @Test
    void restoreUser_staffNotDeleted_throwsBusinessRule() {
        stubActingAdmin();
        when(staffUserRepository.findByIdIncludingDeleted(STAFF_ID)).thenReturn(Optional.of(staff));

        assertThrows(BusinessRuleException.class, () -> adminUserService.restoreUser(ADMIN_EMAIL, "staff", STAFF_ID));
    }

    @Test
    void restoreUser_studentMissing_throwsNotFound() {
        stubActingAdmin();
        when(studentUserRepository.findByIdIncludingDeleted(STUDENT_ID)).thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> adminUserService.restoreUser(ADMIN_EMAIL, "student", STUDENT_ID));
    }

    @Test
    void restoreUser_adminType_throwsBusinessRule() {
        stubActingAdmin();

        assertThrows(BusinessRuleException.class, () -> adminUserService.restoreUser(ADMIN_EMAIL, "admin", 100L));
    }

    @Test
    void restoreUser_unknownType_throwsBadRequest() {
        stubActingAdmin();

        assertThrows(BadRequestException.class, () -> adminUserService.restoreUser(ADMIN_EMAIL, "robot", 1L));
    }

    // ── resetPassword ────────────────────────────────────────────────────────

    @Test
    void resetPassword_student_revokesTokensAndSendsEmail() {
        stubActingAdmin();
        when(studentUserRepository.findById(STUDENT_ID)).thenReturn(Optional.of(student));

        adminUserService.resetPassword(ADMIN_EMAIL, "student", STUDENT_ID);

        verify(authTokenRepository).revokeAllActiveByStudentId(eq(STUDENT_ID), any());
        ArgumentCaptor<AuthToken> tokenCaptor = ArgumentCaptor.forClass(AuthToken.class);
        verify(authTokenRepository).save(tokenCaptor.capture());
        AuthToken saved = tokenCaptor.getValue();
        assertEquals(AuthToken.ActorType.STUDENT, saved.getActorType());
        assertEquals(AuthToken.TokenType.PASSWORD_RESET, saved.getTokenType());
        assertEquals(STUDENT_ID, saved.getStudentId());
        assertNotNull(saved.getTokenValue());
        verify(emailService).sendPasswordResetEmail(eq("student@example.com"), eq(saved.getTokenValue()));
        verify(auditLogRepository).save(any(AdminAuditLog.class));
    }

    @Test
    void resetPassword_deletedStudent_throwsNotFound() {
        stubActingAdmin();
        student.setStatus(StudentUser.StudentStatus.DELETED);
        when(studentUserRepository.findById(STUDENT_ID)).thenReturn(Optional.of(student));

        assertThrows(
                ResourceNotFoundException.class,
                () -> adminUserService.resetPassword(ADMIN_EMAIL, "student", STUDENT_ID));
        verifyNoInteractions(emailService);
    }

    @Test
    void resetPassword_studentMissing_throwsNotFound() {
        stubActingAdmin();
        when(studentUserRepository.findById(STUDENT_ID)).thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> adminUserService.resetPassword(ADMIN_EMAIL, "student", STUDENT_ID));
    }

    @Test
    void resetPassword_staff_savesStaffToken() {
        stubActingAdmin();
        when(staffUserRepository.findById(STAFF_ID)).thenReturn(Optional.of(staff));

        adminUserService.resetPassword(ADMIN_EMAIL, "staff", STAFF_ID);

        verify(authTokenRepository).revokeAllActiveByStaffId(eq(STAFF_ID), any());
        ArgumentCaptor<AuthToken> tokenCaptor = ArgumentCaptor.forClass(AuthToken.class);
        verify(authTokenRepository).save(tokenCaptor.capture());
        assertEquals(AuthToken.ActorType.STAFF, tokenCaptor.getValue().getActorType());
        assertEquals(STAFF_ID, tokenCaptor.getValue().getStaffId());
        verify(emailService).sendPasswordResetEmail(eq("staff@example.com"), any());
    }

    @Test
    void resetPassword_deletedStaff_throwsNotFound() {
        stubActingAdmin();
        staff.setStatus(StaffUser.StaffStatus.DELETED);
        when(staffUserRepository.findById(STAFF_ID)).thenReturn(Optional.of(staff));

        assertThrows(
                ResourceNotFoundException.class, () -> adminUserService.resetPassword(ADMIN_EMAIL, "staff", STAFF_ID));
    }

    @Test
    void resetPassword_admin_savesAdminToken() {
        stubActingAdmin();
        when(adminUserRepository.findById(ADMIN_ID)).thenReturn(Optional.of(actingAdmin));

        adminUserService.resetPassword(ADMIN_EMAIL, "admin", ADMIN_ID);

        verify(authTokenRepository).revokeAllActiveByAdminId(eq(ADMIN_ID), any());
        ArgumentCaptor<AuthToken> tokenCaptor = ArgumentCaptor.forClass(AuthToken.class);
        verify(authTokenRepository).save(tokenCaptor.capture());
        assertEquals(AuthToken.ActorType.ADMIN, tokenCaptor.getValue().getActorType());
        assertEquals(ADMIN_ID, tokenCaptor.getValue().getAdminId());
        verify(emailService).sendPasswordResetEmail(eq(ADMIN_EMAIL), any());
    }

    @Test
    void resetPassword_deletedAdmin_throwsNotFound() {
        stubActingAdmin();
        actingAdmin.setStatus(AdminUser.AdminStatus.DELETED);
        when(adminUserRepository.findById(ADMIN_ID)).thenReturn(Optional.of(actingAdmin));

        assertThrows(
                ResourceNotFoundException.class, () -> adminUserService.resetPassword(ADMIN_EMAIL, "admin", ADMIN_ID));
    }

    @Test
    void resetPassword_adminMissing_throwsNotFound() {
        stubActingAdmin();
        when(adminUserRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> adminUserService.resetPassword(ADMIN_EMAIL, "admin", 100L));
    }

    @Test
    void resetPassword_unknownType_throwsBadRequest() {
        stubActingAdmin();

        assertThrows(BadRequestException.class, () -> adminUserService.resetPassword(ADMIN_EMAIL, "robot", 1L));
    }

    // ── changeStaffRole ──────────────────────────────────────────────────────

    @Test
    void changeStaffRole_promotesToManagerAndAudits() {
        stubActingAdmin();
        when(staffUserRepository.findById(STAFF_ID)).thenReturn(Optional.of(staff));
        ChangeStaffRoleRequest request = new ChangeStaffRoleRequest();
        request.setStaffRole("staff_manager");

        ChangeStaffRoleResponse response = adminUserService.changeStaffRole(ADMIN_EMAIL, STAFF_ID, request);

        assertEquals("staff", response.getOldRole());
        assertEquals("staff_manager", response.getNewRole());
        assertEquals(StaffUser.StaffRole.STAFF_MANAGER, staff.getStaffRole());
        verify(staffUserRepository).save(staff);

        ArgumentCaptor<AdminAuditLog> logCaptor = ArgumentCaptor.forClass(AdminAuditLog.class);
        verify(auditLogRepository).save(logCaptor.capture());
        assertEquals("change_staff_role", logCaptor.getValue().getAction());
    }

    @Test
    void changeStaffRole_staffMissing_throwsNotFound() {
        stubActingAdmin();
        when(staffUserRepository.findById(STAFF_ID)).thenReturn(Optional.empty());
        ChangeStaffRoleRequest request = new ChangeStaffRoleRequest();
        request.setStaffRole("staff_manager");

        assertThrows(
                ResourceNotFoundException.class,
                () -> adminUserService.changeStaffRole(ADMIN_EMAIL, STAFF_ID, request));
    }
}
