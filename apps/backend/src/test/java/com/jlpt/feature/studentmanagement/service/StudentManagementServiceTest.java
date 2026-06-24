/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.studentmanagement.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.jlpt.feature.studentmanagement.dto.StudentProgressResponse;
import com.jlpt.feature.studentmanagement.dto.SubmissionSummaryResponse;
import com.jlpt.shared.audit.AdminAuditLog;
import com.jlpt.feature.staff.entity.StaffUser;
import com.jlpt.feature.corelearning.entity.StudentContentProgress;
import com.jlpt.feature.corelearning.entity.StudentSubmission;
import com.jlpt.feature.student.entity.StudentUser;
import com.jlpt.shared.exception.BusinessException;
import com.jlpt.shared.exception.ResourceNotFoundException;
import com.jlpt.feature.studentmanagement.dto.StudentDetailResponse;
import com.jlpt.shared.audit.AdminAuditLogRepository;
import com.jlpt.feature.auth.repository.AuthTokenRepository;
import com.jlpt.feature.staff.repository.StaffUserRepository;
import com.jlpt.feature.assessment.repository.TestAttemptRepository;
import com.jlpt.feature.corelearning.repository.StudentContentProgressRepository;
import com.jlpt.feature.corelearning.repository.StudentSubmissionRepository;
import com.jlpt.feature.student.repository.StudentUserRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * Unit tests for StudentManagementService.
 * Covers UC-21, UC-22, UC-23 — suspend/activate, progress, and detail endpoints.
 */
@ExtendWith(MockitoExtension.class)
class StudentManagementServiceTest {

    @Mock private StudentUserRepository studentUserRepository;
    @Mock private AuthTokenRepository authTokenRepository;
    @Mock private AdminAuditLogRepository adminAuditLogRepository;
    @Mock private StaffUserRepository staffUserRepository;
    @Mock private StudentContentProgressRepository progressRepository;
    @Mock private StudentSubmissionRepository submissionRepository;
    @Mock private TestAttemptRepository testAttemptRepository;

    @InjectMocks
    private StudentManagementService service;

    private StudentUser activeStudent;
    private StudentUser suspendedStudent;
    private StaffUser staffUser;

    @BeforeEach
    void setUp() {
        activeStudent = StudentUser.builder()
                .id(1L)
                .email("student@test.com")
                .fullName("Nguyen Van A")
                .status(StudentUser.StudentStatus.ACTIVE)
                .phone("0901234567")
                .avatarUrl("https://cdn.example.com/1.png")
                .currentJlptLevel(StudentUser.JlptLevel.N3)
                .targetJlptLevel(StudentUser.JlptLevel.N2)
                .currentStreak(5)
                .longestStreak(14)
                .lastActivityDate(LocalDate.now())
                .lastLoginAt(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .build();

        suspendedStudent = StudentUser.builder()
                .id(2L)
                .email("suspended@test.com")
                .fullName("Tran Thi B")
                .status(StudentUser.StudentStatus.SUSPENDED)
                .suspendReason("Violation of TOS")
                .currentStreak(0)
                .longestStreak(10)
                .createdAt(LocalDateTime.now())
                .build();

        staffUser = StaffUser.builder()
                .id(10L)
                .email("staff@test.com")
                .fullName("Staff Member")
                .staffRole(StaffUser.StaffRole.STAFF)
                .status(StaffUser.StaffStatus.ACTIVE)
                .build();

        // Wire optional repositories via reflection (since @Autowired(required = false))
        ReflectionTestUtils.setField(service, "progressRepository", progressRepository);
        ReflectionTestUtils.setField(service, "submissionRepository", submissionRepository);
        ReflectionTestUtils.setField(service, "testAttemptRepository", testAttemptRepository);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // UC-22: getStudentDetail
    // ─────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("UC-22: getStudentDetail")
    class GetStudentDetail {

        @Test
        @DisplayName("Happy path: returns full profile for valid student")
        void getStudentDetail_withValidId_returnsFullProfile() {
            when(studentUserRepository.findById(1L)).thenReturn(Optional.of(activeStudent));

            StudentDetailResponse response = service.getStudentDetail(1L);

            assertNotNull(response);
            assertEquals(1L, response.getStudentId());
            assertEquals("Nguyen Van A", response.getFullName());
            assertEquals("student@test.com", response.getEmail());
            assertEquals("active", response.getStatus());
            assertEquals("N3", response.getCurrentJlptLevel());
            assertEquals("N2", response.getTargetJlptLevel());
            assertEquals(5, response.getCurrentStreak());
            assertEquals(14, response.getLongestStreak());
        }

        @Test
        @DisplayName("Security: passwordHash is NOT exposed in response")
        void getStudentDetail_doesNotExposePasswordHash() {
            activeStudent.setPasswordHash("$2a$12$hashedpassword");
            when(studentUserRepository.findById(1L)).thenReturn(Optional.of(activeStudent));

            StudentDetailResponse response = service.getStudentDetail(1L);

            // The DTO class StudentDetailResponse has no passwordHash field — verify via reflection
            assertFalse(
                    java.util.Arrays.stream(response.getClass().getDeclaredFields())
                            .anyMatch(f -> f.getName().toLowerCase().contains("password")),
                    "Response DTO must NOT contain any password-related fields"
            );
        }

        @Test
        @DisplayName("Security: oauthProviderId is NOT exposed in response")
        void getStudentDetail_doesNotExposeOauthProviderId() {
            activeStudent.setOauthProviderId("google-oauth-id-12345");
            when(studentUserRepository.findById(1L)).thenReturn(Optional.of(activeStudent));

            StudentDetailResponse response = service.getStudentDetail(1L);

            assertFalse(
                    java.util.Arrays.stream(response.getClass().getDeclaredFields())
                            .anyMatch(f -> f.getName().toLowerCase().contains("oauth")),
                    "Response DTO must NOT contain any oauth-related fields"
            );
        }

        @Test
        @DisplayName("Not found: throws ResourceNotFoundException with 404")
        void getStudentDetail_withInvalidId_throwsResourceNotFoundException() {
            when(studentUserRepository.findById(9999L)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class,
                    () -> service.getStudentDetail(9999L));
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // UC-21: getStudentProgressSummary
    // ─────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("UC-21: getStudentProgressSummary")
    class GetStudentProgressSummary {

        @Test
        @DisplayName("Happy path: returns correct completion counts per content type")
        void getStudentProgressSummary_withData_returnsCorrectCounts() {
            when(studentUserRepository.findById(1L)).thenReturn(Optional.of(activeStudent));
            when(progressRepository.countCompletedByStudentIdAndContentType(1L, StudentContentProgress.ContentType.LESSON))
                    .thenReturn(10L);
            when(progressRepository.countCompletedByStudentIdAndContentType(1L, StudentContentProgress.ContentType.KANJI))
                    .thenReturn(40L);
            when(progressRepository.countCompletedByStudentIdAndContentType(1L, StudentContentProgress.ContentType.VOCABULARY))
                    .thenReturn(95L);
            when(progressRepository.countCompletedByStudentIdAndContentType(1L, StudentContentProgress.ContentType.GRAMMAR))
                    .thenReturn(12L);
            when(progressRepository.countCompletedByStudentIdAndContentType(1L, StudentContentProgress.ContentType.KANA))
                    .thenReturn(46L);

            StudentProgressResponse response = service.getStudentProgressSummary(1L);

            assertNotNull(response);
            assertEquals(1L, response.getStudentId());
            assertEquals(10, response.getLessonsCompleted());
            assertEquals(40, response.getKanjiCompleted());
            assertEquals(95, response.getVocabularyCompleted());
            assertEquals(12, response.getGrammarCompleted());
            assertEquals(46, response.getKanaCompleted());
        }

        @Test
        @DisplayName("Null progressRepository (optional dependency): returns empty counts instead of crash")
        void getStudentProgressSummary_withNullProgressRepo_returnsEmptyList() {
            // Simulate the case where Người 3's repository hasn't merged yet
            ReflectionTestUtils.setField(service, "progressRepository", null);
            when(studentUserRepository.findById(1L)).thenReturn(Optional.of(activeStudent));

            StudentProgressResponse response = service.getStudentProgressSummary(1L);

            assertNotNull(response);
            assertEquals(0, response.getLessonsCompleted(), "Should return 0 when repo is null");
            assertEquals(0, response.getKanjiCompleted(), "Should return 0 when repo is null");
            assertEquals(0, response.getVocabularyCompleted(), "Should return 0 when repo is null");
        }

        @Test
        @DisplayName("FR-STUDENT-03: returns exam stats from TestAttemptRepository")
        void getStudentProgressSummary_withExamData_returnsExamStats() {
            when(studentUserRepository.findById(1L)).thenReturn(Optional.of(activeStudent));
            when(testAttemptRepository.countExamsByStudentId(1L)).thenReturn(3L);
            when(testAttemptRepository.avgExamScoreByStudentId(1L)).thenReturn(new java.math.BigDecimal("135.5"));
            when(testAttemptRepository.maxExamScoreByStudentId(1L)).thenReturn(new java.math.BigDecimal("150.0"));

            StudentProgressResponse response = service.getStudentProgressSummary(1L);

            assertEquals(3L, response.getTotalExamsTaken());
            assertEquals(new java.math.BigDecimal("135.5"), response.getAverageExamScore());
            assertEquals(new java.math.BigDecimal("150.0"), response.getHighestExamScore());
        }

        @Test
        @DisplayName("Null testAttemptRepository (optional dependency): returns zero/null exam stats instead of crash")
        void getStudentProgressSummary_withNullTestAttemptRepo_returnsZeroExamStats() {
            ReflectionTestUtils.setField(service, "testAttemptRepository", null);
            when(studentUserRepository.findById(1L)).thenReturn(Optional.of(activeStudent));

            StudentProgressResponse response = service.getStudentProgressSummary(1L);

            assertEquals(0L, response.getTotalExamsTaken());
            assertNull(response.getAverageExamScore());
            assertNull(response.getHighestExamScore());
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // UC-23: suspendStudent
    // ─────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("UC-23: suspendStudent")
    class SuspendStudent {

        @Test
        @DisplayName("Happy path: sets status SUSPENDED, revokes tokens, writes audit log")
        void suspendStudent_withValidReason_setsStatusAndRevokesTokens() {
            String validReason = "Sử dụng công cụ hack để tăng điểm số một cách bất thường.";
            when(studentUserRepository.findById(1L)).thenReturn(Optional.of(activeStudent));
            when(staffUserRepository.findByEmail("staff@test.com")).thenReturn(Optional.of(staffUser));
            when(adminAuditLogRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
            when(studentUserRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            StudentDetailResponse result = service.suspendStudent("staff@test.com", 1L, validReason);

            assertNotNull(result);
            assertEquals("suspended", result.getStatus());

            // Verify token revocation was called synchronously
            verify(authTokenRepository).revokeAllActiveByStudentId(eq(1L), any(LocalDateTime.class));
            verify(studentUserRepository).save(activeStudent);
        }

        @Test
        @DisplayName("Audit log: writes STUDENT_SUSPENDED with correct fields")
        void suspendStudent_writesAuditLog() {
            String validReason = "Violation of community guidelines - multiple offenses.";
            when(studentUserRepository.findById(1L)).thenReturn(Optional.of(activeStudent));
            when(staffUserRepository.findByEmail("staff@test.com")).thenReturn(Optional.of(staffUser));
            when(studentUserRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            ArgumentCaptor<AdminAuditLog> captor = ArgumentCaptor.forClass(AdminAuditLog.class);

            service.suspendStudent("staff@test.com", 1L, validReason);

            verify(adminAuditLogRepository).save(captor.capture());
            AdminAuditLog log = captor.getValue();
            assertEquals("STUDENT_SUSPENDED", log.getAction());
            assertEquals("student_users", log.getTargetTable());
            assertEquals(1L, log.getTargetId());
            assertNotNull(log.getDescription());
            assertTrue(log.getDescription().contains("student@test.com"),
                    "Audit log description must identify the target student");
        }

        @Test
        @DisplayName("Already suspended: throws BusinessException with HTTP 409")
        void suspendStudent_withAlreadySuspended_throws409Conflict() {
            String validReason = "Trying to suspend an already suspended account.";
            when(studentUserRepository.findById(2L)).thenReturn(Optional.of(suspendedStudent));

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> service.suspendStudent("staff@test.com", 2L, validReason));

            assertEquals(409, ex.getStatus());
            assertEquals("ALREADY_IN_STATE", ex.getErrorCode());
        }

        @Test
        @DisplayName("Short reason (< 10 chars): throws BusinessException with HTTP 400")
        void suspendStudent_withShortReason_throwsValidationException() {
            // Reason has only 5 chars
            BusinessException ex = assertThrows(BusinessException.class,
                    () -> service.suspendStudent("staff@test.com", 1L, "Short"));

            assertEquals(400, ex.getStatus());
            assertEquals("VALIDATION_FAILED", ex.getErrorCode());
        }

        @Test
        @DisplayName("Null reason: throws BusinessException with HTTP 400")
        void suspendStudent_withNullReason_throwsValidationException() {
            BusinessException ex = assertThrows(BusinessException.class,
                    () -> service.suspendStudent("staff@test.com", 1L, null));

            assertEquals(400, ex.getStatus());
        }

        @Test
        @DisplayName("Student not found: throws ResourceNotFoundException")
        void suspendStudent_withInvalidStudentId_throwsResourceNotFoundException() {
            String validReason = "Valid reason with enough characters.";
            when(studentUserRepository.findById(9999L)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class,
                    () -> service.suspendStudent("staff@test.com", 9999L, validReason));
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // UC-23: activateStudent
    // ─────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("UC-23: activateStudent")
    class ActivateStudent {

        @Test
        @DisplayName("Happy path: sets status ACTIVE, clears suspend reason, writes audit log")
        void activateStudent_withSuspendedAccount_setsActiveStatus() {
            when(studentUserRepository.findById(2L)).thenReturn(Optional.of(suspendedStudent));
            when(staffUserRepository.findByEmail("staff@test.com")).thenReturn(Optional.of(staffUser));
            when(studentUserRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            StudentDetailResponse result = service.activateStudent("staff@test.com", 2L);

            assertNotNull(result);
            assertEquals("active", result.getStatus());
            assertNull(result.getSuspendReason(), "Suspend reason must be cleared on activate");

            // Verify audit log was written
            verify(adminAuditLogRepository).save(argThat(log ->
                    "STUDENT_ACTIVATED".equals(log.getAction()) &&
                    "student_users".equals(log.getTargetTable()) &&
                    Long.valueOf(2L).equals(log.getTargetId())
            ));
        }

        @Test
        @DisplayName("Already active: throws BusinessException with HTTP 409")
        void activateStudent_withAlreadyActiveAccount_throws409Conflict() {
            when(studentUserRepository.findById(1L)).thenReturn(Optional.of(activeStudent));

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> service.activateStudent("staff@test.com", 1L));

            assertEquals(409, ex.getStatus());
            assertEquals("ALREADY_IN_STATE", ex.getErrorCode());
        }

        @Test
        @DisplayName("Student not found: throws ResourceNotFoundException")
        void activateStudent_withInvalidStudentId_throwsResourceNotFoundException() {
            when(studentUserRepository.findById(9999L)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class,
                    () -> service.activateStudent("staff@test.com", 9999L));
        }
    }
}
