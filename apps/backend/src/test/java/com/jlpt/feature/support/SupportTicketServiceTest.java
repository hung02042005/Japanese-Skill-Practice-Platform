/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.support;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.jlpt.feature.admin.AdminAuditLogRepository;
import com.jlpt.feature.notification.service.NotificationService;
import com.jlpt.feature.staff.StaffUser;
import com.jlpt.feature.staff.StaffUserRepository;
import com.jlpt.feature.student.StudentUser;
import com.jlpt.feature.student.StudentUserRepository;
import com.jlpt.feature.support.dto.*;
import com.jlpt.feature.support.repository.TicketReplyRepository;
import com.jlpt.feature.support.repository.TicketRepository;
import com.jlpt.feature.support.service.SupportTicketService;
import com.jlpt.shared.exception.ForbiddenException;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SupportTicketServiceTest {

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
    private SupportTicketService service;

    private StudentUser student;
    private StaffUser staff;

    @BeforeEach
    void setUp() {
        student = StudentUser.builder()
                .id(1L)
                .email("student@example.com")
                .fullName("Student")
                .build();
        staff = StaffUser.builder()
                .id(2L)
                .email("staff@example.com")
                .fullName("Staff")
                .staffRole(StaffUser.StaffRole.STAFF)
                .build();
    }

    @Test
    void createTicket_success() {
        when(studentUserRepository.findById(1L)).thenReturn(Optional.of(student));
        when(ticketRepository.save(any())).thenAnswer(i -> {
            Ticket t = i.getArgument(0);
            t.setId(10L);
            return t;
        });

        TicketRequest req = new TicketRequest();
        req.setSubject("Help");
        req.setContent("Need assistance");
        req.setPriority("HIGH");

        TicketResponse res = service.createTicket(1L, req);
        assertEquals(10L, res.getTicketId());
        assertEquals("Help", res.getSubject());
    }

    @Test
    void getStudentTicketDetail_forbiddenForOtherStudent() {
        Ticket ticket = Ticket.builder()
                .id(10L)
                .student(StudentUser.builder().id(99L).build())
                .build();
        when(ticketRepository.findById(10L)).thenReturn(Optional.of(ticket));

        assertThrows(ForbiddenException.class, () -> service.getStudentTicketDetail(10L, 1L));
    }

    @Test
    void addStaffReply_success() {
        Ticket ticket = Ticket.builder()
                .id(10L)
                .student(student)
                .subject("Help")
                .status(Ticket.TicketStatus.OPEN)
                .assignedTo(staff)
                .build();
        when(ticketRepository.findById(10L)).thenReturn(Optional.of(ticket));
        when(staffUserRepository.findByEmail("staff@example.com")).thenReturn(Optional.of(staff));
        when(ticketReplyRepository.save(any())).thenAnswer(i -> {
            TicketReply r = i.getArgument(0);
            r.setId(100L);
            return r;
        });

        TicketReplyRequest req = new TicketReplyRequest();
        req.setMessage("Reply");

        TicketReplyResponse res = service.addStaffReply(10L, "staff@example.com", req);
        assertEquals("STAFF", res.getSenderRole());
        assertEquals("Reply", res.getMessage());
    }

    @Test
    void closeTicket_success() {
        Ticket ticket = Ticket.builder()
                .id(10L)
                .student(student)
                .subject("Help")
                .status(Ticket.TicketStatus.IN_PROGRESS)
                .build();
        when(ticketRepository.findById(10L)).thenReturn(Optional.of(ticket));
        when(staffUserRepository.findByEmail("staff@example.com")).thenReturn(Optional.of(staff));

        TicketResponse res = service.closeTicket(10L, "staff@example.com");
        assertEquals("resolved", res.getStatus());
    }
}
