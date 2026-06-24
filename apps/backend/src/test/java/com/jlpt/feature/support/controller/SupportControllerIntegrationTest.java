/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.support.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jlpt.feature.staff.entity.StaffUser;
import com.jlpt.feature.student.entity.StudentUser;
import com.jlpt.feature.support.entity.Ticket;
import com.jlpt.feature.support.dto.TicketRequest;
import com.jlpt.feature.support.repository.TicketReplyRepository;
import com.jlpt.feature.support.repository.TicketRepository;
import com.jlpt.shared.audit.AdminAuditLogRepository;
import com.jlpt.feature.staff.repository.StaffUserRepository;
import com.jlpt.feature.corelearning.repository.StudentSubmissionRepository;
import com.jlpt.feature.student.repository.StudentUserRepository;
import com.jlpt.shared.security.JwtProvider;
import com.jlpt.feature.notification.repository.NotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class SupportControllerIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private JwtProvider jwtProvider;
    @Autowired private StudentUserRepository studentUserRepository;
    @Autowired private StaffUserRepository staffUserRepository;
    @Autowired private TicketRepository ticketRepository;
    @Autowired private TicketReplyRepository ticketReplyRepository;
    @Autowired private NotificationRepository notificationRepository;
    @Autowired private StudentSubmissionRepository studentSubmissionRepository;
    @Autowired private AdminAuditLogRepository adminAuditLogRepository;

    private StudentUser owner;
    private StudentUser other;
    private String ownerToken;
    private String staffToken;

    @BeforeEach
    void setUp() {
        adminAuditLogRepository.deleteAll();
        studentSubmissionRepository.deleteAll();
        notificationRepository.deleteAll();
        ticketReplyRepository.deleteAll();
        ticketRepository.deleteAll();
        studentUserRepository.deleteAll();
        staffUserRepository.deleteAll();

        owner = studentUserRepository.save(StudentUser.builder()
                .email("owner@example.com")
                .fullName("Owner Student")
                .status(StudentUser.StudentStatus.ACTIVE)
                .build());
        other = studentUserRepository.save(StudentUser.builder()
                .email("other@example.com")
                .fullName("Other Student")
                .status(StudentUser.StudentStatus.ACTIVE)
                .build());
        StaffUser staff = staffUserRepository.save(StaffUser.builder()
                .email("staff@example.com")
                .fullName("Staff Member")
                .staffRole(StaffUser.StaffRole.STAFF)
                .status(StaffUser.StaffStatus.ACTIVE)
                .build());

        ownerToken = jwtProvider.generateTokenFromUsername(owner.getEmail(), 900000);
        staffToken = jwtProvider.generateStaffAccessToken(staff.getId(), staff.getEmail());
    }

    @Test
    void createTicket_returns201Created() throws Exception {
        TicketRequest req = new TicketRequest();
        req.setSubject("Không thể phát âm bài luyện nói bài 2");
        req.setContent("Khi nhấn nút Record, hệ thống báo lỗi không tìm thấy microphone.");
        req.setCategory("technical");
        req.setPriority("high");

        mockMvc.perform(post("/api/support/tickets")
                        .header("Authorization", "Bearer " + ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.data.status").value("open"));
    }

    @Test
    void getTicketDetail_notOwner_returns403() throws Exception {
        Ticket ticket = ticketRepository.save(Ticket.builder()
                .student(owner)
                .subject("S")
                .content("C")
                .build());
        String otherToken = jwtProvider.generateTokenFromUsername(other.getEmail(), 900000);

        mockMvc.perform(get("/api/support/tickets/" + ticket.getId())
                        .header("Authorization", "Bearer " + otherToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void replyToClosedTicket_returns409() throws Exception {
        Ticket ticket = ticketRepository.save(Ticket.builder()
                .student(owner)
                .subject("S")
                .content("C")
                .status(Ticket.TicketStatus.CLOSED)
                .build());

        mockMvc.perform(post("/api/support/tickets/" + ticket.getId() + "/reply")
                        .header("Authorization", "Bearer " + ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"message\":\"hello\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void staffJwt_callingStudentTicketCreate_returns403() throws Exception {
        TicketRequest req = new TicketRequest();
        req.setSubject("S");
        req.setContent("C");

        mockMvc.perform(post("/api/support/tickets")
                        .header("Authorization", "Bearer " + staffToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden());
    }

    @Test
    void getMyNotifications_returnsUnreadCount() throws Exception {
        mockMvc.perform(get("/api/notifications")
                        .header("Authorization", "Bearer " + ownerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.unreadCount").exists());
    }
}
