/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.admin;

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
    private final AdminAuditLogRepository adminAuditLogRepository;

    /** Sở hữu bởi Người 3 — optional để không vỡ context khi chưa sẵn sàng. */
    @Autowired(required = false)
    private StudentSubmissionRepository submissionRepository;

    @Transactional(readOnly = true)
    public DashboardResponse getAdminDashboard() {
        LocalDateTime monthStart = LocalDate.now().withDayOfMonth(1).atStartOfDay();

        long pending = 0;
        long gradedThisMonth = 0;
        if (submissionRepository != null) {
            pending = submissionRepository.countByStatusIn(List.of(
                    StudentSubmission.SubmissionStatus.PENDING,
                    StudentSubmission.SubmissionStatus.AI_GRADED));
            gradedThisMonth = submissionRepository.countByStatusAndGradedAtAfter(
                    StudentSubmission.SubmissionStatus.GRADED, monthStart);
        }

        var recent = adminAuditLogRepository.findTop10ByOrderByCreatedAtDesc().stream()
                .map(this::toActivityItem)
                .toList();

        return DashboardResponse.builder()
                .generatedAt(LocalDateTime.now())
                .totalStudents(studentUserRepository.count())
                .activeStudents(studentUserRepository.countByStatus(StudentUser.StudentStatus.ACTIVE))
                .suspendedStudents(studentUserRepository.countByStatus(StudentUser.StudentStatus.SUSPENDED))
                .newStudentsThisMonth(studentUserRepository.countByCreatedAtAfter(monthStart))
                .openTickets(ticketRepository.countByStatus(Ticket.TicketStatus.OPEN))
                .inProgressTickets(ticketRepository.countByStatus(Ticket.TicketStatus.IN_PROGRESS))
                .resolvedTicketsThisMonth(ticketRepository.countByStatusAndResolvedAtAfter(
                        Ticket.TicketStatus.RESOLVED, monthStart))
                .pendingSubmissions(pending)
                .gradedSubmissionsThisMonth(gradedThisMonth)
                .recentActivity(recent)
                .build();
    }

    @Transactional(readOnly = true)
    public AdminDashboardSummaryResponse getSummary() {
        LocalDateTime dayStart = LocalDate.now().atStartOfDay();
        long totalUsers =
                studentUserRepository.count() + staffUserRepository.count() + adminUserRepository.count();
        return AdminDashboardSummaryResponse.builder()
                .totalUsers(totalUsers)
                .activeToday(studentUserRepository.countByLastActivityDate(LocalDate.now()))
                .quizAttemptsToday(testAttemptRepository.countByStartedAtAfter(dayStart))
                .systemStatus("OK")
                .build();
    }

    private DashboardResponse.RecentActivityItem toActivityItem(AdminAuditLog l) {
        String actorName;
        String actorType;
        if (l.getAdminActor() != null) {
            actorName = l.getAdminActor().getFullName();
            actorType = "ADMIN";
        } else if (l.getStaffActor() != null) {
            actorName = l.getStaffActor().getFullName();
            actorType = "STAFF";
        } else if (l.getStudentActor() != null) {
            actorName = l.getStudentActor().getFullName();
            actorType = "STUDENT";
        } else {
            actorName = "Hệ thống";
            actorType = "SYSTEM";
        }
        return DashboardResponse.RecentActivityItem.builder()
                .actorName(actorName)
                .actorType(actorType)
                .action(l.getAction())
                .timestamp(l.getCreatedAt())
                .build();
    }
}
