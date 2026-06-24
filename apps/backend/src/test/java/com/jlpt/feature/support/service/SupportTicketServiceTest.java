/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.support.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.jlpt.feature.notification.dto.SendNotificationRequest;
import com.jlpt.feature.support.dto.TicketReplyRequest;
import com.jlpt.feature.notification.entity.Notification;
import com.jlpt.feature.staff.entity.StaffUser;
import com.jlpt.feature.corelearning.entity.StudentSubmission;
import com.jlpt.feature.student.entity.StudentUser;
import com.jlpt.feature.support.entity.Ticket;
import com.jlpt.feature.support.entity.TicketReply;
import com.jlpt.shared.exception.BusinessException;
import com.jlpt.shared.exception.ForbiddenException;
import com.jlpt.shared.exception.ResourceNotFoundException;
import com.jlpt.feature.support.dto.ManualGradeRequest;
import com.jlpt.feature.support.dto.TicketRequest;
import com.jlpt.shared.audit.AdminAuditLogRepository;
import com.jlpt.feature.staff.repository.StaffUserRepository;
import com.jlpt.feature.corelearning.repository.StudentSubmissionRepository;
import com.jlpt.feature.student.repository.StudentUserRepository;
import com.jlpt.feature.support.repository.TicketReplyRepository;
import com.jlpt.feature.support.repository.TicketRepository;
import com.jlpt.feature.notification.repository.NotificationRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class SupportTicketServiceTest {

    @Mock private TicketRepository ticketRepository;
    @Mock private TicketReplyRepository ticketReplyRepository;
    @Mock private NotificationRepository notificationRepository;
    @Mock private StudentUserRepository studentUserRepository;
    @Mock private StaffUserRepository staffUserRepository;
    @Mock private AdminAuditLogRepository adminAuditLogRepository;
    @Mock private StudentSubmissionRepository submissionRepository;

    @InjectMocks
    private SupportTicketService service;

    private StudentUser student;
    private StaffUser staff;

    @BeforeEach
    void setUp() {
        // submissionRepository is field-injected via @Autowired(required = false), outside the
        // Lombok-generated constructor — Mockito's @InjectMocks only does constructor injection
        // here, so the optional field must be wired manually.
        ReflectionTestUtils.setField(service, "submissionRepository", submissionRepository);

        student = StudentUser.builder()
                .id(1L)
                .email("student@example.com")
                .fullName("Nguyen Van A")
                .status(StudentUser.StudentStatus.ACTIVE)
                .build();

        staff = StaffUser.builder()
                .id(2L)
                .email("staff@example.com")
                .fullName("Tran Thi B")
                .staffRole(StaffUser.StaffRole.STAFF)
                .build();
    }

    private Ticket openTicket() {
        return Ticket.builder()
                .id(88L)
                .student(student)
                .subject("Subject")
                .content("Content")
                .priority(Ticket.Priority.NORMAL)
                .status(Ticket.TicketStatus.OPEN)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    void createTicket_withValidData_returnsTicketWithOpenStatus() {
        when(studentUserRepository.findById(1L)).thenReturn(Optional.of(student));
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(inv -> {
            Ticket t = inv.getArgument(0);
            t.setId(88L);
            return t;
        });
        when(ticketReplyRepository.countByTicketId(88L)).thenReturn(0L);

        TicketRequest req = new TicketRequest();
        req.setSubject("Không phát âm được");
        req.setContent("Không tìm thấy microphone");

        var result = service.createTicket(1L, req);

        assertEquals("open", result.getStatus());
        assertEquals(88L, result.getTicketId());
        verify(ticketRepository).save(any(Ticket.class));
    }

    @Test
    void getMyTickets_onlyReturnsOwnTickets() {
        Ticket t = openTicket();
        when(ticketRepository.findByStudentId(1L, PageRequest.of(0, 10)))
                .thenReturn(new PageImpl<>(List.of(t)));
        when(ticketReplyRepository.countByTicketId(88L)).thenReturn(0L);

        Page<?> result = service.getMyTickets(1L, null, 0, 10);

        assertEquals(1, result.getTotalElements());
        verify(ticketRepository).findByStudentId(1L, PageRequest.of(0, 10));
        verify(ticketRepository, never()).findAllByFilters(any(), any(), any(), any(), any());
    }

    @Test
    void getStudentTicketDetail_notOwnTicket_throwsForbidden() {
        Ticket t = openTicket();
        when(ticketRepository.findById(88L)).thenReturn(Optional.of(t));

        assertThrows(ForbiddenException.class, () -> service.getStudentTicketDetail(88L, 999L));
    }

    @Test
    void addStudentReply_notOwnTicket_throwsForbidden() {
        Ticket t = openTicket();
        when(ticketRepository.findById(88L)).thenReturn(Optional.of(t));

        TicketReplyRequest req = new TicketReplyRequest();
        req.setMessage("Hello");

        assertThrows(ForbiddenException.class, () -> service.addStudentReply(88L, 999L, req));
    }

    @Test
    void addStudentReply_toClosedTicket_throws409() {
        Ticket t = openTicket();
        t.setStatus(Ticket.TicketStatus.CLOSED);
        when(ticketRepository.findById(88L)).thenReturn(Optional.of(t));

        TicketReplyRequest req = new TicketReplyRequest();
        req.setMessage("Hello");

        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.addStudentReply(88L, 1L, req));
        assertEquals(409, ex.getStatus());
        assertEquals("TICKET_CLOSED", ex.getErrorCode());
    }

    @Test
    void addStaffReply_whenTicketOpen_changesStatusToInProgress() {
        Ticket t = openTicket();
        when(ticketRepository.findById(88L)).thenReturn(Optional.of(t));
        when(staffUserRepository.findByEmail("staff@example.com")).thenReturn(Optional.of(staff));
        when(ticketReplyRepository.save(any(TicketReply.class))).thenAnswer(inv -> inv.getArgument(0));

        TicketReplyRequest req = new TicketReplyRequest();
        req.setMessage("Đã ghi nhận");

        service.addStaffReply(88L, "staff@example.com", req);

        assertEquals(Ticket.TicketStatus.IN_PROGRESS, t.getStatus());
        verify(ticketRepository).save(t);
    }

    @Test
    void closeTicket_asStaff_setsResolvedStatus() {
        Ticket t = openTicket();
        when(ticketRepository.findById(88L)).thenReturn(Optional.of(t));
        when(staffUserRepository.findByEmail("staff@example.com")).thenReturn(Optional.of(staff));

        var result = service.closeTicket(88L, "staff@example.com");

        assertEquals("resolved", result.getStatus());
        assertNotNull(t.getResolvedAt());
        verify(adminAuditLogRepository).save(any());
    }

    @Test
    void assignTicket_asPlainStaff_throwsForbidden() {
        when(staffUserRepository.findByEmail("staff@example.com")).thenReturn(Optional.of(staff));

        assertThrows(ForbiddenException.class,
                () -> service.assignTicket(88L, 5L, "staff@example.com", false));
        verify(ticketRepository, never()).findById(any());
    }

    @Test
    void assignTicket_asStaffManager_succeeds() {
        staff.setStaffRole(StaffUser.StaffRole.STAFF_MANAGER);
        StaffUser target = StaffUser.builder().id(5L).email("target@example.com").fullName("Target").build();
        Ticket t = openTicket();
        when(staffUserRepository.findByEmail("staff@example.com")).thenReturn(Optional.of(staff));
        when(ticketRepository.findById(88L)).thenReturn(Optional.of(t));
        when(staffUserRepository.findById(5L)).thenReturn(Optional.of(target));

        var result = service.assignTicket(88L, 5L, "staff@example.com", false);

        assertEquals(5L, result.getAssignedToStaffId());
    }

    @Test
    void assignTicket_asAdmin_succeedsWithoutManagerCheck() {
        Ticket t = openTicket();
        StaffUser target = StaffUser.builder().id(5L).email("target@example.com").fullName("Target").build();
        when(ticketRepository.findById(88L)).thenReturn(Optional.of(t));
        when(staffUserRepository.findById(5L)).thenReturn(Optional.of(target));

        var result = service.assignTicket(88L, 5L, "admin@example.com", true);

        assertEquals(5L, result.getAssignedToStaffId());
    }

    @Test
    void sendNotification_returnsJobIdImmediately() {
        when(staffUserRepository.findByEmail("staff@example.com")).thenReturn(Optional.of(staff));
        when(studentUserRepository.findAll()).thenReturn(List.of(student));

        SendNotificationRequest req = new SendNotificationRequest();
        req.setTitle("Bảo trì hệ thống");
        req.setContent("Hệ thống bảo trì");
        req.setNotificationType("warning");
        req.setChannel("both");

        long start = System.currentTimeMillis();
        String jobId = service.sendNotification("staff@example.com", req);
        long elapsed = System.currentTimeMillis() - start;

        assertNotNull(jobId);
        assertTrue(jobId.startsWith("job_notification_"));
        assertTrue(elapsed < 500, "sendNotification must return quickly (non-blocking)");
        verify(notificationRepository).save(any(Notification.class));
    }

    @Test
    void markNotificationRead_setsIsReadAndReadAt() {
        Notification n = Notification.builder().id(10L).student(student).isRead(false).build();
        when(notificationRepository.findById(10L)).thenReturn(Optional.of(n));

        service.markNotificationRead(10L, 1L);

        assertTrue(n.getIsRead());
        assertNotNull(n.getReadAt());
        verify(notificationRepository).save(n);
    }

    @Test
    void markNotificationRead_notOwner_throwsForbidden() {
        Notification n = Notification.builder().id(10L).student(student).isRead(false).build();
        when(notificationRepository.findById(10L)).thenReturn(Optional.of(n));

        assertThrows(ForbiddenException.class, () -> service.markNotificationRead(10L, 999L));
    }

    @Test
    void markAllNotificationsRead_returnsMarkedCount() {
        when(notificationRepository.markAllReadByStudentId(eq(1L), any())).thenReturn(3);

        int result = service.markAllNotificationsRead(1L);

        assertEquals(3, result);
    }

    private StudentSubmission speakingAiGradedSubmission() {
        return StudentSubmission.builder()
                .id(45L)
                .student(student)
                .submissionType(StudentSubmission.SubmissionType.SPEAKING)
                .status(StudentSubmission.SubmissionStatus.AI_GRADED)
                .aiOverallScore(new BigDecimal("72.50"))
                .submittedAt(LocalDateTime.now())
                .build();
    }

    @Test
    void manualGrade_withSpeakingAiGraded_setsManualAndFinalScore() {
        StudentSubmission submission = speakingAiGradedSubmission();
        when(submissionRepository.findById(45L)).thenReturn(Optional.of(submission));
        when(staffUserRepository.findByEmail("staff@example.com")).thenReturn(Optional.of(staff));

        ManualGradeRequest req = new ManualGradeRequest();
        req.setManualScore(new BigDecimal("85.50"));
        req.setManualFeedback("Phát âm tốt");

        var result = service.manualGrade(45L, "staff@example.com", req);

        assertEquals(new BigDecimal("85.50"), result.getManualScore());
        assertEquals(new BigDecimal("85.50"), result.getFinalScore());
        assertEquals("graded", result.getStatus());
        assertEquals(StudentSubmission.SubmissionStatus.GRADED, submission.getStatus());
        verify(submissionRepository).save(submission);
    }

    @Test
    void manualGrade_sendsNotificationToStudent() {
        StudentSubmission submission = speakingAiGradedSubmission();
        when(submissionRepository.findById(45L)).thenReturn(Optional.of(submission));
        when(staffUserRepository.findByEmail("staff@example.com")).thenReturn(Optional.of(staff));

        ManualGradeRequest req = new ManualGradeRequest();
        req.setManualScore(new BigDecimal("85.50"));

        service.manualGrade(45L, "staff@example.com", req);

        verify(notificationRepository).save(any(Notification.class));
        verify(adminAuditLogRepository).save(any());
    }

    @Test
    void manualGrade_withHandwriting_throws422() {
        StudentSubmission submission = speakingAiGradedSubmission();
        submission.setSubmissionType(StudentSubmission.SubmissionType.HANDWRITING);
        when(submissionRepository.findById(45L)).thenReturn(Optional.of(submission));

        ManualGradeRequest req = new ManualGradeRequest();
        req.setManualScore(new BigDecimal("85.50"));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.manualGrade(45L, "staff@example.com", req));
        assertEquals(422, ex.getStatus());
    }

    @Test
    void manualGrade_withPendingStatus_throws422() {
        StudentSubmission submission = speakingAiGradedSubmission();
        submission.setStatus(StudentSubmission.SubmissionStatus.PENDING);
        when(submissionRepository.findById(45L)).thenReturn(Optional.of(submission));

        ManualGradeRequest req = new ManualGradeRequest();
        req.setManualScore(new BigDecimal("85.50"));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.manualGrade(45L, "staff@example.com", req));
        assertEquals(422, ex.getStatus());
    }

    @Test
    void manualGrade_submissionNotFound_throws404() {
        when(submissionRepository.findById(45L)).thenReturn(Optional.empty());

        ManualGradeRequest req = new ManualGradeRequest();
        req.setManualScore(new BigDecimal("85.50"));

        assertThrows(ResourceNotFoundException.class,
                () -> service.manualGrade(45L, "staff@example.com", req));
    }
}
