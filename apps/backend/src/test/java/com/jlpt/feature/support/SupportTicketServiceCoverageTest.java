/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.support;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.jlpt.feature.admin.AdminAuditLog;
import com.jlpt.feature.admin.AdminAuditLogRepository;
import com.jlpt.feature.assessment.StudentSubmission;
import com.jlpt.feature.assessment.StudentSubmissionRepository;
import com.jlpt.feature.notification.Notification;
import com.jlpt.feature.notification.service.NotificationService;
import com.jlpt.feature.staff.StaffUser;
import com.jlpt.feature.staff.StaffUserRepository;
import com.jlpt.feature.student.StudentUser;
import com.jlpt.feature.student.StudentUserRepository;
import com.jlpt.feature.support.dto.GradeResponse;
import com.jlpt.feature.support.dto.ManualGradeRequest;
import com.jlpt.feature.support.dto.SubmissionResponse;
import com.jlpt.feature.support.dto.TicketDetailResponse;
import com.jlpt.feature.support.dto.TicketReplyRequest;
import com.jlpt.feature.support.dto.TicketReplyResponse;
import com.jlpt.feature.support.dto.TicketRequest;
import com.jlpt.feature.support.dto.TicketResponse;
import com.jlpt.feature.support.repository.TicketReplyRepository;
import com.jlpt.feature.support.repository.TicketRepository;
import com.jlpt.feature.support.service.SupportTicketService;
import com.jlpt.shared.exception.BusinessException;
import com.jlpt.shared.exception.ForbiddenException;
import com.jlpt.shared.exception.ResourceNotFoundException;
import java.math.BigDecimal;
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
import org.springframework.test.util.ReflectionTestUtils;

/**
 * Bổ sung độ phủ cho SupportTicketService: phân trang/lọc ticket, quyền phản hồi & đóng ticket,
 * phân công ticket (UC-29) và chấm điểm thủ công bài nói (UC-31).
 */
@ExtendWith(MockitoExtension.class)
class SupportTicketServiceCoverageTest {

    private static final String STAFF_EMAIL = "staff@example.com";

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private TicketReplyRepository ticketReplyRepository;

    @Mock
    private NotificationService notificationService;

    @Mock
    private StudentUserRepository studentUserRepository;

    @Mock
    private StaffUserRepository staffUserRepository;

    @Mock
    private AdminAuditLogRepository adminAuditLogRepository;

    @Mock
    private StudentSubmissionRepository submissionRepository;

    @InjectMocks
    private SupportTicketService service;

    private StudentUser student;
    private StaffUser staff;
    private StaffUser manager;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(service, "submissionRepository", submissionRepository);

        student = StudentUser.builder()
                .id(1L)
                .email("student@example.com")
                .fullName("Student")
                .currentJlptLevel(StudentUser.JlptLevel.N4)
                .build();
        staff = StaffUser.builder()
                .id(2L)
                .email(STAFF_EMAIL)
                .fullName("Staff")
                .staffRole(StaffUser.StaffRole.STAFF)
                .status(StaffUser.StaffStatus.ACTIVE)
                .build();
        manager = StaffUser.builder()
                .id(3L)
                .email("manager@example.com")
                .fullName("Manager")
                .staffRole(StaffUser.StaffRole.STAFF_MANAGER)
                .status(StaffUser.StaffStatus.ACTIVE)
                .build();
    }

    private Ticket ticket(Ticket.TicketStatus status) {
        return Ticket.builder()
                .id(10L)
                .student(student)
                .subject("Help")
                .content("Need assistance")
                .category("technical")
                .priority(Ticket.Priority.NORMAL)
                .status(status)
                .createdAt(LocalDateTime.now())
                .build();
    }

    // ── createTicket ─────────────────────────────────────────────────────────

    @Test
    void createTicket_withoutPriority_defaultsToNormal() {
        when(studentUserRepository.findById(1L)).thenReturn(Optional.of(student));
        when(ticketRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        TicketRequest req = new TicketRequest();
        req.setSubject("Help");
        req.setContent("Need assistance");

        TicketResponse response = service.createTicket(1L, req);

        assertEquals("normal", response.getPriority());
        assertEquals("open", response.getStatus());
        assertEquals("Student", response.getStudentName());
    }

    @Test
    void createTicket_studentNotFound_throwsNotFound() {
        when(studentUserRepository.findById(404L)).thenReturn(Optional.empty());
        TicketRequest req = new TicketRequest();

        assertThrows(ResourceNotFoundException.class, () -> service.createTicket(404L, req));
    }

    // ── getMyTickets ─────────────────────────────────────────────────────────

    @Test
    void getMyTickets_noStatusFilter_returnsAllOfStudent() {
        when(ticketRepository.findByStudentId(eq(1L), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(ticket(Ticket.TicketStatus.OPEN))));

        Page<TicketResponse> page = service.getMyTickets(1L, null, 0, 10);

        assertEquals(1, page.getTotalElements());
        assertEquals(10L, page.getContent().get(0).getTicketId());
    }

    @Test
    void getMyTickets_blankStatus_treatedAsNoFilter() {
        when(ticketRepository.findByStudentId(eq(1L), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(ticket(Ticket.TicketStatus.OPEN))));

        assertEquals(1, service.getMyTickets(1L, "   ", 0, 10).getTotalElements());
    }

    @Test
    void getMyTickets_withStatusFilter_parsesEnum() {
        when(ticketRepository.findByStudentIdAndStatus(eq(1L), eq(Ticket.TicketStatus.RESOLVED), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(ticket(Ticket.TicketStatus.RESOLVED))));

        Page<TicketResponse> page = service.getMyTickets(1L, "resolved", 0, 10);

        assertEquals("resolved", page.getContent().get(0).getStatus());
    }

    @Test
    void getMyTickets_invalidStatus_throwsInvalidStatus() {
        BusinessException ex = assertThrows(BusinessException.class, () -> service.getMyTickets(1L, "flying", 0, 10));

        assertEquals(400, ex.getStatus());
        assertEquals("INVALID_STATUS", ex.getErrorCode());
    }

    // ── ticket detail ────────────────────────────────────────────────────────

    @Test
    void getStudentTicketDetail_ownTicket_includesRepliesFromBothSides() {
        Ticket t = ticket(Ticket.TicketStatus.IN_PROGRESS);
        t.setAssignedTo(staff);
        TicketReply studentReply = TicketReply.builder()
                .id(100L)
                .ticket(t)
                .studentSender(student)
                .message("Từ học viên")
                .build();
        TicketReply staffReply = TicketReply.builder()
                .id(101L)
                .ticket(t)
                .staffSender(staff)
                .message("Từ nhân viên")
                .build();
        when(ticketRepository.findById(10L)).thenReturn(Optional.of(t));
        when(ticketReplyRepository.findByTicketIdOrderByCreatedAtAsc(10L))
                .thenReturn(List.of(studentReply, staffReply));

        TicketDetailResponse detail = service.getStudentTicketDetail(10L, 1L);

        assertEquals(2, detail.getReplies().size());
        assertEquals("STUDENT", detail.getReplies().get(0).getSenderRole());
        assertEquals("Student", detail.getReplies().get(0).getSenderName());
        assertEquals("STAFF", detail.getReplies().get(1).getSenderRole());
        assertEquals("Staff", detail.getReplies().get(1).getSenderName());
        assertEquals(2L, detail.getAssignedToStaffId());
        assertEquals("Staff", detail.getAssignedToStaffName());
    }

    @Test
    void getStaffTicketDetail_skipsOwnershipCheck() {
        when(ticketRepository.findById(10L)).thenReturn(Optional.of(ticket(Ticket.TicketStatus.OPEN)));
        when(ticketReplyRepository.findByTicketIdOrderByCreatedAtAsc(10L)).thenReturn(List.of());

        TicketDetailResponse detail = service.getStaffTicketDetail(10L);

        assertEquals(10L, detail.getTicketId());
        assertTrue(detail.getReplies().isEmpty());
        assertNull(detail.getAssignedToStaffId());
    }

    @Test
    void getStaffTicketDetail_unknownTicket_throwsNotFound() {
        when(ticketRepository.findById(404L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.getStaffTicketDetail(404L));
    }

    // ── addStudentReply ──────────────────────────────────────────────────────

    @Test
    void addStudentReply_ownOpenTicket_savesReplyAndTouchesLastReplyAt() {
        Ticket t = ticket(Ticket.TicketStatus.OPEN);
        when(ticketRepository.findById(10L)).thenReturn(Optional.of(t));
        when(studentUserRepository.findById(1L)).thenReturn(Optional.of(student));
        when(ticketReplyRepository.save(any())).thenAnswer(i -> {
            TicketReply r = i.getArgument(0);
            r.setId(100L);
            return r;
        });
        TicketReplyRequest req = new TicketReplyRequest();
        req.setMessage("Cảm ơn");
        req.setAttachmentUrl("/uploads/a.png");

        TicketReplyResponse response = service.addStudentReply(10L, 1L, req);

        assertEquals("STUDENT", response.getSenderRole());
        assertEquals("/uploads/a.png", response.getAttachmentUrl());
        assertNotNull(t.getLastReplyAt());
        verify(ticketRepository).save(t);
    }

    @Test
    void addStudentReply_otherStudentsTicket_throwsForbidden() {
        Ticket t = ticket(Ticket.TicketStatus.OPEN);
        when(ticketRepository.findById(10L)).thenReturn(Optional.of(t));
        TicketReplyRequest req = new TicketReplyRequest();
        req.setMessage("Cảm ơn");

        assertThrows(ForbiddenException.class, () -> service.addStudentReply(10L, 99L, req));
    }

    @Test
    void addStudentReply_resolvedTicket_throwsTicketClosed() {
        when(ticketRepository.findById(10L)).thenReturn(Optional.of(ticket(Ticket.TicketStatus.RESOLVED)));
        TicketReplyRequest req = new TicketReplyRequest();
        req.setMessage("Cảm ơn");

        BusinessException ex = assertThrows(BusinessException.class, () -> service.addStudentReply(10L, 1L, req));
        assertEquals(409, ex.getStatus());
        assertEquals("TICKET_CLOSED", ex.getErrorCode());
    }

    @Test
    void addStudentReply_closedTicket_throwsTicketClosed() {
        when(ticketRepository.findById(10L)).thenReturn(Optional.of(ticket(Ticket.TicketStatus.CLOSED)));
        TicketReplyRequest req = new TicketReplyRequest();
        req.setMessage("Cảm ơn");

        assertEquals(
                "TICKET_CLOSED",
                assertThrows(BusinessException.class, () -> service.addStudentReply(10L, 1L, req))
                        .getErrorCode());
    }

    // ── addStaffReply ────────────────────────────────────────────────────────

    @Test
    void addStaffReply_managerOnUnassignedTicket_isAllowedAndMovesToInProgress() {
        Ticket t = ticket(Ticket.TicketStatus.OPEN);
        when(ticketRepository.findById(10L)).thenReturn(Optional.of(t));
        when(staffUserRepository.findByEmail("manager@example.com")).thenReturn(Optional.of(manager));
        when(ticketReplyRepository.save(any())).thenAnswer(i -> {
            TicketReply r = i.getArgument(0);
            r.setId(100L);
            return r;
        });
        TicketReplyRequest req = new TicketReplyRequest();
        req.setMessage("Chúng tôi đang xử lý");

        TicketReplyResponse response = service.addStaffReply(10L, "manager@example.com", req);

        assertEquals("Manager", response.getSenderName());
        assertEquals(Ticket.TicketStatus.IN_PROGRESS, t.getStatus());
        verify(notificationService)
                .notifyStudent(
                        eq(student),
                        anyString(),
                        anyString(),
                        eq(Notification.NotificationType.SYSTEM),
                        eq("ticket_reply_100"),
                        eq(manager));
    }

    @Test
    void addStaffReply_unassignedNonManager_throwsForbidden() {
        Ticket t = ticket(Ticket.TicketStatus.OPEN);
        when(ticketRepository.findById(10L)).thenReturn(Optional.of(t));
        when(staffUserRepository.findByEmail(STAFF_EMAIL)).thenReturn(Optional.of(staff));
        TicketReplyRequest req = new TicketReplyRequest();
        req.setMessage("Xin chào");

        assertThrows(ForbiddenException.class, () -> service.addStaffReply(10L, STAFF_EMAIL, req));
        verifyNoInteractions(notificationService);
    }

    @Test
    void addStaffReply_assignedStaffOnInProgressTicket_keepsStatus() {
        Ticket t = ticket(Ticket.TicketStatus.IN_PROGRESS);
        t.setAssignedTo(staff);
        when(ticketRepository.findById(10L)).thenReturn(Optional.of(t));
        when(staffUserRepository.findByEmail(STAFF_EMAIL)).thenReturn(Optional.of(staff));
        when(ticketReplyRepository.save(any())).thenAnswer(i -> {
            TicketReply r = i.getArgument(0);
            r.setId(100L);
            return r;
        });
        TicketReplyRequest req = new TicketReplyRequest();
        req.setMessage("Đã xử lý xong");

        service.addStaffReply(10L, STAFF_EMAIL, req);

        assertEquals(Ticket.TicketStatus.IN_PROGRESS, t.getStatus());
    }

    @Test
    void addStaffReply_assignedTicketFromAssignedStatus_movesToInProgress() {
        Ticket t = ticket(Ticket.TicketStatus.ASSIGNED);
        t.setAssignedTo(staff);
        when(ticketRepository.findById(10L)).thenReturn(Optional.of(t));
        when(staffUserRepository.findByEmail(STAFF_EMAIL)).thenReturn(Optional.of(staff));
        when(ticketReplyRepository.save(any())).thenAnswer(i -> {
            TicketReply r = i.getArgument(0);
            r.setId(100L);
            return r;
        });
        TicketReplyRequest req = new TicketReplyRequest();
        req.setMessage("Bắt đầu xử lý");

        service.addStaffReply(10L, STAFF_EMAIL, req);

        assertEquals(Ticket.TicketStatus.IN_PROGRESS, t.getStatus());
    }

    @Test
    void addStaffReply_unknownStaffEmail_throwsNotFound() {
        when(ticketRepository.findById(10L)).thenReturn(Optional.of(ticket(Ticket.TicketStatus.OPEN)));
        when(staffUserRepository.findByEmail("ghost@example.com")).thenReturn(Optional.empty());
        TicketReplyRequest req = new TicketReplyRequest();
        req.setMessage("Xin chào");

        assertThrows(ResourceNotFoundException.class, () -> service.addStaffReply(10L, "ghost@example.com", req));
    }

    // ── close ────────────────────────────────────────────────────────────────

    @Test
    void closeTicket_unknownActorEmail_stillClosesWithNullActor() {
        Ticket t = ticket(Ticket.TicketStatus.IN_PROGRESS);
        when(ticketRepository.findById(10L)).thenReturn(Optional.of(t));
        when(staffUserRepository.findByEmail("admin@example.com")).thenReturn(Optional.empty());

        TicketResponse response = service.closeTicket(10L, "admin@example.com");

        assertEquals("resolved", response.getStatus());
        assertNotNull(t.getResolvedAt());

        ArgumentCaptor<AdminAuditLog> logCaptor = ArgumentCaptor.forClass(AdminAuditLog.class);
        verify(adminAuditLogRepository).save(logCaptor.capture());
        assertEquals("TICKET_CLOSED", logCaptor.getValue().getAction());
        assertNull(logCaptor.getValue().getStaffActor());
    }

    @Test
    void closeStudentTicket_ownTicket_setsClosed() {
        Ticket t = ticket(Ticket.TicketStatus.IN_PROGRESS);
        when(ticketRepository.findById(10L)).thenReturn(Optional.of(t));

        TicketResponse response = service.closeStudentTicket(10L, 1L);

        assertEquals("closed", response.getStatus());
        assertNotNull(t.getResolvedAt());
    }

    @Test
    void closeStudentTicket_otherStudent_throwsForbidden() {
        when(ticketRepository.findById(10L)).thenReturn(Optional.of(ticket(Ticket.TicketStatus.OPEN)));

        assertThrows(ForbiddenException.class, () -> service.closeStudentTicket(10L, 99L));
    }

    @Test
    void closeStudentTicket_alreadyResolved_throwsTicketClosed() {
        when(ticketRepository.findById(10L)).thenReturn(Optional.of(ticket(Ticket.TicketStatus.RESOLVED)));

        assertEquals(
                "TICKET_CLOSED",
                assertThrows(BusinessException.class, () -> service.closeStudentTicket(10L, 1L))
                        .getErrorCode());
    }

    // ── assignTicket ─────────────────────────────────────────────────────────

    @Test
    void assignTicket_byManager_movesOpenToAssigned() {
        Ticket t = ticket(Ticket.TicketStatus.OPEN);
        when(staffUserRepository.findByEmail("manager@example.com")).thenReturn(Optional.of(manager));
        when(ticketRepository.findById(10L)).thenReturn(Optional.of(t));
        when(staffUserRepository.findById(2L)).thenReturn(Optional.of(staff));

        TicketResponse response = service.assignTicket(10L, 2L, "manager@example.com", false);

        assertEquals("assigned", response.getStatus());
        assertEquals(2L, response.getAssignedToStaffId());
        assertSame(staff, t.getAssignedTo());
        verify(adminAuditLogRepository).save(any(AdminAuditLog.class));
    }

    @Test
    void assignTicket_byAdmin_skipsStaffRoleCheck() {
        Ticket t = ticket(Ticket.TicketStatus.IN_PROGRESS);
        when(ticketRepository.findById(10L)).thenReturn(Optional.of(t));
        when(staffUserRepository.findById(2L)).thenReturn(Optional.of(staff));
        when(staffUserRepository.findByEmail("admin@sakuji.com")).thenReturn(Optional.empty());

        TicketResponse response = service.assignTicket(10L, 2L, "admin@sakuji.com", true);

        assertEquals("in_progress", response.getStatus());
        assertEquals(2L, response.getAssignedToStaffId());
    }

    @Test
    void assignTicket_byPlainStaff_throwsForbidden() {
        when(staffUserRepository.findByEmail(STAFF_EMAIL)).thenReturn(Optional.of(staff));

        assertThrows(ForbiddenException.class, () -> service.assignTicket(10L, 2L, STAFF_EMAIL, false));
        verifyNoInteractions(ticketRepository);
    }

    @Test
    void assignTicket_closedTicket_throwsTicketClosed() {
        when(ticketRepository.findById(10L)).thenReturn(Optional.of(ticket(Ticket.TicketStatus.CLOSED)));

        assertEquals(
                "TICKET_CLOSED",
                assertThrows(BusinessException.class, () -> service.assignTicket(10L, 2L, "admin@sakuji.com", true))
                        .getErrorCode());
    }

    @Test
    void assignTicket_targetStaffMissing_throwsNotFound() {
        when(ticketRepository.findById(10L)).thenReturn(Optional.of(ticket(Ticket.TicketStatus.OPEN)));
        when(staffUserRepository.findById(404L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.assignTicket(10L, 404L, "admin@sakuji.com", true));
    }

    @Test
    void assignTicket_suspendedTargetStaff_throwsStaffNotActive() {
        staff.setStatus(StaffUser.StaffStatus.SUSPENDED);
        when(ticketRepository.findById(10L)).thenReturn(Optional.of(ticket(Ticket.TicketStatus.OPEN)));
        when(staffUserRepository.findById(2L)).thenReturn(Optional.of(staff));

        BusinessException ex =
                assertThrows(BusinessException.class, () -> service.assignTicket(10L, 2L, "admin@sakuji.com", true));
        assertEquals(422, ex.getStatus());
        assertEquals("STAFF_NOT_ACTIVE", ex.getErrorCode());
    }

    // ── getAllTickets ────────────────────────────────────────────────────────

    @Test
    void getAllTickets_parsesStatusAndPriority() {
        when(ticketRepository.findAllByFilters(
                        eq(Ticket.TicketStatus.OPEN),
                        eq("technical"),
                        eq(Ticket.Priority.HIGH),
                        eq("loi"),
                        any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(ticket(Ticket.TicketStatus.OPEN))));

        assertEquals(
                1,
                service.getAllTickets("open", "technical", "high", "loi", 0, 10).getTotalElements());
    }

    @Test
    void getAllTickets_invalidOrNullFilters_becomeNull() {
        when(ticketRepository.findAllByFilters(isNull(), isNull(), isNull(), isNull(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));

        assertEquals(
                0,
                service.getAllTickets("khong-ton-tai", null, "sieu-gap", null, 0, 10)
                        .getTotalElements());
    }

    @Test
    void getAllTickets_nullStatusAndPriority_passNull() {
        when(ticketRepository.findAllByFilters(isNull(), isNull(), isNull(), isNull(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));

        assertTrue(service.getAllTickets(null, null, null, null, 0, 10).isEmpty());
    }

    // ── submissions (UC-31) ──────────────────────────────────────────────────

    private StudentSubmission speakingSubmission(StudentSubmission.SubmissionStatus status) {
        return StudentSubmission.builder()
                .id(50L)
                .student(student)
                .submissionType(StudentSubmission.SubmissionType.SPEAKING)
                .status(status)
                .recordingUrl("/uploads/audio/50.webm")
                .durationSeconds(42)
                .aiOverallScore(new BigDecimal("70.5"))
                .aiPronunciationScore(new BigDecimal("68.0"))
                .aiFluencyScore(new BigDecimal("72.0"))
                .submittedAt(LocalDateTime.now())
                .build();
    }

    @Test
    void getAllSubmissions_withFilters_mapsResponseUsingAiScoreWhenNotGraded() {
        when(submissionRepository.findAllByTypeAndFilters(
                        eq(StudentSubmission.SubmissionType.SPEAKING),
                        eq(StudentSubmission.SubmissionStatus.AI_GRADED),
                        any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(speakingSubmission(StudentSubmission.SubmissionStatus.AI_GRADED))));

        Page<SubmissionResponse> page = service.getAllSubmissions("speaking", "ai_graded", 0, 10);

        SubmissionResponse row = page.getContent().get(0);
        assertEquals(50L, row.getSubmissionId());
        assertEquals("Student", row.getStudentName());
        assertEquals("N4", row.getJlptLevel());
        assertEquals(new BigDecimal("70.5"), row.getFinalScore());
        assertNull(row.getGradedBy());
    }

    @Test
    void getAllSubmissions_blankFilters_passNullToRepository() {
        StudentSubmission graded = speakingSubmission(StudentSubmission.SubmissionStatus.GRADED);
        graded.setManualScore(new BigDecimal("88.0"));
        graded.setGradedBy(staff);
        student.setCurrentJlptLevel(null);
        when(submissionRepository.findAllByTypeAndFilters(isNull(), isNull(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(graded)));

        SubmissionResponse row =
                service.getAllSubmissions("  ", "", 0, 10).getContent().get(0);

        assertEquals(new BigDecimal("88.0"), row.getFinalScore());
        assertEquals("Staff", row.getGradedBy());
        assertNull(row.getJlptLevel());
    }

    @Test
    void getAllSubmissions_nullFilters_passNullToRepository() {
        when(submissionRepository.findAllByTypeAndFilters(isNull(), isNull(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));

        assertTrue(service.getAllSubmissions(null, null, 0, 10).isEmpty());
    }

    @Test
    void getAllSubmissions_withoutSubmissionModule_returnsEmptyPage() {
        ReflectionTestUtils.setField(service, "submissionRepository", null);

        assertTrue(service.getAllSubmissions("speaking", null, 0, 10).isEmpty());
    }

    @Test
    void getSubmissionDetail_found_mapsResponse() {
        when(submissionRepository.findById(50L))
                .thenReturn(Optional.of(speakingSubmission(StudentSubmission.SubmissionStatus.AI_GRADED)));

        assertEquals(50L, service.getSubmissionDetail(50L).getSubmissionId());
    }

    @Test
    void getSubmissionDetail_missing_throwsNotFound() {
        when(submissionRepository.findById(404L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.getSubmissionDetail(404L));
    }

    @Test
    void getSubmissionDetail_withoutSubmissionModule_throwsServiceUnavailable() {
        ReflectionTestUtils.setField(service, "submissionRepository", null);

        BusinessException ex = assertThrows(BusinessException.class, () -> service.getSubmissionDetail(50L));
        assertEquals(503, ex.getStatus());
        assertEquals("SERVICE_UNAVAILABLE", ex.getErrorCode());
    }

    // ── manualGrade ──────────────────────────────────────────────────────────

    private ManualGradeRequest gradeRequest(String score) {
        ManualGradeRequest req = new ManualGradeRequest();
        req.setManualScore(new BigDecimal(score));
        req.setManualFeedback("Phát âm tốt, cần luyện thêm ngữ điệu");
        return req;
    }

    @Test
    void manualGrade_speakingSubmission_savesScoreAndNotifiesStudent() {
        StudentSubmission submission = speakingSubmission(StudentSubmission.SubmissionStatus.AI_GRADED);
        when(submissionRepository.findById(50L)).thenReturn(Optional.of(submission));
        when(staffUserRepository.findByEmail(STAFF_EMAIL)).thenReturn(Optional.of(staff));

        GradeResponse response = service.manualGrade(50L, STAFF_EMAIL, gradeRequest("88.5"));

        assertEquals(new BigDecimal("88.5"), response.getManualScore());
        assertEquals(new BigDecimal("88.5"), response.getFinalScore());
        assertEquals("graded", response.getStatus());
        assertEquals("Staff", response.getGradedByStaffName());
        assertEquals(StudentSubmission.SubmissionStatus.GRADED, submission.getStatus());
        assertSame(staff, submission.getGradedBy());
        assertNotNull(submission.getGradedAt());
        verify(submissionRepository).save(submission);
        verify(notificationService)
                .notifyStudent(
                        eq(student),
                        anyString(),
                        anyString(),
                        eq(Notification.NotificationType.ACHIEVEMENT),
                        eq("speaking_graded_50"),
                        eq(staff));
        verify(adminAuditLogRepository).save(any(AdminAuditLog.class));
    }

    @Test
    void manualGrade_handwritingSubmission_throwsInvalidSubmissionType() {
        StudentSubmission submission = speakingSubmission(StudentSubmission.SubmissionStatus.AI_GRADED);
        submission.setSubmissionType(StudentSubmission.SubmissionType.HANDWRITING);
        when(submissionRepository.findById(50L)).thenReturn(Optional.of(submission));
        ManualGradeRequest req = gradeRequest("88.5");

        BusinessException ex = assertThrows(BusinessException.class, () -> service.manualGrade(50L, STAFF_EMAIL, req));
        assertEquals(422, ex.getStatus());
        assertEquals("INVALID_SUBMISSION_TYPE", ex.getErrorCode());
    }

    @Test
    void manualGrade_rejectedSubmission_throwsInvalidStatus() {
        when(submissionRepository.findById(50L))
                .thenReturn(Optional.of(speakingSubmission(StudentSubmission.SubmissionStatus.REJECTED)));
        ManualGradeRequest req = gradeRequest("88.5");

        assertEquals(
                "INVALID_STATUS",
                assertThrows(BusinessException.class, () -> service.manualGrade(50L, STAFF_EMAIL, req))
                        .getErrorCode());
    }

    @Test
    void manualGrade_submissionMissing_throwsNotFound() {
        when(submissionRepository.findById(404L)).thenReturn(Optional.empty());
        ManualGradeRequest req = gradeRequest("88.5");

        assertThrows(ResourceNotFoundException.class, () -> service.manualGrade(404L, STAFF_EMAIL, req));
    }

    @Test
    void manualGrade_withoutSubmissionModule_throwsServiceUnavailable() {
        ReflectionTestUtils.setField(service, "submissionRepository", null);
        ManualGradeRequest req = gradeRequest("88.5");

        assertEquals(
                "SERVICE_UNAVAILABLE",
                assertThrows(BusinessException.class, () -> service.manualGrade(50L, STAFF_EMAIL, req))
                        .getErrorCode());
    }
}
