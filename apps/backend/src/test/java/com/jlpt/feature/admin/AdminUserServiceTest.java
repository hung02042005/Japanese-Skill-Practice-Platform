/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.admin;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.jlpt.feature.admin.dto.request.SuspendUserRequest;
import com.jlpt.feature.admin.dto.response.ActivateUserResponse;
import com.jlpt.feature.admin.dto.response.RestoreUserResponse;
import com.jlpt.feature.admin.dto.response.SoftDeleteUserResponse;
import com.jlpt.feature.admin.dto.response.SuspendUserResponse;
import com.jlpt.feature.auth.AuthTokenRepository;
import com.jlpt.feature.staff.StaffUser;
import com.jlpt.feature.staff.StaffUserRepository;
import com.jlpt.feature.staff.dto.request.CreateStaffRequest;
import com.jlpt.feature.staff.dto.response.CreateStaffResponse;
import com.jlpt.feature.student.StudentUser;
import com.jlpt.feature.student.StudentUserRepository;
import com.jlpt.shared.email.EmailService;
import com.jlpt.shared.exception.BusinessRuleException;
import com.jlpt.shared.exception.DuplicateResourceException;
import com.jlpt.shared.exception.ForbiddenException;
import com.jlpt.shared.exception.ResourceNotFoundException;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Test các thao tác nhạy cảm nhất của AdminUserService: suspend/activate/soft-delete/restore
 * user và tạo tài khoản Staff — bao gồm quy tắc BR-37-01 (admin không được tự sửa/xoá chính mình).
 */
@ExtendWith(MockitoExtension.class)
class AdminUserServiceTest {

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
    private StudentUser activeStudent;
    private StaffUser activeStaff;

    @BeforeEach
    void setUp() {
        actingAdmin = AdminUser.builder()
                .id(ADMIN_ID)
                .email(ADMIN_EMAIL)
                .fullName("Root Admin")
                .status(AdminUser.AdminStatus.ACTIVE)
                .build();
        lenientStub();

        activeStudent = StudentUser.builder()
                .id(STUDENT_ID)
                .email("student@example.com")
                .fullName("Student One")
                .status(StudentUser.StudentStatus.ACTIVE)
                .build();

        activeStaff = StaffUser.builder()
                .id(STAFF_ID)
                .email("staff@example.com")
                .fullName("Staff One")
                .staffRole(StaffUser.StaffRole.STAFF)
                .status(StaffUser.StaffStatus.ACTIVE)
                .build();
    }

    private void lenientStub() {
        when(adminUserRepository.findByEmail(ADMIN_EMAIL)).thenReturn(Optional.of(actingAdmin));
    }

    // ── suspendUser ──────────────────────────────────────────────────────────

    @Test
    void suspendUser_Student_Success() {
        when(studentUserRepository.findById(STUDENT_ID)).thenReturn(Optional.of(activeStudent));
        SuspendUserRequest request = new SuspendUserRequest();
        request.setReason("Vi phạm điều khoản sử dụng nhiều lần");

        SuspendUserResponse response = adminUserService.suspendUser(ADMIN_EMAIL, "student", STUDENT_ID, request);

        assertEquals("suspended", response.getStatus());
        assertEquals(StudentUser.StudentStatus.SUSPENDED, activeStudent.getStatus());
        assertEquals(request.getReason(), activeStudent.getSuspendReason());
        verify(studentUserRepository).save(activeStudent);
        verify(authTokenRepository).revokeAllActiveByStudentId(eq(STUDENT_ID), any());
        verify(auditLogRepository).save(any(AdminAuditLog.class));
    }

    @Test
    void suspendUser_AlreadySuspended_ThrowsDuplicateResource() {
        activeStudent.setStatus(StudentUser.StudentStatus.SUSPENDED);
        when(studentUserRepository.findById(STUDENT_ID)).thenReturn(Optional.of(activeStudent));
        SuspendUserRequest request = new SuspendUserRequest();
        request.setReason("Vi phạm điều khoản sử dụng nhiều lần");

        assertThrows(
                DuplicateResourceException.class,
                () -> adminUserService.suspendUser(ADMIN_EMAIL, "student", STUDENT_ID, request));
        verify(studentUserRepository, never()).save(any());
    }

    @Test
    void suspendUser_UserNotFound_ThrowsResourceNotFound() {
        when(studentUserRepository.findById(STUDENT_ID)).thenReturn(Optional.empty());
        SuspendUserRequest request = new SuspendUserRequest();
        request.setReason("Vi phạm điều khoản sử dụng nhiều lần");

        assertThrows(
                ResourceNotFoundException.class,
                () -> adminUserService.suspendUser(ADMIN_EMAIL, "student", STUDENT_ID, request));
    }

    @Test
    void suspendUser_AdminCannotSuspendSelf_ThrowsForbidden() {
        SuspendUserRequest request = new SuspendUserRequest();
        request.setReason("Vi phạm điều khoản sử dụng nhiều lần");

        assertThrows(
                ForbiddenException.class, () -> adminUserService.suspendUser(ADMIN_EMAIL, "admin", ADMIN_ID, request));
        verify(adminUserRepository, never()).save(any());
    }

    // ── activateUser ─────────────────────────────────────────────────────────

    @Test
    void activateUser_Student_Success() {
        activeStudent.setStatus(StudentUser.StudentStatus.SUSPENDED);
        activeStudent.setSuspendReason("some reason");
        when(studentUserRepository.findById(STUDENT_ID)).thenReturn(Optional.of(activeStudent));

        ActivateUserResponse response = adminUserService.activateUser(ADMIN_EMAIL, "student", STUDENT_ID);

        assertEquals("active", response.getStatus());
        assertEquals(StudentUser.StudentStatus.ACTIVE, activeStudent.getStatus());
        assertNull(activeStudent.getSuspendReason());
        verify(studentUserRepository).save(activeStudent);
    }

    @Test
    void activateUser_NotSuspended_ThrowsDuplicateResource() {
        when(studentUserRepository.findById(STUDENT_ID)).thenReturn(Optional.of(activeStudent));

        assertThrows(
                DuplicateResourceException.class,
                () -> adminUserService.activateUser(ADMIN_EMAIL, "student", STUDENT_ID));
    }

    // ── softDeleteUser ───────────────────────────────────────────────────────

    @Test
    void softDeleteUser_Staff_Success() {
        when(staffUserRepository.findByIdIncludingDeleted(STAFF_ID)).thenReturn(Optional.of(activeStaff));

        SoftDeleteUserResponse response = adminUserService.softDeleteUser(ADMIN_EMAIL, "staff", STAFF_ID);

        assertEquals("deleted", response.getStatus());
        assertEquals(StaffUser.StaffStatus.DELETED, activeStaff.getStatus());
        verify(staffUserRepository).save(activeStaff);
        verify(authTokenRepository).revokeAllActiveByStaffId(eq(STAFF_ID), any());
    }

    @Test
    void softDeleteUser_AlreadyDeleted_ThrowsDuplicateResource() {
        activeStaff.setStatus(StaffUser.StaffStatus.DELETED);
        when(staffUserRepository.findByIdIncludingDeleted(STAFF_ID)).thenReturn(Optional.of(activeStaff));

        assertThrows(
                DuplicateResourceException.class,
                () -> adminUserService.softDeleteUser(ADMIN_EMAIL, "staff", STAFF_ID));
    }

    @Test
    void softDeleteUser_AdminType_ThrowsBusinessRule() {
        assertThrows(BusinessRuleException.class, () -> adminUserService.softDeleteUser(ADMIN_EMAIL, "admin", 12345L));
    }

    // ── restoreUser ──────────────────────────────────────────────────────────

    @Test
    void restoreUser_Student_Success() {
        activeStudent.setStatus(StudentUser.StudentStatus.DELETED);
        when(studentUserRepository.findByIdIncludingDeleted(STUDENT_ID)).thenReturn(Optional.of(activeStudent));

        RestoreUserResponse response = adminUserService.restoreUser(ADMIN_EMAIL, "student", STUDENT_ID);

        assertEquals("active", response.getStatus());
        assertEquals(StudentUser.StudentStatus.ACTIVE, activeStudent.getStatus());
        verify(studentUserRepository).save(activeStudent);
    }

    @Test
    void restoreUser_NotDeleted_ThrowsBusinessRule() {
        when(studentUserRepository.findByIdIncludingDeleted(STUDENT_ID)).thenReturn(Optional.of(activeStudent));

        assertThrows(
                BusinessRuleException.class, () -> adminUserService.restoreUser(ADMIN_EMAIL, "student", STUDENT_ID));
    }

    // ── createStaff ──────────────────────────────────────────────────────────

    @Test
    void createStaff_Success_SendsInvitationEmail() {
        CreateStaffRequest request = new CreateStaffRequest();
        request.setFullName("New Staff");
        request.setEmail("new.staff@example.com");
        request.setStaffRole("staff");

        when(studentUserRepository.existsByEmail("new.staff@example.com")).thenReturn(false);
        when(staffUserRepository.existsByEmail("new.staff@example.com")).thenReturn(false);
        when(adminUserRepository.existsByEmail("new.staff@example.com")).thenReturn(false);
        when(staffUserRepository.save(any(StaffUser.class))).thenAnswer(invocation -> {
            StaffUser s = invocation.getArgument(0);
            s.setId(STAFF_ID);
            return s;
        });

        CreateStaffResponse response = adminUserService.createStaff(ADMIN_EMAIL, request);

        assertEquals(STAFF_ID, response.getStaffId());
        assertEquals("pending", response.getStatus());
        verify(emailService).sendStaffInvitationEmail(eq("new.staff@example.com"), any());
        verify(auditLogRepository).save(any(AdminAuditLog.class));
    }

    @Test
    void createStaff_DuplicateEmail_ThrowsDuplicateResource() {
        CreateStaffRequest request = new CreateStaffRequest();
        request.setFullName("New Staff");
        request.setEmail("existing@example.com");
        request.setStaffRole("staff");

        when(staffUserRepository.existsByEmail("existing@example.com")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> adminUserService.createStaff(ADMIN_EMAIL, request));
        verify(staffUserRepository, never()).save(any());
        verify(emailService, never()).sendStaffInvitationEmail(any(), any());
    }
}
