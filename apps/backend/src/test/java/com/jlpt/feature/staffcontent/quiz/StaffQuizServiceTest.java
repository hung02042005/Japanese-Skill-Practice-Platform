/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.staffcontent.quiz;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.jlpt.feature.learning.LessonRepository;
import com.jlpt.feature.staff.StaffUser;
import com.jlpt.feature.staff.StaffUserRepository;
import com.jlpt.feature.staffcontent.quiz.dto.*;
import com.jlpt.feature.staffcontent.quiz.entity.QuizAssessmentEntity;
import com.jlpt.feature.staffcontent.quiz.entity.QuizQuestionRefEntity;
import com.jlpt.feature.staffcontent.quiz.exception.QuizBusinessException;
import com.jlpt.feature.staffcontent.quiz.repository.QuizAssessmentRepository;
import com.jlpt.feature.staffcontent.quiz.repository.QuizAssignmentRepository;
import com.jlpt.feature.staffcontent.quiz.repository.QuizQuestionRefRepository;
import com.jlpt.feature.staffcontent.quiz.service.StaffQuizService;
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
class StaffQuizServiceTest {

    @Mock
    private QuizAssessmentRepository assessmentRepository;

    @Mock
    private QuizAssignmentRepository assignmentRepository;

    @Mock
    private QuizQuestionRefRepository questionRefRepository;

    @Mock
    private LessonRepository lessonRepository;

    @Mock
    private StaffUserRepository staffUserRepository;

    @InjectMocks
    private StaffQuizService service;

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
    void createQuiz_success() {
        when(staffUserRepository.findByEmail("staff@example.com")).thenReturn(Optional.of(staff));
        when(assessmentRepository.save(any())).thenAnswer(i -> {
            QuizAssessmentEntity entity = i.getArgument(0);
            entity.setId(1L);
            return entity;
        });

        CreateQuizRequest req = new CreateQuizRequest();
        req.setTitle("Quiz N5");
        req.setTopic("Grammar");
        req.setJlptLevel("N5");
        req.setDurationMin(15);
        req.setPassScore(10);
        req.setTotalScore(20);
        req.setStatus("draft");

        QuizDetailResponse res = service.createQuiz(req, "staff@example.com");
        assertEquals(1L, res.getAssessmentId());
        assertEquals("Quiz N5", res.getTitle());
    }

    @Test
    void createQuiz_missingLessonAndTopicThrows() {
        when(staffUserRepository.findByEmail("staff@example.com")).thenReturn(Optional.of(staff));

        CreateQuizRequest req = new CreateQuizRequest();
        req.setTitle("Quiz N5");
        req.setJlptLevel("N5");
        req.setDurationMin(15);
        req.setPassScore(10);
        req.setTotalScore(20);

        assertThrows(QuizBusinessException.class, () -> service.createQuiz(req, "staff@example.com"));
    }

    @Test
    void listQuizzes_success() {
        when(staffUserRepository.findByEmail("staff@example.com")).thenReturn(Optional.of(staff));
        QuizAssessmentEntity entity =
                QuizAssessmentEntity.builder().id(1L).title("Quiz").build();
        Page<QuizAssessmentEntity> page = new PageImpl<>(List.of(entity));
        when(assessmentRepository.findQuizzesWithFilters(any(), any(), any(), any(Pageable.class)))
                .thenReturn(page);

        QuizListResponse res = service.listQuizzes("N5", "draft", null, 0, 10, "staff@example.com");
        assertEquals(1, res.getTotalElements());
    }

    @Test
    void getQuiz_success() {
        when(staffUserRepository.findByEmail("staff@example.com")).thenReturn(Optional.of(staff));
        QuizAssessmentEntity entity = QuizAssessmentEntity.builder()
                .id(1L)
                .title("Quiz")
                .totalScore(20)
                .build();
        when(assessmentRepository.findByIdAndAssessmentTypeAndStatusNot(1L, "quiz", "deleted"))
                .thenReturn(Optional.of(entity));

        QuizDetailResponse res = service.getQuiz(1L, "staff@example.com");
        assertEquals(1L, res.getAssessmentId());
    }

    @Test
    void assignQuestions_success() {
        when(staffUserRepository.findByEmail("staff@example.com")).thenReturn(Optional.of(staff));
        QuizAssessmentEntity entity = QuizAssessmentEntity.builder()
                .id(1L)
                .status("draft")
                .totalScore(10)
                .createdBy(100L)
                .build();
        when(assessmentRepository.findByIdAndAssessmentTypeAndStatusNot(1L, "quiz", "deleted"))
                .thenReturn(Optional.of(entity));

        QuizQuestionRefEntity q1 = mock(QuizQuestionRefEntity.class);
        when(q1.getStatus()).thenReturn("published");
        when(questionRefRepository.findById(10L)).thenReturn(Optional.of(q1));

        AssignQuestionsRequest.AssignmentItem item = new AssignQuestionsRequest.AssignmentItem();
        item.setQuestionId(10L);
        item.setDisplayOrder(1);
        item.setScore(BigDecimal.valueOf(10));

        AssignQuestionsRequest req = new AssignQuestionsRequest();
        req.setAssignments(List.of(item));

        AssignResultResponse res = service.assignQuestions(1L, req, "staff@example.com");
        assertEquals(1, res.getAssignedCount());
        assertTrue(res.isScoreMatched());
    }

    @Test
    void submitForReview_success() {
        when(staffUserRepository.findByEmail("staff@example.com")).thenReturn(Optional.of(staff));
        QuizAssessmentEntity entity = QuizAssessmentEntity.builder()
                .id(1L)
                .status("draft")
                .createdBy(100L)
                .build();
        when(assessmentRepository.findByIdAndAssessmentTypeAndStatusNot(1L, "quiz", "deleted"))
                .thenReturn(Optional.of(entity));
        when(assignmentRepository.countByParentTypeAndParentId("assessment", 1L))
                .thenReturn(3L);

        QuizSubmitReviewResponse res = service.submitForReview(1L, "staff@example.com");
        assertEquals("pending_review", res.getStatus());
    }
}
