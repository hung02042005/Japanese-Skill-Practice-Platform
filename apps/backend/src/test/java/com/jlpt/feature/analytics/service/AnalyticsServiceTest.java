/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.analytics.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.jlpt.shared.audit.AdminAuditLog;
import com.jlpt.feature.assessment.entity.Assessment;
import com.jlpt.feature.corelearning.entity.StudentContentProgress;
import com.jlpt.feature.student.entity.StudentUser;
import com.jlpt.feature.assessment.entity.TestAttempt;
import com.jlpt.shared.exception.BadRequestException;
import com.jlpt.shared.exception.ResourceNotFoundException;
import com.jlpt.feature.admin.dto.DashboardResponse;
import com.jlpt.feature.analytics.dto.AdminReportResponse;
import com.jlpt.feature.analytics.dto.AnalyticsResponse;
import com.jlpt.feature.analytics.dto.CompletionRateResponse;
import com.jlpt.feature.analytics.dto.ExamAttemptResponse;
import com.jlpt.feature.analytics.dto.QuizStatsResponse;
import com.jlpt.feature.analytics.dto.StreakDetailResponse;
import com.jlpt.feature.analytics.service.AnalyticsService;
import com.jlpt.feature.support.repository.TicketRepository;
import com.jlpt.shared.audit.AdminAuditLogRepository;
import com.jlpt.feature.assessment.repository.AssessmentRepository;
import com.jlpt.feature.assessment.repository.AttemptAnswerRepository;
import com.jlpt.feature.staff.repository.StaffUserRepository;
import com.jlpt.feature.corelearning.repository.StudentContentProgressRepository;
import com.jlpt.feature.corelearning.repository.StudentSubmissionRepository;
import com.jlpt.feature.student.repository.StudentUserRepository;
import com.jlpt.feature.assessment.repository.TestAttemptRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

/**
 * Unit tests for AnalyticsService.
 * Covers UC-19, UC-32, UC-38 — all score/rate computations are server-side.
 */
@ExtendWith(MockitoExtension.class)
class AnalyticsServiceTest {

    @Mock
    private StudentUserRepository studentUserRepository;

    @Mock
    private AdminAuditLogRepository adminAuditLogRepository;

    @Mock
    private StaffUserRepository staffUserRepository;

    @Mock
    private AssessmentRepository assessmentRepository;

    @Mock
    private StudentContentProgressRepository progressRepository;

    @Mock
    private StudentSubmissionRepository submissionRepository;

    @Mock
    private TestAttemptRepository testAttemptRepository;

    @Mock
    private AttemptAnswerRepository attemptAnswerRepository;

    @Mock
    private TicketRepository ticketRepository;

    @InjectMocks
    private AnalyticsService analyticsService;

    private StudentUser mockStudent;

    @BeforeEach
    void setUp() {
        mockStudent = StudentUser.builder()
                .id(1L)
                .email("student@test.com")
                .fullName("Test Student")
                .status(StudentUser.StudentStatus.ACTIVE)
                .currentStreak(5)
                .longestStreak(10)
                .lastActivityDate(LocalDate.now())
                .build();

        // Wire optional repositories via reflection (since @Autowired(required = false))
        org.springframework.test.util.ReflectionTestUtils.setField(
                analyticsService, "progressRepository", progressRepository);
        org.springframework.test.util.ReflectionTestUtils.setField(
                analyticsService, "submissionRepository", submissionRepository);
        org.springframework.test.util.ReflectionTestUtils.setField(
                analyticsService, "testAttemptRepository", testAttemptRepository);
        org.springframework.test.util.ReflectionTestUtils.setField(
                analyticsService, "attemptAnswerRepository", attemptAnswerRepository);
        org.springframework.test.util.ReflectionTestUtils.setField(
                analyticsService, "ticketRepository", ticketRepository);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // UC-19: getStudentProgressAnalytics
    // ─────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("UC-19: getStudentProgressAnalytics")
    class GetStudentProgressAnalytics {

        @Test
        @DisplayName("Happy path: returns full analytics for valid student")
        void testGetAnalytics_HappyPath() {
            when(studentUserRepository.findById(1L)).thenReturn(Optional.of(mockStudent));
            for (StudentContentProgress.ContentType type : StudentContentProgress.ContentType.values()) {
                when(progressRepository.countCompletedByStudentIdAndContentType(1L, type)).thenReturn(2L);
                when(progressRepository.countByStudentIdAndContentType(1L, type)).thenReturn(5L);
            }
            when(submissionRepository.avgSpeakingScoreByStudentId(1L)).thenReturn(80.0);
            when(testAttemptRepository.findByStudentIdAndAttemptTypeIn(eq(1L), any(), any(Pageable.class)))
                    .thenReturn(Page.empty());

            AnalyticsResponse result = analyticsService.getStudentProgressAnalytics(1L);

            assertNotNull(result);
            assertEquals(1L, result.getStudentId());
            assertEquals(5, result.getCurrentStreak());
            assertEquals(10, result.getLongestStreak());
            assertEquals("ACTIVE", result.getStreakStatus());
            assertNotNull(result.getCompletions());
            assertNotNull(result.getCompletionRates());
            assertNotNull(result.getSkillsRadar());
            assertEquals(80.0, result.getSkillsRadar().get("speaking"));
        }

        @Test
        @DisplayName("Student not found: throws ResourceNotFoundException")
        void testGetAnalytics_StudentNotFound() {
            when(studentUserRepository.findById(99L)).thenReturn(Optional.empty());
            assertThrows(ResourceNotFoundException.class,
                    () -> analyticsService.getStudentProgressAnalytics(99L));
        }

        @Test
        @DisplayName("Division by zero in completionRate: returns 0.0, not exception")
        void testGetAnalytics_DivisionByZero_ReturnsZero() {
            when(studentUserRepository.findById(1L)).thenReturn(Optional.of(mockStudent));
            for (StudentContentProgress.ContentType type : StudentContentProgress.ContentType.values()) {
                when(progressRepository.countCompletedByStudentIdAndContentType(1L, type)).thenReturn(0L);
                when(progressRepository.countByStudentIdAndContentType(1L, type)).thenReturn(0L); // total = 0 → rate = 0
            }
            when(submissionRepository.avgSpeakingScoreByStudentId(1L)).thenReturn(null);
            when(testAttemptRepository.findByStudentIdAndAttemptTypeIn(eq(1L), any(), any(Pageable.class)))
                    .thenReturn(Page.empty());

            AnalyticsResponse result = analyticsService.getStudentProgressAnalytics(1L);

            result.getCompletionRates().values().forEach(rate ->
                    assertEquals(0.0, rate, "Expected 0.0 for zero-total division"));
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // UC-19: getStudyStreakDetail
    // ─────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("UC-19: getStudyStreakDetail")
    class GetStudyStreakDetail {

        @Test
        @DisplayName("Last activity today: streakStatus = ACTIVE")
        void testStreak_Active() {
            mockStudent.setLastActivityDate(LocalDate.now());
            when(studentUserRepository.findById(1L)).thenReturn(Optional.of(mockStudent));

            StreakDetailResponse result = analyticsService.getStudyStreakDetail(1L);

            assertEquals("ACTIVE", result.getStreakStatus());
            assertTrue(result.getStreakStatusMessage().contains("hôm nay"));
        }

        @Test
        @DisplayName("Last activity yesterday: streakStatus = AT_RISK")
        void testStreak_AtRisk() {
            mockStudent.setLastActivityDate(LocalDate.now().minusDays(1));
            when(studentUserRepository.findById(1L)).thenReturn(Optional.of(mockStudent));

            StreakDetailResponse result = analyticsService.getStudyStreakDetail(1L);

            assertEquals("AT_RISK", result.getStreakStatus());
        }

        @Test
        @DisplayName("Last activity 3 days ago: streakStatus = BROKEN")
        void testStreak_Broken() {
            mockStudent.setLastActivityDate(LocalDate.now().minusDays(3));
            when(studentUserRepository.findById(1L)).thenReturn(Optional.of(mockStudent));

            StreakDetailResponse result = analyticsService.getStudyStreakDetail(1L);

            assertEquals("BROKEN", result.getStreakStatus());
        }

        @Test
        @DisplayName("Null lastActivityDate: streakStatus = BROKEN")
        void testStreak_NullActivity_Broken() {
            mockStudent.setLastActivityDate(null);
            when(studentUserRepository.findById(1L)).thenReturn(Optional.of(mockStudent));

            StreakDetailResponse result = analyticsService.getStudyStreakDetail(1L);

            assertEquals("BROKEN", result.getStreakStatus());
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // UC-19: getCompletionRate
    // ─────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("UC-19: getCompletionRate")
    class GetCompletionRate {

        @Test
        @DisplayName("Single contentType: returns correct rate")
        void testCompletionRate_SingleType() {
            when(studentUserRepository.findById(1L)).thenReturn(Optional.of(mockStudent));
            when(progressRepository.countByStudentIdAndContentType(1L, StudentContentProgress.ContentType.LESSON))
                    .thenReturn(10L);
            when(progressRepository.countCompletedByStudentIdAndContentType(1L, StudentContentProgress.ContentType.LESSON))
                    .thenReturn(7L);

            CompletionRateResponse result = analyticsService.getCompletionRate(1L, "lesson");

            assertEquals("lesson", result.getContentType());
            assertEquals(10L, result.getTotalCount());
            assertEquals(7L, result.getCompletedCount());
            assertEquals(70.0, result.getCompletionRate());
        }

        @Test
        @DisplayName("Null contentType: aggregates all types")
        void testCompletionRate_AllTypes() {
            when(studentUserRepository.findById(1L)).thenReturn(Optional.of(mockStudent));
            for (StudentContentProgress.ContentType type : StudentContentProgress.ContentType.values()) {
                when(progressRepository.countByStudentIdAndContentType(1L, type)).thenReturn(4L);
                when(progressRepository.countCompletedByStudentIdAndContentType(1L, type)).thenReturn(2L);
            }

            CompletionRateResponse result = analyticsService.getCompletionRate(1L, null);

            assertNull(result.getContentType());
            // 5 types × 4 total = 20 total; 5 × 2 = 10 completed; rate = 50.0
            assertEquals(50.0, result.getCompletionRate());
        }

        @Test
        @DisplayName("Invalid contentType: throws BadRequestException")
        void testCompletionRate_InvalidType() {
            when(studentUserRepository.findById(1L)).thenReturn(Optional.of(mockStudent));
            assertThrows(BadRequestException.class,
                    () -> analyticsService.getCompletionRate(1L, "INVALID_TYPE"));
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // UC-32: getQuizStats
    // ─────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("UC-32: getQuizStats")
    class GetQuizStats {

        @Test
        @DisplayName("Happy path: returns stats with correct passRate")
        void testQuizStats_HappyPath() {
            Assessment mockAssessment = Assessment.builder()
                    .id(10L)
                    .title("N5 Grammar Quiz")
                    .totalScore(100)
                    .build();
            when(assessmentRepository.findById(10L)).thenReturn(Optional.of(mockAssessment));
            when(testAttemptRepository.countSubmittedByParentId(10L)).thenReturn(20L);
            when(testAttemptRepository.avgScoreByParentId(10L)).thenReturn(new BigDecimal("75.5"));
            when(testAttemptRepository.countPassedByParentId(10L)).thenReturn(15L);
            // findSubmittedByParentId returns empty list → no attemptAnswerRepository calls needed
            when(testAttemptRepository.findSubmittedByParentId(10L)).thenReturn(Collections.emptyList());

            QuizStatsResponse result = analyticsService.getQuizStats(10L);

            assertEquals(10L, result.getAssessmentId());
            assertEquals("N5 Grammar Quiz", result.getTitle());
            assertEquals(20, result.getTotalAttempts());
            assertEquals(75.5, result.getAverageScore());
            // 15/20 = 75.0%
            assertEquals(75.0, result.getPassRate());
        }

        @Test
        @DisplayName("Assessment not found: throws ResourceNotFoundException")
        void testQuizStats_NotFound() {
            when(assessmentRepository.findById(99L)).thenReturn(Optional.empty());
            assertThrows(ResourceNotFoundException.class,
                    () -> analyticsService.getQuizStats(99L));
        }

        @Test
        @DisplayName("Zero attempts: passRate = 0.0 (no division by zero)")
        void testQuizStats_ZeroAttempts() {
            Assessment mockAssessment = Assessment.builder().id(10L).title("Test").totalScore(50).build();
            when(assessmentRepository.findById(10L)).thenReturn(Optional.of(mockAssessment));
            when(testAttemptRepository.countSubmittedByParentId(10L)).thenReturn(0L);
            when(testAttemptRepository.avgScoreByParentId(10L)).thenReturn(null);
            when(testAttemptRepository.countPassedByParentId(10L)).thenReturn(0L);
            // empty attempts list → attemptAnswerRepository.findByAttemptIdIn called with empty list → service returns early
            when(testAttemptRepository.findSubmittedByParentId(10L)).thenReturn(Collections.emptyList());

            QuizStatsResponse result = analyticsService.getQuizStats(10L);

            assertEquals(0, result.getTotalAttempts());
            assertEquals(0.0, result.getPassRate(), "Pass rate must be 0 when no attempts");
            assertEquals(0.0, result.getAverageScore(), "Avg score must be 0 when no attempts");
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // UC-32: getStudentExamHistory
    // ─────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("UC-32: getStudentExamHistory")
    class GetStudentExamHistory {

        @Test
        @DisplayName("Happy path: returns paginated exam history")
        void testExamHistory_HappyPath() {
            when(studentUserRepository.findById(1L)).thenReturn(Optional.of(mockStudent));
            TestAttempt attempt = TestAttempt.builder()
                    .id(100L)
                    .student(mockStudent)
                    .attemptType(TestAttempt.AttemptType.EXAM)
                    .parentType(TestAttempt.ParentType.ASSESSMENT)
                    .parentId(5L)
                    .totalScore(new BigDecimal("85.0"))
                    .maxScore(new BigDecimal("100.0"))
                    .isPassed(true)
                    .startedAt(LocalDateTime.now().minusHours(2))
                    .submittedAt(LocalDateTime.now().minusHours(1))
                    .status(TestAttempt.AttemptStatus.SUBMITTED)
                    .build();

            when(testAttemptRepository.findByStudentIdAndAttemptTypeIn(eq(1L), any(), any(Pageable.class)))
                    .thenReturn(new PageImpl<>(List.of(attempt)));
            when(assessmentRepository.findById(5L))
                    .thenReturn(Optional.of(Assessment.builder().id(5L).title("N5 Exam").build()));

            var page = analyticsService.getStudentExamHistory(1L, 0, 20);

            assertEquals(1, page.getTotalElements());
            ExamAttemptResponse resp = page.getContent().get(0);
            assertEquals(100L, resp.getAttemptId());
            assertEquals("exam", resp.getAttemptType());
            assertEquals("N5 Exam", resp.getAssessmentTitle());
            assertTrue(resp.getIsPassed());
        }

        @Test
        @DisplayName("No history: returns empty page")
        void testExamHistory_Empty() {
            when(studentUserRepository.findById(1L)).thenReturn(Optional.of(mockStudent));
            when(testAttemptRepository.findByStudentIdAndAttemptTypeIn(eq(1L), any(), any(Pageable.class)))
                    .thenReturn(Page.empty());

            var page = analyticsService.getStudentExamHistory(1L, 0, 20);

            assertEquals(0, page.getTotalElements());
            assertTrue(page.getContent().isEmpty());
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // UC-38: getAdminDashboard
    // ─────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("UC-38: getAdminDashboard")
    class GetAdminDashboard {

        @Test
        @DisplayName("Happy path: admin dashboard aggregates correctly")
        void testAdminDashboard_HappyPath() {
            when(studentUserRepository.count()).thenReturn(100L);
            when(studentUserRepository.countByStatusValue("active")).thenReturn(90L);
            when(studentUserRepository.countByStatusValue("suspended")).thenReturn(5L);
            when(studentUserRepository.countCreatedThisMonth()).thenReturn(15L);
            when(ticketRepository.countByStatus(any())).thenReturn(10L);
            when(submissionRepository.countPendingManualGrade()).thenReturn(3L);
            when(adminAuditLogRepository.findTop5ByOrderByCreatedAtDesc()).thenReturn(Collections.emptyList());

            DashboardResponse result = analyticsService.getAdminDashboard();

            assertNotNull(result);
            assertEquals(100L, result.getTotalStudents());
            assertEquals(90L, result.getActiveStudents());
            assertEquals(15L, result.getNewStudentsThisMonth());
            assertEquals(3L, result.getPendingSubmissions());
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // UC-38: getAdminReport
    // ─────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("UC-38: getAdminReport")
    class GetAdminReport {

        private final LocalDate start = LocalDate.now().minusDays(30);
        private final LocalDate end = LocalDate.now();

        @Test
        @DisplayName("Happy path: report with valid date range")
        void testAdminReport_HappyPath() {
            when(studentUserRepository.countByCreatedAtBetween(any(), any())).thenReturn(25L);
            when(testAttemptRepository.countExamAttemptsBetween(any(), any())).thenReturn(150L);
            when(testAttemptRepository.avgExamScoreBetween(any(), any())).thenReturn(new BigDecimal("72.35"));
            when(assessmentRepository.countPublishedByJlptLevel(any())).thenReturn(10L);

            AdminReportResponse result = analyticsService.getAdminReport(start, end);

            assertNotNull(result);
            assertEquals(start, result.getPeriod().getStartDate());
            assertEquals(end, result.getPeriod().getEndDate());
            assertEquals(25L, result.getNewRegistrations());
            assertEquals(150L, result.getTotalExamAttempts());
            assertEquals(72.35, result.getAvgExamScore());
        }

        @Test
        @DisplayName("startDate == endDate: throws BadRequestException")
        void testAdminReport_SameDateRange() {
            LocalDate sameDate = LocalDate.now();
            assertThrows(BadRequestException.class,
                    () -> analyticsService.getAdminReport(sameDate, sameDate));
        }

        @Test
        @DisplayName("startDate after endDate: throws BadRequestException")
        void testAdminReport_InvalidDateRange() {
            assertThrows(BadRequestException.class,
                    () -> analyticsService.getAdminReport(end, start));
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // UC-38: exportReport
    // ─────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("UC-38: exportReport")
    class ExportReport {

        private final LocalDate start = LocalDate.now().minusDays(7);
        private final LocalDate end = LocalDate.now();

        @Test
        @DisplayName("Invalid format: throws BadRequestException")
        void testExport_InvalidFormat() {
            assertThrows(BadRequestException.class,
                    () -> analyticsService.exportReport(start, end, "pdf"));
        }

        @Test
        @DisplayName("Invalid date range: throws BadRequestException")
        void testExport_InvalidDateRange() {
            assertThrows(BadRequestException.class,
                    () -> analyticsService.exportReport(end, start, "csv"));
        }
    }
}
