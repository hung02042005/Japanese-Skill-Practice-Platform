/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.admin;

import com.jlpt.feature.admin.dto.AdminDashboardResponse;
import com.jlpt.feature.admin.dto.AdminDashboardSummaryResponse;
import com.jlpt.feature.admin.dto.DashboardResponse;
import com.jlpt.feature.assessment.StudentSubmission;
import com.jlpt.feature.assessment.StudentSubmissionRepository;
import com.jlpt.feature.assessment.TestAttemptRepository;
import com.jlpt.feature.staff.StaffUserRepository;
import com.jlpt.feature.student.StudentUser;
import com.jlpt.feature.student.StudentUserRepository;
import com.jlpt.feature.support.Ticket;
import com.jlpt.feature.support.repository.TicketRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Admin dashboard (UC-37) — tổng hợp số liệu hệ thống + hoạt động gần đây. */
@Service
@RequiredArgsConstructor
public class AdminDashboardService {

    private final StudentUserRepository studentUserRepository;
    private final StaffUserRepository staffUserRepository;
    private final AdminUserRepository adminUserRepository;
    private final TicketRepository ticketRepository;
    private final TestAttemptRepository testAttemptRepository;
    private final MaintenanceModeService maintenanceModeService;

    /** Sở hữu bởi Người 3 — optional để không vỡ context khi chưa sẵn sàng. */
    @Autowired(required = false)
    private StudentSubmissionRepository submissionRepository;

    /** GET /api/admin/dashboard — gộp summary + kpi trong 1 request. */
    @Transactional(readOnly = true)
    public AdminDashboardResponse getOverview() {
        return AdminDashboardResponse.builder()
                .summary(buildSummary())
                .kpi(buildKpi())
                .build();
    }

    private DashboardResponse buildKpi() {
        LocalDateTime monthStart = LocalDate.now().withDayOfMonth(1).atStartOfDay();

        long pending = 0;
        if (submissionRepository != null) {
            pending = submissionRepository.countByStatusIn(
                    List.of(StudentSubmission.SubmissionStatus.PENDING, StudentSubmission.SubmissionStatus.AI_GRADED));
        }

        return DashboardResponse.builder()
                .suspendedStudents(studentUserRepository.countByStatus(StudentUser.StudentStatus.SUSPENDED))
                .newStudentsThisMonth(studentUserRepository.countByCreatedAtAfter(monthStart))
                .openTickets(ticketRepository.countByStatus(Ticket.TicketStatus.OPEN))
                .inProgressTickets(ticketRepository.countByStatus(Ticket.TicketStatus.IN_PROGRESS))
                .pendingSubmissions(pending)
                .build();
    }

    private AdminDashboardSummaryResponse buildSummary() {
        LocalDateTime dayStart = LocalDate.now().atStartOfDay();
        long totalUsers = studentUserRepository.count() + staffUserRepository.count() + adminUserRepository.count();
        return AdminDashboardSummaryResponse.builder()
                .totalUsers(totalUsers)
                .activeToday(studentUserRepository.countByLastActivityDate(LocalDate.now()))
                .quizAttemptsToday(testAttemptRepository.countByStartedAtAfter(dayStart))
                .systemStatus(resolveSystemStatus())
                .build();
    }

    /** Trạng thái hệ thống thật: phản ánh cờ bảo trì trong settings (group=system). */
    private String resolveSystemStatus() {
        return maintenanceModeService.isEnabled() ? "MAINTENANCE" : "OK";
    }
}
