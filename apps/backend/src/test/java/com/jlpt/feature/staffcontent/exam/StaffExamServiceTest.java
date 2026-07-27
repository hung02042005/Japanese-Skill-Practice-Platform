/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.staffcontent.exam;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.jlpt.feature.staff.StaffUser;
import com.jlpt.feature.staff.StaffUserRepository;
import com.jlpt.feature.staffcontent.exam.dto.*;
import com.jlpt.feature.staffcontent.exam.entity.ExamAssessmentEntity;
import com.jlpt.feature.staffcontent.exam.entity.ExamQuestionRefEntity;
import com.jlpt.feature.staffcontent.exam.exception.ExamBusinessException;
import com.jlpt.feature.staffcontent.exam.repository.ExamAssessmentRepository;
import com.jlpt.feature.staffcontent.exam.repository.ExamAssignmentRepository;
import com.jlpt.feature.staffcontent.exam.repository.ExamQuestionRefRepository;
import com.jlpt.feature.staffcontent.exam.service.StaffExamService;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class StaffExamServiceTest {

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

    private StaffUser staff;

    @BeforeEach
    void setUp() {
        staff = StaffUser.builder()
                .id(100L)
                .email("staff@example.com")
                .staffRole(StaffUser.StaffRole.STAFF)
                .build();
    }

    @Test
    void createExam_success() {
        when(staffUserRepository.findByEmail("staff@example.com")).thenReturn(Optional.of(staff));
        when(assessmentRepository.save(any())).thenAnswer(i -> {
            ExamAssessmentEntity entity = i.getArgument(0);
            entity.setId(1L);
            return entity;
        });

        CreateExamRequest req = new CreateExamRequest();
        req.setTitle("JLPT N5 Mock Exam");
        req.setJlptLevel("N5");
        req.setDurationMin(60);
        req.setPassScore(60);
        req.setTotalScore(100);
        req.setStatus("draft");

        ExamDetailResponse res = service.createExam(req, "staff@example.com");
        assertEquals(1L, res.getAssessmentId());
        assertEquals("JLPT N5 Mock Exam", res.getTitle());
    }

    @Test
    void createExam_publishNotAllowed() {
        when(staffUserRepository.findByEmail("staff@example.com")).thenReturn(Optional.of(staff));

        CreateExamRequest req = new CreateExamRequest();
        req.setStatus("published");

        assertThrows(ExamBusinessException.class, () -> service.createExam(req, "staff@example.com"));
    }

    @Test
    void listExams_success() {
        when(staffUserRepository.findByEmail("staff@example.com")).thenReturn(Optional.of(staff));
        ExamAssessmentEntity entity = ExamAssessmentEntity.builder()
                .id(1L)
                .title("Exam")
                .createdBy(100L)
                .build();
        Page<ExamAssessmentEntity> page = new PageImpl<>(List.of(entity));
        when(assessmentRepository.findExamsWithFilters(any(), any(), any(Pageable.class)))
                .thenReturn(page);

        ExamListResponse res = service.listExams("N5", "draft", 0, 10, "staff@example.com");
        assertEquals(1, res.getTotalElements());
    }

    @Test
    void getExam_success() {
        when(staffUserRepository.findByEmail("staff@example.com")).thenReturn(Optional.of(staff));
        ExamAssessmentEntity entity = ExamAssessmentEntity.builder()
                .id(1L)
                .title("Exam")
                .totalScore(100)
                .createdBy(100L)
                .build();
        when(assessmentRepository.findByIdAndAssessmentTypeAndStatusNot(1L, "exam", "deleted"))
                .thenReturn(Optional.of(entity));

        ExamDetailResponse res = service.getExam(1L, "staff@example.com");
        assertEquals(1L, res.getAssessmentId());
    }

    @Test
    void assignQuestions_success() {
        when(staffUserRepository.findByEmail("staff@example.com")).thenReturn(Optional.of(staff));
        ExamAssessmentEntity entity = ExamAssessmentEntity.builder()
                .id(1L)
                .jlptLevel("N5")
                .status("draft")
                .totalScore(10)
                .createdBy(100L)
                .build();
        when(assessmentRepository.findByIdAndAssessmentTypeAndStatusNot(1L, "exam", "deleted"))
                .thenReturn(Optional.of(entity));

        ExamQuestionRefEntity q1 = mock(ExamQuestionRefEntity.class);
        when(q1.getStatus()).thenReturn("published");
        when(q1.getJlptLevel()).thenReturn("N5");
        when(questionRefRepository.findById(10L)).thenReturn(Optional.of(q1));

        ExamAssignQuestionsRequest.ExamAssignmentItem item = new ExamAssignQuestionsRequest.ExamAssignmentItem();
        item.setQuestionId(10L);
        item.setSectionName("vocabulary");
        item.setDisplayOrder(1);
        item.setScore(BigDecimal.valueOf(10));

        ExamAssignQuestionsRequest req = new ExamAssignQuestionsRequest();
        req.setAssignments(List.of(item));

        ExamAssignResultResponse res = service.assignQuestions(1L, req, "staff@example.com");
        assertEquals(1, res.getAssignedCount());
        assertTrue(res.isScoreMatched());
    }

    @Test
    void submitForReview_emptyExamThrows() {
        when(staffUserRepository.findByEmail("staff@example.com")).thenReturn(Optional.of(staff));
        ExamAssessmentEntity entity = ExamAssessmentEntity.builder()
                .id(1L)
                .status("draft")
                .createdBy(100L)
                .build();
        when(assessmentRepository.findByIdAndAssessmentTypeAndStatusNot(1L, "exam", "deleted"))
                .thenReturn(Optional.of(entity));
        when(assignmentRepository.countByParentTypeAndParentId("assessment", 1L))
                .thenReturn(0L);

        assertThrows(ExamBusinessException.class, () -> service.submitForReview(1L, "staff@example.com"));
    }

    @Test
    void submitForReview_success() {
        when(staffUserRepository.findByEmail("staff@example.com")).thenReturn(Optional.of(staff));
        ExamAssessmentEntity entity = ExamAssessmentEntity.builder()
                .id(1L)
                .status("draft")
                .createdBy(100L)
                .build();
        when(assessmentRepository.findByIdAndAssessmentTypeAndStatusNot(1L, "exam", "deleted"))
                .thenReturn(Optional.of(entity));
        when(assignmentRepository.countByParentTypeAndParentId("assessment", 1L))
                .thenReturn(5L);

        ExamSubmitReviewResponse res = service.submitForReview(1L, "staff@example.com");
        assertEquals("pending_review", res.getStatus());
    }
}
