/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.admin;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.jlpt.feature.admin.dto.AuditLogItemResponse;
import com.jlpt.feature.staff.StaffUser;
import com.jlpt.feature.student.StudentUser;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

/**
 * Unit tests cho AdminAuditLogService — UC-38 xem nhật ký kiểm toán.
 * Bao gồm các nhánh lọc filter và logic xác định actor role.
 */
@ExtendWith(MockitoExtension.class)
class AdminAuditLogServiceTest {

    @Mock
    private AdminAuditLogRepository adminAuditLogRepository;

    @InjectMocks
    private AdminAuditLogService adminAuditLogService;

    // ── getAuditLogs ─────────────────────────────────────────────────────────

    @Test
    void getAuditLogs_withActionAndTable_passesFiltersToRepository() {
        Page<AdminAuditLog> emptyPage = new PageImpl<>(List.of());
        when(adminAuditLogRepository.findByFilters(eq("LOGIN"), eq("admin_users"), any()))
                .thenReturn(emptyPage);

        adminAuditLogService.getAuditLogs("LOGIN", "admin_users", 0, 10);

        verify(adminAuditLogRepository).findByFilters(eq("LOGIN"), eq("admin_users"), any());
    }

    @Test
    void getAuditLogs_blankAction_passesNullToRepository() {
        Page<AdminAuditLog> emptyPage = new PageImpl<>(List.of());
        when(adminAuditLogRepository.findByFilters(isNull(), isNull(), any())).thenReturn(emptyPage);

        adminAuditLogService.getAuditLogs("   ", "", 0, 10);

        verify(adminAuditLogRepository).findByFilters(isNull(), isNull(), any());
    }

    @Test
    void getAuditLogs_nullAction_passesNullToRepository() {
        Page<AdminAuditLog> emptyPage = new PageImpl<>(List.of());
        when(adminAuditLogRepository.findByFilters(isNull(), isNull(), any())).thenReturn(emptyPage);

        adminAuditLogService.getAuditLogs(null, null, 0, 10);

        verify(adminAuditLogRepository).findByFilters(isNull(), isNull(), any());
    }

    @Test
    void getAuditLogs_mapsLogToResponse() {
        AdminUser admin = AdminUser.builder()
                .id(1L)
                .email("admin@test.com")
                .fullName("Admin User")
                .build();
        AdminAuditLog log = AdminAuditLog.builder()
                .id(10L)
                .adminActor(admin)
                .action("QUIZ_SUBMITTED")
                .description("score=5/10")
                .createdAt(LocalDateTime.now())
                .build();

        Page<AdminAuditLog> page = new PageImpl<>(List.of(log), PageRequest.of(0, 10), 1);
        when(adminAuditLogRepository.findByFilters(any(), any(), any())).thenReturn(page);

        Page<AuditLogItemResponse> result = adminAuditLogService.getAuditLogs(null, null, 0, 10);

        assertEquals(1, result.getTotalElements());
        AuditLogItemResponse item = result.getContent().get(0);
        assertEquals(10L, item.getLogId());
        assertEquals("QUIZ_SUBMITTED", item.getActionType());
        assertEquals("admin@test.com", item.getAdminEmail());
        assertEquals("Admin User", item.getActorName());
        assertEquals("ADMIN", item.getActorRole());
    }

    // ── actorRole logic ──────────────────────────────────────────────────────

    @Test
    void toResponse_withAdminActor_returnsAdminRole() {
        AdminUser admin =
                AdminUser.builder().id(1L).email("a@test.com").fullName("Admin").build();
        AdminAuditLog log = AdminAuditLog.builder()
                .id(1L)
                .adminActor(admin)
                .action("ACTION")
                .createdAt(LocalDateTime.now())
                .build();
        Page<AdminAuditLog> page = new PageImpl<>(List.of(log));
        when(adminAuditLogRepository.findByFilters(any(), any(), any())).thenReturn(page);

        AuditLogItemResponse response = adminAuditLogService
                .getAuditLogs(null, null, 0, 10)
                .getContent()
                .get(0);

        assertEquals("ADMIN", response.getActorRole());
        assertEquals("a@test.com", response.getAdminEmail());
    }

    @Test
    void toResponse_withStaffManagerActor_returnsManagerRole() {
        StaffUser manager = StaffUser.builder()
                .id(2L)
                .email("manager@test.com")
                .fullName("Manager")
                .staffRole(StaffUser.StaffRole.STAFF_MANAGER)
                .build();
        AdminAuditLog log = AdminAuditLog.builder()
                .id(2L)
                .staffActor(manager)
                .action("ACTION")
                .createdAt(LocalDateTime.now())
                .build();
        Page<AdminAuditLog> page = new PageImpl<>(List.of(log));
        when(adminAuditLogRepository.findByFilters(any(), any(), any())).thenReturn(page);

        AuditLogItemResponse response = adminAuditLogService
                .getAuditLogs(null, null, 0, 10)
                .getContent()
                .get(0);

        assertEquals("MANAGER", response.getActorRole());
        assertEquals("manager@test.com", response.getAdminEmail());
    }

    @Test
    void toResponse_withStaffActor_returnsStaffRole() {
        StaffUser staff = StaffUser.builder()
                .id(3L)
                .email("staff@test.com")
                .fullName("Staff")
                .staffRole(StaffUser.StaffRole.STAFF)
                .build();
        AdminAuditLog log = AdminAuditLog.builder()
                .id(3L)
                .staffActor(staff)
                .action("ACTION")
                .createdAt(LocalDateTime.now())
                .build();
        Page<AdminAuditLog> page = new PageImpl<>(List.of(log));
        when(adminAuditLogRepository.findByFilters(any(), any(), any())).thenReturn(page);

        AuditLogItemResponse response = adminAuditLogService
                .getAuditLogs(null, null, 0, 10)
                .getContent()
                .get(0);

        assertEquals("STAFF", response.getActorRole());
    }

    @Test
    void toResponse_withStudentActor_returnsStudentRole() {
        StudentUser student = StudentUser.builder()
                .id(4L)
                .email("student@test.com")
                .fullName("Student")
                .build();
        AdminAuditLog log = AdminAuditLog.builder()
                .id(4L)
                .studentActor(student)
                .action("ACTION")
                .createdAt(LocalDateTime.now())
                .build();
        Page<AdminAuditLog> page = new PageImpl<>(List.of(log));
        when(adminAuditLogRepository.findByFilters(any(), any(), any())).thenReturn(page);

        AuditLogItemResponse response = adminAuditLogService
                .getAuditLogs(null, null, 0, 10)
                .getContent()
                .get(0);

        assertEquals("STUDENT", response.getActorRole());
        assertEquals("student@test.com", response.getAdminEmail());
    }

    @Test
    void toResponse_noActor_returnsSystemRoleAndNullEmail() {
        AdminAuditLog log = AdminAuditLog.builder()
                .id(5L)
                .action("SYSTEM_ACTION")
                .createdAt(LocalDateTime.now())
                .build();
        Page<AdminAuditLog> page = new PageImpl<>(List.of(log));
        when(adminAuditLogRepository.findByFilters(any(), any(), any())).thenReturn(page);

        AuditLogItemResponse response = adminAuditLogService
                .getAuditLogs(null, null, 0, 10)
                .getContent()
                .get(0);

        assertEquals("SYSTEM", response.getActorRole());
        assertNull(response.getAdminEmail());
        assertEquals("Hệ thống", response.getActorName());
    }
}
