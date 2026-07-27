/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.staffcontent.dashboard.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.jlpt.feature.assessment.Assessment;
import com.jlpt.feature.assessment.AssessmentRepository;
import com.jlpt.feature.learning.Kanji.ContentStatus;
import com.jlpt.feature.staff.StaffUser;
import com.jlpt.feature.staff.StaffUserRepository;
import com.jlpt.feature.staffcontent.dashboard.dto.StaffDashboardResponse;
import com.jlpt.shared.exception.ForbiddenException;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit tests cho StaffDashboardService.
 */
@ExtendWith(MockitoExtension.class)
class StaffDashboardServiceTest {

    @Mock
    private StaffUserRepository staffUserRepository;

    @Mock
    private AssessmentRepository assessmentRepository;

    @InjectMocks
    private StaffDashboardService staffDashboardService;

    private StaffUser staff;

    @BeforeEach
    void setUp() {
        staff = StaffUser.builder().id(1L).email("staff@test.com").build();
    }

    @Test
    void getDashboard_staffNotFound_throwsForbiddenException() {
        when(staffUserRepository.findByEmail("unknown@test.com")).thenReturn(Optional.empty());

        assertThrows(ForbiddenException.class, () -> staffDashboardService.getDashboard("unknown@test.com"));
    }

    @Test
    void getDashboard_success_returnsDashboardData() {
        when(staffUserRepository.findByEmail("staff@test.com")).thenReturn(Optional.of(staff));
        when(assessmentRepository.countByCreatedBy_IdAndStatus(1L, ContentStatus.DRAFT))
                .thenReturn(3L);
        when(assessmentRepository.countByCreatedBy_IdAndStatus(1L, ContentStatus.PENDING_REVIEW))
                .thenReturn(2L);

        Assessment a1 = Assessment.builder()
                .id(10L)
                .title("Quiz 1")
                .assessmentType(Assessment.AssessmentType.QUIZ)
                .status(ContentStatus.DRAFT)
                .build();
        when(assessmentRepository.findTop8ByCreatedBy_IdAndIsDeletedFalseOrderByUpdatedAtDesc(1L))
                .thenReturn(List.of(a1));

        StaffDashboardResponse response = staffDashboardService.getDashboard("staff@test.com");

        assertNotNull(response);
        assertEquals(3L, response.getDraftCount());
        assertEquals(2L, response.getPendingReviewCount());
        assertEquals(1, response.getRecentActivity().size());
        assertEquals("Quiz 1", response.getRecentActivity().get(0).getTitle());
    }
}
