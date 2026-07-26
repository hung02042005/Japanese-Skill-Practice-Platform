/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.admin;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.jlpt.feature.admin.dto.AdminDashboardResponse;
import com.jlpt.feature.assessment.TestAttemptRepository;
import com.jlpt.feature.staff.StaffUserRepository;
import com.jlpt.feature.student.StudentUser;
import com.jlpt.feature.student.StudentUserRepository;
import com.jlpt.feature.support.Ticket;
import com.jlpt.feature.support.repository.TicketRepository;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit tests cho AdminDashboardService — UC-37 tổng hợp số liệu hệ thống.
 */
@ExtendWith(MockitoExtension.class)
class AdminDashboardServiceTest {

    @Mock
    private StudentUserRepository studentUserRepository;

    @Mock
    private StaffUserRepository staffUserRepository;

    @Mock
    private AdminUserRepository adminUserRepository;

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private TestAttemptRepository testAttemptRepository;

    @Mock
    private MaintenanceModeService maintenanceModeService;

    @InjectMocks
    private AdminDashboardService adminDashboardService;

    // ── System status ────────────────────────────────────────────────────────

    @Test
    void getOverview_maintenanceModeOn_returnsMaintenanceStatus() {
        stubRepositories();
        when(maintenanceModeService.isEnabled()).thenReturn(true);

        AdminDashboardResponse response = adminDashboardService.getOverview();

        assertEquals("MAINTENANCE", response.getSummary().getSystemStatus());
    }

    @Test
    void getOverview_maintenanceModeOff_returnsOkStatus() {
        stubRepositories();
        when(maintenanceModeService.isEnabled()).thenReturn(false);

        AdminDashboardResponse response = adminDashboardService.getOverview();

        assertEquals("OK", response.getSummary().getSystemStatus());
    }

    // ── Total users aggregation ───────────────────────────────────────────────

    @Test
    void getOverview_aggregatesTotalUsers() {
        when(studentUserRepository.count()).thenReturn(100L);
        when(staffUserRepository.count()).thenReturn(10L);
        when(adminUserRepository.count()).thenReturn(2L);
        when(studentUserRepository.countByLastActivityDate(any())).thenReturn(0L);
        when(testAttemptRepository.countByStartedAtAfter(any())).thenReturn(0L);
        when(maintenanceModeService.isEnabled()).thenReturn(false);
        when(studentUserRepository.countByStatus(StudentUser.StudentStatus.SUSPENDED))
                .thenReturn(0L);
        when(studentUserRepository.countByCreatedAtAfter(any())).thenReturn(0L);
        when(ticketRepository.countByStatus(Ticket.TicketStatus.OPEN)).thenReturn(0L);
        when(ticketRepository.countByStatus(Ticket.TicketStatus.IN_PROGRESS)).thenReturn(0L);

        AdminDashboardResponse response = adminDashboardService.getOverview();

        assertEquals(112L, response.getSummary().getTotalUsers());
    }

    // ── KPI — pending submissions ─────────────────────────────────────────────

    @Test
    void getOverview_withoutSubmissionRepository_pendingIsZero() {
        // submissionRepository is null (not injected) — pending should be 0
        stubRepositories();
        when(maintenanceModeService.isEnabled()).thenReturn(false);

        AdminDashboardResponse response = adminDashboardService.getOverview();

        assertEquals(0L, response.getKpi().getPendingSubmissions());
    }

    @Test
    void getOverview_openTickets_countedCorrectly() {
        stubRepositories();
        when(ticketRepository.countByStatus(Ticket.TicketStatus.OPEN)).thenReturn(5L);
        when(maintenanceModeService.isEnabled()).thenReturn(false);

        AdminDashboardResponse response = adminDashboardService.getOverview();

        assertEquals(5L, response.getKpi().getOpenTickets());
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private void stubRepositories() {
        when(studentUserRepository.count()).thenReturn(0L);
        when(staffUserRepository.count()).thenReturn(0L);
        when(adminUserRepository.count()).thenReturn(0L);
        when(studentUserRepository.countByLastActivityDate(any(LocalDate.class)))
                .thenReturn(0L);
        when(testAttemptRepository.countByStartedAtAfter(any())).thenReturn(0L);
        when(studentUserRepository.countByStatus(StudentUser.StudentStatus.SUSPENDED))
                .thenReturn(0L);
        when(studentUserRepository.countByCreatedAtAfter(any())).thenReturn(0L);
        when(ticketRepository.countByStatus(Ticket.TicketStatus.OPEN)).thenReturn(0L);
        when(ticketRepository.countByStatus(Ticket.TicketStatus.IN_PROGRESS)).thenReturn(0L);
    }
}
