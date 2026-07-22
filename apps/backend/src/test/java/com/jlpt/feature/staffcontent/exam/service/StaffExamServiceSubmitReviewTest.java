/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.staffcontent.exam.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import com.jlpt.feature.staff.StaffUser;
import com.jlpt.feature.staff.StaffUserRepository;
import com.jlpt.feature.staffcontent.exam.entity.ExamAssessmentEntity;
import com.jlpt.feature.staffcontent.exam.entity.ExamAssignmentEntity;
import com.jlpt.feature.staffcontent.exam.exception.ExamBusinessException;
import com.jlpt.feature.staffcontent.exam.repository.ExamAssessmentRepository;
import com.jlpt.feature.staffcontent.exam.repository.ExamAssignmentRepository;
import com.jlpt.feature.staffcontent.exam.repository.ExamQuestionRefRepository;
import com.jlpt.feature.staffcontent.exam.service.StaffExamService;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** L3-R1 (PA-3) — submitForReview chặn cứng đề thi rỗng (FR-28-31). */
@ExtendWith(MockitoExtension.class)
class StaffExamServiceSubmitReviewTest {

    @Mock
    private ExamAssessmentRepository assessmentRepository;

    @Mock
    private ExamAssignmentRepository assignmentRepository;

    @Mock
    private ExamQuestionRefRepository questionRefRepository;

    @Mock
    private StaffUserRepository staffUserRepository;

    @InjectMocks
    private StaffExamService service;

    @Test
    void submitForReview_rejectsEmptyExam() {
        StaffUser staff =
                StaffUser.builder().id(1L).staffRole(StaffUser.StaffRole.STAFF).build();
        when(staffUserRepository.findByEmail("s@x.com")).thenReturn(Optional.of(staff));

        ExamAssessmentEntity exam = ExamAssessmentEntity.builder()
                .id(5L)
                .createdBy(1L)
                .status("draft")
                .totalScore(10)
                .build();
        when(assessmentRepository.findByIdAndAssessmentTypeAndStatusNot(5L, "exam", "deleted"))
                .thenReturn(Optional.of(exam));
        when(assignmentRepository.countByParentTypeAndParentId("assessment", 5L))
                .thenReturn(0L);

        ExamBusinessException ex =
                assertThrows(ExamBusinessException.class, () -> service.submitForReview(5L, "s@x.com"));
        assertEquals("EMPTY_EXAM", ex.getErrorCode());
    }
}
