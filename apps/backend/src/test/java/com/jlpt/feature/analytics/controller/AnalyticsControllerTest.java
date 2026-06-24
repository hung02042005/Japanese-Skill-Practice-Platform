/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.analytics.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.jlpt.feature.admin.dto.DashboardResponse;
import com.jlpt.feature.analytics.controller.AnalyticsController;
import com.jlpt.feature.analytics.dto.AdminReportResponse;
import com.jlpt.feature.analytics.dto.QuizStatsResponse;
import com.jlpt.feature.analytics.service.AnalyticsService;
import com.jlpt.shared.security.JwtProvider;
import com.jlpt.shared.security.UserDetailsServiceImpl;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Controller tests for AnalyticsController using MockMvc + @WebMvcTest.
 * Tests: unauthenticated access (401) and response structure for authorized access.
 * Note: Method-level @PreAuthorize security is tested via integration tests.
 * Individual endpoint routing and response format verified here with @WithMockUser.
 */
@WebMvcTest(AnalyticsController.class)
class AnalyticsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AnalyticsService analyticsService;

    @MockBean
    private JwtProvider jwtProvider;

    @MockBean
    private UserDetailsServiceImpl userDetailsService;

    @MockBean
    private com.jlpt.feature.admin.service.SystemSettingService systemSettingService;

    // ─────────────────────────────────────────────────────────────────────────
    // 401 tests — unauthenticated access must always return 401
    // ─────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("Unauthenticated access — all endpoints return 401")
    class UnauthenticatedAccess {

        @Test
        @DisplayName("GET /api/analytics/my-progress — 401")
        void myProgress_NoAuth_Returns401() throws Exception {
            mockMvc.perform(get("/api/analytics/my-progress"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("GET /api/analytics/streak — 401")
        void streak_NoAuth_Returns401() throws Exception {
            mockMvc.perform(get("/api/analytics/streak"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("GET /api/analytics/completion — 401")
        void completion_NoAuth_Returns401() throws Exception {
            mockMvc.perform(get("/api/analytics/completion"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("GET /api/analytics/quizzes/{id}/stats — 401")
        void quizStats_NoAuth_Returns401() throws Exception {
            mockMvc.perform(get("/api/analytics/quizzes/1/stats"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("GET /api/analytics/students/{id}/exam-history — 401")
        void examHistory_NoAuth_Returns401() throws Exception {
            mockMvc.perform(get("/api/analytics/students/1/exam-history"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("GET /api/analytics/dashboard — 401")
        void dashboard_NoAuth_Returns401() throws Exception {
            mockMvc.perform(get("/api/analytics/dashboard"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("GET /api/analytics/admin/reports — 401")
        void adminReports_NoAuth_Returns401() throws Exception {
            mockMvc.perform(get("/api/analytics/admin/reports")
                            .param("startDate", "2026-01-01")
                            .param("endDate", "2026-01-31"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("GET /api/analytics/admin/reports/export — 401")
        void adminExport_NoAuth_Returns401() throws Exception {
            mockMvc.perform(get("/api/analytics/admin/reports/export")
                            .param("startDate", "2026-01-01")
                            .param("endDate", "2026-01-31"))
                    .andExpect(status().isUnauthorized());
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 200 tests — authorized access, verify response shape
    // ─────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("Authorized access — verify response shape")
    class AuthorizedAccess {

        @Test
        @DisplayName("GET /api/analytics/quizzes/{id}/stats — STAFF: 200 with correct structure")
        @WithMockUser(roles = "STAFF")
        void quizStats_AsStaff_Returns200WithStructure() throws Exception {
            QuizStatsResponse mockStats = QuizStatsResponse.builder()
                    .assessmentId(10L)
                    .title("N5 Grammar Quiz")
                    .totalAttempts(20)
                    .averageScore(75.5)
                    .passRate(80.0)
                    .questionAccuracy(Collections.emptyList())
                    .build();
            when(analyticsService.getQuizStats(10L)).thenReturn(mockStats);

            mockMvc.perform(get("/api/analytics/quizzes/10/stats"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(200))
                    .andExpect(jsonPath("$.message").value("Operation successful"))
                    .andExpect(jsonPath("$.data.assessmentId").value(10))
                    .andExpect(jsonPath("$.data.title").value("N5 Grammar Quiz"))
                    .andExpect(jsonPath("$.data.totalAttempts").value(20))
                    .andExpect(jsonPath("$.data.passRate").value(80.0));
        }

        @Test
        @DisplayName("GET /api/analytics/students/{id}/exam-history — STAFF: 200 with pagination")
        @WithMockUser(roles = "STAFF")
        void examHistory_AsStaff_Returns200WithPagination() throws Exception {
            when(analyticsService.getStudentExamHistory(1L, 0, 20))
                    .thenReturn(new PageImpl<>(List.of()));

            mockMvc.perform(get("/api/analytics/students/1/exam-history")
                            .param("page", "0")
                            .param("size", "20"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(200))
                    .andExpect(jsonPath("$.data.totalElements").value(0))
                    .andExpect(jsonPath("$.data.totalPages").value(1));
        }

        @Test
        @DisplayName("GET /api/analytics/dashboard — ADMIN: 200 with dashboard data")
        @WithMockUser(roles = "ADMIN")
        void dashboard_AsAdmin_Returns200() throws Exception {
            when(analyticsService.getAdminDashboard()).thenReturn(DashboardResponse.builder()
                    .generatedAt(LocalDateTime.now())
                    .totalStudents(100L)
                    .activeStudents(90L)
                    .build());

            mockMvc.perform(get("/api/analytics/dashboard"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(200))
                    .andExpect(jsonPath("$.data.totalStudents").value(100));
        }

        @Test
        @DisplayName("GET /api/analytics/admin/reports — ADMIN: 200 with report structure")
        @WithMockUser(roles = "ADMIN")
        void adminReports_AsAdmin_Returns200() throws Exception {
            AdminReportResponse mockReport = AdminReportResponse.builder()
                    .period(AdminReportResponse.ReportPeriod.builder()
                            .startDate(LocalDate.of(2026, 1, 1))
                            .endDate(LocalDate.of(2026, 1, 31))
                            .build())
                    .newRegistrations(25L)
                    .totalExamAttempts(150L)
                    .avgExamScore(72.5)
                    .courseCompletionRates(Collections.emptyList())
                    .build();
            when(analyticsService.getAdminReport(any(LocalDate.class), any(LocalDate.class)))
                    .thenReturn(mockReport);

            mockMvc.perform(get("/api/analytics/admin/reports")
                            .param("startDate", "2026-01-01")
                            .param("endDate", "2026-01-31"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(200))
                    .andExpect(jsonPath("$.data.newRegistrations").value(25))
                    .andExpect(jsonPath("$.data.totalExamAttempts").value(150))
                    .andExpect(jsonPath("$.data.avgExamScore").value(72.5));
        }

        @Test
        @DisplayName("GET /api/analytics/admin/reports/export — ADMIN CSV: 200 with downloadUrl")
        @WithMockUser(roles = "ADMIN")
        void adminExport_AsAdmin_Csv_Returns200() throws Exception {
            Map<String, Object> mockExport = Map.of(
                    "downloadUrl", "/exports/report-20260616.csv",
                    "expiresAt", LocalDateTime.now().plusHours(24).toString());
            when(analyticsService.exportReport(any(LocalDate.class), any(LocalDate.class), eq("csv")))
                    .thenReturn(mockExport);

            mockMvc.perform(get("/api/analytics/admin/reports/export")
                            .param("startDate", "2026-01-01")
                            .param("endDate", "2026-01-31")
                            .param("format", "csv"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(200))
                    .andExpect(jsonPath("$.data.downloadUrl").value("/exports/report-20260616.csv"));
        }
    }
}
