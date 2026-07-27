/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.admin;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.jlpt.feature.admin.dto.request.SuspendUserRequest;
import com.jlpt.feature.admin.dto.response.ActivateUserResponse;
import com.jlpt.feature.admin.dto.response.RestoreUserResponse;
import com.jlpt.feature.admin.dto.response.SoftDeleteUserResponse;
import com.jlpt.feature.admin.dto.response.SuspendUserResponse;
import com.jlpt.feature.admin.dto.response.UserSummaryResponse;
import com.jlpt.feature.auth.dto.request.IssueTempPasswordRequest;
import com.jlpt.feature.auth.dto.response.IssueTempPasswordResponse;
import com.jlpt.feature.auth.dto.response.StaffResetRequestResponse;
import com.jlpt.feature.staff.StaffPasswordResetService;
import com.jlpt.feature.staff.dto.request.ChangeStaffRoleRequest;
import com.jlpt.feature.staff.dto.request.CreateStaffRequest;
import com.jlpt.feature.staff.dto.request.UpdateStaffInfoRequest;
import com.jlpt.feature.staff.dto.response.ChangeStaffRoleResponse;
import com.jlpt.feature.staff.dto.response.CreateStaffResponse;
import com.jlpt.feature.staff.dto.response.StaffDetailResponse;
import com.jlpt.feature.student.dto.request.UpdateStudentRequest;
import com.jlpt.feature.student.dto.response.StudentDetailResponse;
import com.jlpt.shared.common.ApiResponse;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

/**
 * UC-37 — AdminController chỉ điều phối: mỗi endpoint phải gọi đúng service với email admin đang
 * đăng nhập, và bọc kết quả vào ApiResponse với HTTP status đúng.
 */
@ExtendWith(MockitoExtension.class)
class AdminControllerTest {

    private static final String ADMIN_EMAIL = "admin@sakuji.com";

    @Mock
    private AdminUserService adminUserService;

    @Mock
    private StaffPasswordResetService staffPasswordResetService;

    @Mock
    private Authentication auth;

    @InjectMocks
    private AdminController controller;

    @BeforeEach
    void setUp() {
        lenient().when(auth.getName()).thenReturn(ADMIN_EMAIL);
    }

    @Test
    void listUsers_wrapsPageIntoContentAndTotals() {
        UserSummaryResponse row =
                UserSummaryResponse.builder().userId(1L).userType("student").build();
        when(adminUserService.listUsers("student", "an", "active", "N5", null, 0, 20))
                .thenReturn(new PageImpl<>(List.of(row)));

        ResponseEntity<ApiResponse<Map<String, Object>>> response =
                controller.listUsers("student", "an", "active", "N5", null, 0, 20);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        Map<String, Object> data = response.getBody().getData();
        assertEquals(List.of(row), data.get("content"));
        assertEquals(1L, data.get("totalElements"));
        assertEquals(1, data.get("totalPages"));
    }

    @Test
    void getUserDetail_returnsServiceResultAsIs() {
        StudentDetailResponse detail =
                StudentDetailResponse.builder().studentId(1L).build();
        when(adminUserService.getUserDetail("student", 1L)).thenReturn(detail);

        ResponseEntity<ApiResponse<Object>> response = controller.getUserDetail("student", 1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(detail, response.getBody().getData());
    }

    @Test
    void createStaff_returns201Created() {
        CreateStaffRequest request = new CreateStaffRequest();
        CreateStaffResponse created =
                CreateStaffResponse.builder().staffId(2L).email("s@x.com").build();
        when(adminUserService.createStaff(ADMIN_EMAIL, request)).thenReturn(created);

        ResponseEntity<ApiResponse<CreateStaffResponse>> response = controller.createStaff(auth, request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertSame(created, response.getBody().getData());
        assertTrue(response.getBody().getMessage().contains("Email mời đã được gửi"));
    }

    @Test
    void updateStudent_delegatesWithStudentType() {
        UpdateStudentRequest request = new UpdateStudentRequest();
        StudentDetailResponse updated =
                StudentDetailResponse.builder().studentId(1L).build();
        when(adminUserService.updateUser(ADMIN_EMAIL, "student", 1L, request)).thenReturn(updated);

        ResponseEntity<ApiResponse<Object>> response = controller.updateStudent(auth, 1L, request);

        assertSame(updated, response.getBody().getData());
        verify(adminUserService).updateUser(ADMIN_EMAIL, "student", 1L, request);
    }

    @Test
    void updateStaff_delegatesWithStaffType() {
        UpdateStaffInfoRequest request = new UpdateStaffInfoRequest();
        StaffDetailResponse updated = StaffDetailResponse.builder().staffId(2L).build();
        when(adminUserService.updateUser(ADMIN_EMAIL, "staff", 2L, request)).thenReturn(updated);

        assertSame(updated, controller.updateStaff(auth, 2L, request).getBody().getData());
    }

    @Test
    void suspendUser_delegatesWithPathType() {
        SuspendUserRequest request = new SuspendUserRequest();
        SuspendUserResponse result =
                SuspendUserResponse.builder().userId(1L).status("suspended").build();
        when(adminUserService.suspendUser(ADMIN_EMAIL, "student", 1L, request)).thenReturn(result);

        ResponseEntity<ApiResponse<SuspendUserResponse>> response =
                controller.suspendUser(auth, "student", 1L, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("suspended", response.getBody().getData().getStatus());
    }

    @Test
    void activateUser_delegates() {
        ActivateUserResponse result =
                ActivateUserResponse.builder().userId(1L).status("active").build();
        when(adminUserService.activateUser(ADMIN_EMAIL, "student", 1L)).thenReturn(result);

        assertEquals(
                "active",
                controller.activateUser(auth, "student", 1L).getBody().getData().getStatus());
    }

    @Test
    void resetPassword_returnsSuccessWithoutBodyData() {
        ResponseEntity<ApiResponse<Void>> response = controller.resetPassword(auth, "staff", 2L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNull(response.getBody().getData());
        verify(adminUserService).resetPassword(ADMIN_EMAIL, "staff", 2L);
    }

    @Test
    void listStaffResetRequests_delegatesStatusFilter() {
        StaffResetRequestResponse row = StaffResetRequestResponse.builder().build();
        when(staffPasswordResetService.listRequests("pending")).thenReturn(List.of(row));

        ResponseEntity<ApiResponse<List<StaffResetRequestResponse>>> response =
                controller.listStaffResetRequests("pending");

        assertEquals(1, response.getBody().getData().size());
    }

    @Test
    void issueTempPassword_delegatesToPasswordResetService() {
        IssueTempPasswordRequest request = new IssueTempPasswordRequest();
        IssueTempPasswordResponse result = IssueTempPasswordResponse.builder().build();
        when(staffPasswordResetService.issueTempPassword(ADMIN_EMAIL, 2L, request))
                .thenReturn(result);

        ResponseEntity<ApiResponse<IssueTempPasswordResponse>> response =
                controller.issueTempPassword(auth, 2L, request);

        assertSame(result, response.getBody().getData());
        assertTrue(response.getBody().getMessage().contains("mật khẩu tạm thời"));
    }

    @Test
    void softDeleteUser_delegates() {
        SoftDeleteUserResponse result =
                SoftDeleteUserResponse.builder().userId(1L).status("deleted").build();
        when(adminUserService.softDeleteUser(ADMIN_EMAIL, "student", 1L)).thenReturn(result);

        assertEquals(
                "deleted",
                controller
                        .softDeleteUser(auth, "student", 1L)
                        .getBody()
                        .getData()
                        .getStatus());
    }

    @Test
    void restoreUser_delegates() {
        RestoreUserResponse result =
                RestoreUserResponse.builder().userId(1L).status("active").build();
        when(adminUserService.restoreUser(ADMIN_EMAIL, "student", 1L)).thenReturn(result);

        assertEquals(
                "active",
                controller.restoreUser(auth, "student", 1L).getBody().getData().getStatus());
    }

    @Test
    void changeStaffRole_delegates() {
        ChangeStaffRoleRequest request = new ChangeStaffRoleRequest();
        ChangeStaffRoleResponse result = ChangeStaffRoleResponse.builder()
                .staffId(2L)
                .newRole("staff_manager")
                .build();
        when(adminUserService.changeStaffRole(ADMIN_EMAIL, 2L, request)).thenReturn(result);

        ResponseEntity<ApiResponse<ChangeStaffRoleResponse>> response = controller.changeStaffRole(auth, 2L, request);

        assertEquals("staff_manager", response.getBody().getData().getNewRole());
    }
}
