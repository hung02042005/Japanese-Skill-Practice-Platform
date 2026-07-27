/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.staffcontent.student.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.jlpt.feature.assessment.AssessmentRepository;
import com.jlpt.feature.assessment.TestAttemptRepository;
import com.jlpt.feature.auth.AuthTokenRepository;
import com.jlpt.feature.staff.StaffManagerGuard;
import com.jlpt.feature.staffcontent.student.dto.StaffStudentListResponse;
import com.jlpt.feature.staffcontent.student.dto.StaffStudentProgressResponse;
import com.jlpt.feature.staffcontent.student.dto.StaffStudentSummaryResponse;
import com.jlpt.feature.student.StudentContentProgressRepository;
import com.jlpt.feature.student.StudentUser;
import com.jlpt.feature.student.StudentUserRepository;
import com.jlpt.shared.exception.DuplicateResourceException;
import com.jlpt.shared.exception.ForbiddenException;
import com.jlpt.shared.exception.ResourceNotFoundException;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

/**
 * Unit tests cho StaffStudentService.
 */
@ExtendWith(MockitoExtension.class)
class StaffStudentServiceTest {

    @Mock
    private StudentUserRepository studentUserRepository;

    @Mock
    private StudentContentProgressRepository progressRepository;

    @Mock
    private TestAttemptRepository testAttemptRepository;

    @Mock
    private AssessmentRepository assessmentRepository;

    @Mock
    private StaffManagerGuard staffManagerGuard;

    @Mock
    private AuthTokenRepository authTokenRepository;

    @InjectMocks
    private StaffStudentService staffStudentService;

    private StudentUser student;

    @BeforeEach
    void setUp() {
        student = StudentUser.builder()
                .id(1L)
                .email("student@test.com")
                .fullName("Student User")
                .status(StudentUser.StudentStatus.ACTIVE)
                .currentJlptLevel(StudentUser.JlptLevel.N4)
                .build();
    }

    // ── listStudents ──────────────────────────────────────────────────────────

    @Test
    void listStudents_returnsFilteredPage() {
        when(studentUserRepository.findAllAdminFiltered(any(), any(), any(), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(student)));

        StaffStudentListResponse res = staffStudentService.listStudents("Student", "N4", "active", 0, 10);

        assertNotNull(res);
        assertEquals(1, res.getContent().size());
        assertEquals("Student User", res.getContent().get(0).getFullName());
    }

    // ── getProgress ───────────────────────────────────────────────────────────

    @Test
    void getProgress_studentNotFound_throwsResourceNotFoundException() {
        when(studentUserRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> staffStudentService.getProgress(99L));
    }

    @Test
    void getProgress_success_returnsProgressSummary() {
        when(studentUserRepository.findById(1L)).thenReturn(Optional.of(student));
        when(progressRepository.countCompleted(eq(1L), any(), any())).thenReturn(12L);
        when(testAttemptRepository.findByStudent_IdAndStatusIn(eq(1L), any())).thenReturn(List.of());

        StaffStudentProgressResponse res = staffStudentService.getProgress(1L);

        assertNotNull(res);
        assertEquals(1L, res.getStudentId());
        assertEquals(12L, res.getLessonsCompleted());
    }

    // ── suspend ───────────────────────────────────────────────────────────────

    @Test
    void suspend_notManager_throwsForbiddenException() {
        doThrow(new ForbiddenException("Forbidden"))
                .when(staffManagerGuard)
                .requireManager(eq("staff@test.com"), anyString());

        assertThrows(ForbiddenException.class, () -> staffStudentService.suspend("staff@test.com", 1L, "Reason"));
    }

    @Test
    void suspend_alreadySuspended_throwsDuplicateResourceException() {
        student.setStatus(StudentUser.StudentStatus.SUSPENDED);
        when(studentUserRepository.findById(1L)).thenReturn(Optional.of(student));

        assertThrows(
                DuplicateResourceException.class, () -> staffStudentService.suspend("manager@test.com", 1L, "Reason"));
    }

    @Test
    void suspend_success_updatesStatusAndRevokesTokens() {
        when(studentUserRepository.findById(1L)).thenReturn(Optional.of(student));

        StaffStudentSummaryResponse res = staffStudentService.suspend("manager@test.com", 1L, "Cheating");

        assertEquals("suspended", res.getStatus());
        assertEquals(StudentUser.StudentStatus.SUSPENDED, student.getStatus());
        assertEquals("Cheating", student.getSuspendReason());
        verify(studentUserRepository).save(student);
        verify(authTokenRepository).revokeAllActiveByStudentId(eq(1L), any());
    }

    // ── activate ──────────────────────────────────────────────────────────────

    @Test
    void activate_success_updatesStatusToActive() {
        student.setStatus(StudentUser.StudentStatus.SUSPENDED);
        student.setSuspendReason("Old reason");
        when(studentUserRepository.findById(1L)).thenReturn(Optional.of(student));

        StaffStudentSummaryResponse res = staffStudentService.activate("manager@test.com", 1L);

        assertEquals("active", res.getStatus());
        assertEquals(StudentUser.StudentStatus.ACTIVE, student.getStatus());
        assertNull(student.getSuspendReason());
        verify(studentUserRepository).save(student);
    }
}
