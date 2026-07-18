/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.support.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.jlpt.feature.admin.AdminAuditLogRepository;
import com.jlpt.feature.notification.Notification;
import com.jlpt.feature.notification.service.NotificationService;
import com.jlpt.feature.staff.StaffUser;
import com.jlpt.feature.staff.StaffUserRepository;
import com.jlpt.feature.student.StudentUser;
import com.jlpt.feature.student.StudentUserRepository;
import com.jlpt.feature.support.Ticket;
import com.jlpt.feature.support.TicketReply;
import com.jlpt.feature.support.dto.TicketReplyRequest;
import com.jlpt.feature.support.dto.TicketReplyResponse;
import com.jlpt.feature.support.dto.TicketRequest;
import com.jlpt.feature.support.dto.TicketResponse;
import com.jlpt.feature.support.repository.TicketReplyRepository;
import com.jlpt.feature.support.repository.TicketRepository;
import com.jlpt.shared.exception.BusinessException;
import com.jlpt.shared.exception.ForbiddenException;
import com.jlpt.shared.exception.ResourceNotFoundException;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Test các quy tắc phân quyền/trạng thái nhạy cảm nhất của SupportTicketService: học viên chỉ
 * thao tác được trên ticket của chính mình, staff chỉ trả lời được ticket được giao (hoặc là
 * Manager), và không ai thao tác được trên ticket đã đóng.
 */
@ExtendWith(MockitoExtension.class)
class SupportTicketServiceTest {

    private static final Long STUDENT_ID = 1L;
    private static final Long OTHER_STUDENT_ID = 2L;
    private static final Long TICKET_ID = 100L;

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

    @InjectMocks
    private SupportTicketService supportTicketService;

    private StudentUser owner;
    private Ticket openTicket;

    @BeforeEach
    void setUp() {
        owner = StudentUser.builder()
                .id(STUDENT_ID)
                .email("owner@example.com")
                .fullName("Owner Student")
                .build();
        openTicket = Ticket.builder()
                .id(TICKET_ID)
                .student(owner)
                .subject("Không đăng nhập được")
                .content("Lỗi 401 liên tục")
                .status(Ticket.TicketStatus.OPEN)
                .priority(Ticket.Priority.NORMAL)
                .build();
    }

    // ── createTicket ─────────────────────────────────────────────────────────

    @Test
    void createTicket_Success_StatusOpen() {
        when(studentUserRepository.findById(STUDENT_ID)).thenReturn(Optional.of(owner));
        when(ticketReplyRepository.countByTicketId(any())).thenReturn(0L);

        TicketRequest req = new TicketRequest();
        req.setSubject("Không đăng nhập được");
        req.setContent("Lỗi 401 liên tục");
        req.setPriority("high");

        TicketResponse response = supportTicketService.createTicket(STUDENT_ID, req);

        assertEquals("open", response.getStatus());
        assertEquals("high", response.getPriority());
        verify(ticketRepository).save(any(Ticket.class));
    }

    // ── addStudentReply ──────────────────────────────────────────────────────

    @Test
    void addStudentReply_NotOwner_ThrowsForbidden() {
        when(ticketRepository.findById(TICKET_ID)).thenReturn(Optional.of(openTicket));
        TicketReplyRequest req = new TicketReplyRequest();
        req.setMessage("Xin chào");

        assertThrows(
                ForbiddenException.class, () -> supportTicketService.addStudentReply(TICKET_ID, OTHER_STUDENT_ID, req));
        verify(ticketReplyRepository, never()).save(any());
    }

    @Test
    void addStudentReply_TicketAlreadyResolved_ThrowsBusinessException() {
        openTicket.setStatus(Ticket.TicketStatus.RESOLVED);
        when(ticketRepository.findById(TICKET_ID)).thenReturn(Optional.of(openTicket));
        TicketReplyRequest req = new TicketReplyRequest();
        req.setMessage("Xin chào");

        BusinessException ex = assertThrows(
                BusinessException.class, () -> supportTicketService.addStudentReply(TICKET_ID, STUDENT_ID, req));
        assertEquals("TICKET_CLOSED", ex.getErrorCode());
        verify(ticketReplyRepository, never()).save(any());
    }

    @Test
    void addStudentReply_Owner_Success() {
        when(ticketRepository.findById(TICKET_ID)).thenReturn(Optional.of(openTicket));
        when(studentUserRepository.findById(STUDENT_ID)).thenReturn(Optional.of(owner));
        when(ticketReplyRepository.save(any(TicketReply.class))).thenAnswer(inv -> {
            TicketReply r = inv.getArgument(0);
            r.setId(500L);
            return r;
        });
        TicketReplyRequest req = new TicketReplyRequest();
        req.setMessage("Đây là chi tiết lỗi");

        TicketReplyResponse response = supportTicketService.addStudentReply(TICKET_ID, STUDENT_ID, req);

        assertEquals("STUDENT", response.getSenderRole());
        assertNotNull(openTicket.getLastReplyAt());
        verify(ticketRepository).save(openTicket);
    }

    // ── addStaffReply ────────────────────────────────────────────────────────

    @Test
    void addStaffReply_NeitherAssigneeNorManager_ThrowsForbidden() {
        StaffUser unrelatedStaff = StaffUser.builder()
                .id(99L)
                .email("random@staff.com")
                .fullName("Random Staff")
                .staffRole(StaffUser.StaffRole.STAFF)
                .build();
        StaffUser assignee = StaffUser.builder().id(5L).build();
        openTicket.setAssignedTo(assignee);

        when(ticketRepository.findById(TICKET_ID)).thenReturn(Optional.of(openTicket));
        when(staffUserRepository.findByEmail("random@staff.com")).thenReturn(Optional.of(unrelatedStaff));
        TicketReplyRequest req = new TicketReplyRequest();
        req.setMessage("Tôi trả lời hộ nhé");

        assertThrows(
                ForbiddenException.class, () -> supportTicketService.addStaffReply(TICKET_ID, "random@staff.com", req));
        verify(ticketReplyRepository, never()).save(any());
    }

    @Test
    void addStaffReply_Assignee_Success_NotifiesStudent() {
        StaffUser assignee = StaffUser.builder()
                .id(5L)
                .email("assignee@staff.com")
                .fullName("Assignee Staff")
                .staffRole(StaffUser.StaffRole.STAFF)
                .build();
        openTicket.setAssignedTo(assignee);
        openTicket.setStatus(Ticket.TicketStatus.ASSIGNED);

        when(ticketRepository.findById(TICKET_ID)).thenReturn(Optional.of(openTicket));
        when(staffUserRepository.findByEmail("assignee@staff.com")).thenReturn(Optional.of(assignee));
        when(ticketReplyRepository.save(any(TicketReply.class))).thenAnswer(inv -> {
            TicketReply r = inv.getArgument(0);
            r.setId(501L);
            return r;
        });
        TicketReplyRequest req = new TicketReplyRequest();
        req.setMessage("Đã kiểm tra, đang xử lý");

        TicketReplyResponse response = supportTicketService.addStaffReply(TICKET_ID, "assignee@staff.com", req);

        assertEquals("STAFF", response.getSenderRole());
        assertEquals(Ticket.TicketStatus.IN_PROGRESS, openTicket.getStatus());
        verify(notificationService)
                .notifyStudent(eq(owner), any(), any(), eq(Notification.NotificationType.SYSTEM), any(), eq(assignee));
    }

    @Test
    void addStaffReply_Manager_CanReplyEvenIfNotAssignee() {
        StaffUser manager = StaffUser.builder()
                .id(7L)
                .email("manager@staff.com")
                .fullName("Manager")
                .staffRole(StaffUser.StaffRole.STAFF_MANAGER)
                .build();
        // openTicket.assignedTo vẫn null — Manager không cần được giao vẫn trả lời được.
        when(ticketRepository.findById(TICKET_ID)).thenReturn(Optional.of(openTicket));
        when(staffUserRepository.findByEmail("manager@staff.com")).thenReturn(Optional.of(manager));
        when(ticketReplyRepository.save(any(TicketReply.class))).thenAnswer(inv -> {
            TicketReply r = inv.getArgument(0);
            r.setId(502L);
            return r;
        });
        TicketReplyRequest req = new TicketReplyRequest();
        req.setMessage("Manager can thiep");

        assertDoesNotThrow(() -> supportTicketService.addStaffReply(TICKET_ID, "manager@staff.com", req));
    }

    // ── closeStudentTicket ───────────────────────────────────────────────────

    @Test
    void closeStudentTicket_NotOwner_ThrowsForbidden() {
        when(ticketRepository.findById(TICKET_ID)).thenReturn(Optional.of(openTicket));

        assertThrows(
                ForbiddenException.class, () -> supportTicketService.closeStudentTicket(TICKET_ID, OTHER_STUDENT_ID));
        verify(ticketRepository, never()).save(any());
    }

    @Test
    void closeStudentTicket_Owner_Success() {
        when(ticketRepository.findById(TICKET_ID)).thenReturn(Optional.of(openTicket));

        TicketResponse response = supportTicketService.closeStudentTicket(TICKET_ID, STUDENT_ID);

        assertEquals("closed", response.getStatus());
        assertNotNull(openTicket.getResolvedAt());
    }

    // ── assignTicket ─────────────────────────────────────────────────────────

    @Test
    void assignTicket_PlainStaffNotAdmin_ThrowsForbidden() {
        StaffUser plainStaff = StaffUser.builder()
                .id(8L)
                .email("plain@staff.com")
                .staffRole(StaffUser.StaffRole.STAFF)
                .build();
        when(staffUserRepository.findByEmail("plain@staff.com")).thenReturn(Optional.of(plainStaff));

        assertThrows(
                ForbiddenException.class,
                () -> supportTicketService.assignTicket(TICKET_ID, 5L, "plain@staff.com", false));
        verify(ticketRepository, never()).findById(any());
    }

    @Test
    void assignTicket_TargetStaffNotFound_ThrowsResourceNotFound() {
        when(ticketRepository.findById(TICKET_ID)).thenReturn(Optional.of(openTicket));
        when(staffUserRepository.findById(5L)).thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> supportTicketService.assignTicket(TICKET_ID, 5L, "admin@sakuji.com", true));
    }
}
