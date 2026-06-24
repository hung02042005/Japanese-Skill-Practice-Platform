/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.studentmanagement.controller;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jlpt.feature.staff.entity.StaffUser;
import com.jlpt.feature.student.entity.StudentUser;
import com.jlpt.shared.audit.AdminAuditLogRepository;
import com.jlpt.feature.auth.repository.AuthTokenRepository;
import com.jlpt.feature.staff.repository.StaffUserRepository;
import com.jlpt.feature.student.repository.StudentUserRepository;
import com.jlpt.shared.security.JwtProvider;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Integration tests for StaffStudentController.
 * Covers UC-21, UC-22, UC-23 — real Spring context with in-memory H2 database.
 */
@SpringBootTest
@AutoConfigureMockMvc
class StaffStudentControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private JwtProvider jwtProvider;
    @Autowired private StudentUserRepository studentUserRepository;
    @Autowired private StaffUserRepository staffUserRepository;
    @Autowired private AuthTokenRepository authTokenRepository;
    @Autowired private AdminAuditLogRepository adminAuditLogRepository;

    private StudentUser activeStudent;
    private StudentUser suspendedStudent;
    private StaffUser staff;
    private String staffToken;
    private String studentToken;

    @BeforeEach
    void setUp() {
        // Clean up in reverse FK order
        adminAuditLogRepository.deleteAll();
        authTokenRepository.deleteAll();
        studentUserRepository.deleteAll();
        staffUserRepository.deleteAll();

        activeStudent = studentUserRepository.save(StudentUser.builder()
                .email("active.student@example.com")
                .fullName("Nguyen Van Active")
                .status(StudentUser.StudentStatus.ACTIVE)
                .currentStreak(5)
                .longestStreak(14)
                .build());

        suspendedStudent = studentUserRepository.save(StudentUser.builder()
                .email("suspended.student@example.com")
                .fullName("Tran Thi Suspended")
                .status(StudentUser.StudentStatus.SUSPENDED)
                .suspendReason("Previous violation of TOS - account suspended by staff.")
                .currentStreak(0)
                .longestStreak(10)
                .build());

        staff = staffUserRepository.save(StaffUser.builder()
                .email("staff@example.com")
                .fullName("Staff Member")
                .staffRole(StaffUser.StaffRole.STAFF)
                .status(StaffUser.StaffStatus.ACTIVE)
                .build());

        staffToken  = jwtProvider.generateStaffAccessToken(staff.getId(), staff.getEmail());
        studentToken = jwtProvider.generateTokenFromUsername(activeStudent.getEmail(), 900000);
    }

    // ── UC-22: GET /api/staff/students ────────────────────────────────────────

    @Test
    @DisplayName("GET /staff/students → 200 paginated list")
    void listStudents_asStaff_returns200Paginated() throws Exception {
        mockMvc.perform(get("/api/staff/students")
                        .header("Authorization", "Bearer " + staffToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.totalElements").isNumber());
    }

    @Test
    @DisplayName("GET /staff/students?status=suspended → returns only suspended students")
    void listStudents_withStatusFilter_returnsOnlyMatchingStudents() throws Exception {
        mockMvc.perform(get("/api/staff/students")
                        .param("status", "suspended")
                        .header("Authorization", "Bearer " + staffToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[*].status", everyItem(is("suspended"))));
    }

    // ── UC-21: GET /api/staff/students/{id} ───────────────────────────────────

    @Test
    @DisplayName("GET /staff/students/{id} → 200, no passwordHash in response")
    void getStudentDetail_returnsProfileWithoutPasswordHash() throws Exception {
        mockMvc.perform(get("/api/staff/students/" + activeStudent.getId())
                        .header("Authorization", "Bearer " + staffToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.studentId").value(activeStudent.getId()))
                .andExpect(jsonPath("$.data.fullName").value("Nguyen Van Active"))
                .andExpect(jsonPath("$.data.email").value("active.student@example.com"))
                .andExpect(jsonPath("$.data.status").value("active"))
                // NFR-STUDENT-03: passwordHash must NOT appear in response
                .andExpect(jsonPath("$.data.passwordHash").doesNotExist())
                .andExpect(jsonPath("$.data.password_hash").doesNotExist());
    }

    @Test
    @DisplayName("GET /staff/students/9999 → 404 STUDENT_NOT_FOUND")
    void getStudentDetail_withInvalidId_returns404() throws Exception {
        mockMvc.perform(get("/api/staff/students/9999")
                        .header("Authorization", "Bearer " + staffToken))
                .andExpect(status().isNotFound());
    }

    // ── UC-21: GET /api/staff/students/{id}/progress ─────────────────────────

    @Test
    @DisplayName("GET /staff/students/{id}/progress → 200 with progress data")
    void getStudentProgress_returns200() throws Exception {
        mockMvc.perform(get("/api/staff/students/" + activeStudent.getId() + "/progress")
                        .header("Authorization", "Bearer " + staffToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.studentId").value(activeStudent.getId()))
                .andExpect(jsonPath("$.data.fullName").value("Nguyen Van Active"));
    }

    // ── UC-21: GET /api/staff/students/{id}/submissions ───────────────────────

    @Test
    @DisplayName("GET /staff/students/{id}/submissions → 200 paginated list")
    void getStudentSubmissions_returns200() throws Exception {
        mockMvc.perform(get("/api/staff/students/" + activeStudent.getId() + "/submissions")
                        .header("Authorization", "Bearer " + staffToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isArray());
    }

    // ── UC-23: POST /api/staff/students/{id}/suspend ──────────────────────────

    @Test
    @DisplayName("POST /suspend with valid reason (10+ chars) → 200, student is suspended")
    void suspendStudent_withValidReason_returns200AndSetsStatus() throws Exception {
        String body = objectMapper.writeValueAsString(
                Map.of("reason", "Sử dụng công cụ hack để tăng điểm số thi thử một cách bất thường."));

        mockMvc.perform(post("/api/staff/students/" + activeStudent.getId() + "/suspend")
                        .header("Authorization", "Bearer " + staffToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("suspended"))
                .andExpect(jsonPath("$.data.passwordHash").doesNotExist());

        // Verify in DB
        StudentUser updated = studentUserRepository.findById(activeStudent.getId()).orElseThrow();
        assertEquals(StudentUser.StudentStatus.SUSPENDED, updated.getStatus());
    }

    @Test
    @DisplayName("POST /suspend with reason < 10 chars → 400 VALIDATION_FAILED")
    void suspendStudent_withShortReason_returns400() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of("reason", "Short"));

        mockMvc.perform(post("/api/staff/students/" + activeStudent.getId() + "/suspend")
                        .header("Authorization", "Bearer " + staffToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /suspend on already-suspended account → 409 ALREADY_IN_STATE")
    void suspendStudent_alreadySuspended_returns409() throws Exception {
        String body = objectMapper.writeValueAsString(
                Map.of("reason", "Trying to suspend an already suspended account - violation."));

        mockMvc.perform(post("/api/staff/students/" + suspendedStudent.getId() + "/suspend")
                        .header("Authorization", "Bearer " + staffToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isConflict());
    }

    // ── UC-23: POST /api/staff/students/{id}/activate ─────────────────────────

    @Test
    @DisplayName("POST /activate on suspended account → 200, student is active")
    void activateStudent_fromSuspended_returns200AndSetsActive() throws Exception {
        mockMvc.perform(post("/api/staff/students/" + suspendedStudent.getId() + "/activate")
                        .header("Authorization", "Bearer " + staffToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("active"))
                .andExpect(jsonPath("$.data.suspendReason").doesNotExist());

        // Verify in DB
        StudentUser updated = studentUserRepository.findById(suspendedStudent.getId()).orElseThrow();
        assertEquals(StudentUser.StudentStatus.ACTIVE, updated.getStatus());
    }

    @Test
    @DisplayName("POST /activate on already-active account → 409 ALREADY_IN_STATE")
    void activateStudent_alreadyActive_returns409() throws Exception {
        mockMvc.perform(post("/api/staff/students/" + activeStudent.getId() + "/activate")
                        .header("Authorization", "Bearer " + staffToken))
                .andExpect(status().isConflict());
    }

    // ── Security Tests ────────────────────────────────────────────────────────

    @Test
    @DisplayName("Student JWT calling /staff/students → 403 FORBIDDEN")
    void studentJwt_callingStaffStudents_returns403() throws Exception {
        mockMvc.perform(get("/api/staff/students")
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Unauthenticated request calling /staff/students → 403 (no JWT)")
    void unauthenticated_callingStaffStudents_returns403() throws Exception {
        // Spring Security returns 403 for unauthenticated when JWT filter doesn't set auth
        mockMvc.perform(get("/api/staff/students"))
                .andExpect(status().isForbidden());
    }

    // Static import helpers
    private static void assertEquals(StudentUser.StudentStatus expected, StudentUser.StudentStatus actual) {
        if (expected != actual) {
            throw new AssertionError("Expected " + expected + " but got " + actual);
        }
    }
}
