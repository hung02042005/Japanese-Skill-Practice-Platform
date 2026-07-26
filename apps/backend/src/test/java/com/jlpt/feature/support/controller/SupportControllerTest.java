/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.support.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.jlpt.feature.student.StudentUser;
import com.jlpt.feature.support.dto.TicketDetailResponse;
import com.jlpt.feature.support.dto.TicketReplyRequest;
import com.jlpt.feature.support.dto.TicketReplyResponse;
import com.jlpt.feature.support.dto.TicketRequest;
import com.jlpt.feature.support.dto.TicketResponse;
import com.jlpt.feature.support.service.SupportTicketService;
import com.jlpt.shared.common.ApiResponse;
import com.jlpt.shared.security.UserDetailsImpl;
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

/**
 * UC-29 — SupportController: studentId luôn lấy từ principal (không nhận từ client) để service tự
 * kiểm tra quyền sở hữu ticket.
 */
@ExtendWith(MockitoExtension.class)
class SupportControllerTest {

    private static final Long STUDENT_ID = 1L;

    @Mock
    private SupportTicketService supportTicketService;

    @InjectMocks
    private SupportController controller;

    private UserDetailsImpl principal;

    @BeforeEach
    void setUp() {
        principal = new UserDetailsImpl(StudentUser.builder()
                .id(STUDENT_ID)
                .email("student@sakuji.com")
                .status(StudentUser.StudentStatus.ACTIVE)
                .build());
    }

    @Test
    void createTicket_returns201Created() {
        TicketRequest request = new TicketRequest();
        TicketResponse result = TicketResponse.builder().ticketId(10L).build();
        when(supportTicketService.createTicket(STUDENT_ID, request)).thenReturn(result);

        ResponseEntity<ApiResponse<TicketResponse>> response = controller.createTicket(principal, request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("Ticket hỗ trợ đã được tạo thành công", response.getBody().getMessage());
        assertSame(result, response.getBody().getData());
    }

    @Test
    void getMyTickets_wrapsPageIntoContentAndTotals() {
        TicketResponse row = TicketResponse.builder().ticketId(10L).build();
        when(supportTicketService.getMyTickets(STUDENT_ID, "open", 0, 10)).thenReturn(new PageImpl<>(List.of(row)));

        ResponseEntity<ApiResponse<Map<String, Object>>> response = controller.getMyTickets(principal, "open", 0, 10);

        Map<String, Object> data = response.getBody().getData();
        assertEquals(List.of(row), data.get("content"));
        assertEquals(1L, data.get("totalElements"));
        assertEquals(1, data.get("totalPages"));
    }

    @Test
    void getMyTickets_withoutStatusFilter_passesNull() {
        when(supportTicketService.getMyTickets(STUDENT_ID, null, 0, 10)).thenReturn(new PageImpl<>(List.of()));

        assertEquals(
                0L,
                controller
                        .getMyTickets(principal, null, 0, 10)
                        .getBody()
                        .getData()
                        .get("totalElements"));
    }

    @Test
    void getTicketDetail_usesPrincipalForOwnershipCheck() {
        TicketDetailResponse result =
                TicketDetailResponse.builder().ticketId(10L).build();
        when(supportTicketService.getStudentTicketDetail(10L, STUDENT_ID)).thenReturn(result);

        assertSame(result, controller.getTicketDetail(principal, 10L).getBody().getData());
        verify(supportTicketService).getStudentTicketDetail(10L, STUDENT_ID);
    }

    @Test
    void replyToTicket_delegates() {
        TicketReplyRequest request = new TicketReplyRequest();
        TicketReplyResponse result = TicketReplyResponse.builder().replyId(100L).build();
        when(supportTicketService.addStudentReply(10L, STUDENT_ID, request)).thenReturn(result);

        ResponseEntity<ApiResponse<TicketReplyResponse>> response = controller.replyToTicket(principal, 10L, request);

        assertEquals("Gửi phản hồi thành công", response.getBody().getMessage());
        assertSame(result, response.getBody().getData());
    }

    @Test
    void closeTicket_delegates() {
        TicketResponse result =
                TicketResponse.builder().ticketId(10L).status("closed").build();
        when(supportTicketService.closeStudentTicket(10L, STUDENT_ID)).thenReturn(result);

        ResponseEntity<ApiResponse<TicketResponse>> response = controller.closeTicket(principal, 10L);

        assertEquals("Ticket đã được đóng", response.getBody().getMessage());
        assertEquals("closed", response.getBody().getData().getStatus());
    }
}
