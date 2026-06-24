/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.admin.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jlpt.feature.staff.entity.StaffUser;
import com.jlpt.feature.corelearning.entity.StudentSubmission;
import com.jlpt.feature.student.entity.StudentUser;
import com.jlpt.feature.support.repository.TicketReplyRepository;
import com.jlpt.feature.support.repository.TicketRepository;
import com.jlpt.shared.audit.AdminAuditLogRepository;
import com.jlpt.feature.staff.repository.StaffUserRepository;
import com.jlpt.feature.corelearning.repository.StudentSubmissionRepository;
import com.jlpt.feature.student.repository.StudentUserRepository;
import com.jlpt.shared.security.JwtProvider;
import com.jlpt.feature.notification.repository.NotificationRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class StaffTicketIntegrationTest {

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

    private StudentUser student;
    private StaffUser staff;
    private String staffToken;
    private String studentToken;

    @BeforeEach
    void setUp() {
        adminAuditLogRepository.deleteAll();
        studentSubmissionRepository.deleteAll();
        notificationRepository.deleteAll();
        ticketReplyRepository.deleteAll();
        ticketRepository.deleteAll();
        studentUserRepository.deleteAll();
        staffUserRepository.deleteAll();

        student = studentUserRepository.save(StudentUser.builder()
                .email("student@example.com")
                .fullName("Nguyen Van A")
                .status(StudentUser.StudentStatus.ACTIVE)
                .build());
        staff = staffUserRepository.save(StaffUser.builder()
                .email("staff@example.com")
                .fullName("Tran Thi B")
                .staffRole(StaffUser.StaffRole.STAFF)
                .status(StaffUser.StaffStatus.ACTIVE)
                .build());

        staffToken = jwtProvider.generateStaffAccessToken(staff.getId(), staff.getEmail());
        studentToken = jwtProvider.generateTokenFromUsername(student.getEmail(), 900000);
    }

    @AfterEach
    void tearDown() {
        // Submissions created by the LAST test method in this class would otherwise survive
        // into the next @SpringBootTest class sharing the same cached context/H2 database,
        // causing a FK violation when that class deletes student_users.
        studentSubmissionRepository.deleteAll();
    }

    private StudentSubmission saveSubmission(
            StudentSubmission.SubmissionType type, StudentSubmission.SubmissionStatus status) {
        return studentSubmissionRepository.save(StudentSubmission.builder()
                .student(student)
                .submissionType(type)
                .status(status)
                .aiOverallScore(new BigDecimal("72.50"))
                .submittedAt(LocalDateTime.now())
                .build());
    }

    @Test
    void gradeSpeakingAiGraded_returns200() throws Exception {
        StudentSubmission submission = saveSubmission(
                StudentSubmission.SubmissionType.SPEAKING, StudentSubmission.SubmissionStatus.AI_GRADED);

        mockMvc.perform(post("/api/staff/submissions/" + submission.getId() + "/grade")
                        .header("Authorization", "Bearer " + staffToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"manualScore\":85.50,\"manualFeedback\":\"Tot\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.finalScore").value(85.50))
                .andExpect(jsonPath("$.data.status").value("graded"));
    }

    @Test
    void gradeHandwriting_returns422() throws Exception {
        StudentSubmission submission = saveSubmission(
                StudentSubmission.SubmissionType.HANDWRITING, StudentSubmission.SubmissionStatus.AI_GRADED);

        mockMvc.perform(post("/api/staff/submissions/" + submission.getId() + "/grade")
                        .header("Authorization", "Bearer " + staffToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"manualScore\":85.50}"))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void gradeScoreBelowZero_returns400() throws Exception {
        StudentSubmission submission = saveSubmission(
                StudentSubmission.SubmissionType.SPEAKING, StudentSubmission.SubmissionStatus.AI_GRADED);

        mockMvc.perform(post("/api/staff/submissions/" + submission.getId() + "/grade")
                        .header("Authorization", "Bearer " + staffToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"manualScore\":-1}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void broadcastNotification_returns201WithJobId() throws Exception {
        String body = """
                {
                  "title": "Bao tri he thong",
                  "content": "He thong bao tri",
                  "notificationType": "warning",
                  "channel": "both"
                }
                """;

        long start = System.currentTimeMillis();
        mockMvc.perform(post("/api/staff/notifications")
                        .header("Authorization", "Bearer " + staffToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.jobId").exists());
        long elapsed = System.currentTimeMillis() - start;
        org.junit.jupiter.api.Assertions.assertTrue(elapsed < 500, "broadcast must respond quickly");
    }

    @Test
    void studentJwt_callingStaffTickets_returns403() throws Exception {
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .get("/api/staff/tickets")
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void assignTicket_asPlainStaff_returns403() throws Exception {
        var ticket = ticketRepository.save(com.jlpt.feature.support.entity.Ticket.builder()
                .student(student)
                .subject("S")
                .content("C")
                .build());

        mockMvc.perform(post("/api/staff/tickets/" + ticket.getId() + "/assign")
                        .header("Authorization", "Bearer " + staffToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"assignToStaffId\":" + staff.getId() + "}"))
                .andExpect(status().isForbidden());
    }
}
