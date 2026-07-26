/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.assessment;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.jlpt.feature.admin.AdminAuditLog;
import com.jlpt.feature.admin.AdminAuditLogRepository;
import com.jlpt.feature.assessment.dto.request.AnswerRequest;
import com.jlpt.feature.assessment.dto.request.QuestionRequest;
import com.jlpt.feature.assessment.dto.request.QuizRequest;
import com.jlpt.feature.assessment.dto.response.ExamStartResponse;
import com.jlpt.feature.assessment.dto.response.QuizResponse;
import com.jlpt.feature.assessment.dto.response.ScoreResponse;
import com.jlpt.feature.learning.Kanji;
import com.jlpt.feature.staff.StaffUser;
import com.jlpt.feature.student.StudentUser;
import com.jlpt.shared.exception.AttemptAlreadySubmittedException;
import com.jlpt.shared.exception.BusinessRuleException;
import com.jlpt.shared.exception.ForbiddenException;
import com.jlpt.shared.exception.ResourceNotFoundException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit tests cho QuizService.
 */
@ExtendWith(MockitoExtension.class)
class QuizServiceTest {

    @Mock
    private AssessmentRepository assessmentRepository;

    @Mock
    private QuestionRepository questionRepository;

    @Mock
    private QuestionAssignmentRepository questionAssignmentRepository;

    @Mock
    private TestAttemptRepository testAttemptRepository;

    @Mock
    private AttemptAnswerRepository attemptAnswerRepository;

    @Mock
    private AdminAuditLogRepository adminAuditLogRepository;

    @InjectMocks
    private QuizService quizService;

    private StaffUser staffUser;
    private StudentUser studentUser;
    private Assessment quizAssessment;

    @BeforeEach
    void setUp() {
        staffUser = StaffUser.builder().id(1L).email("staff@test.com").build();
        studentUser = StudentUser.builder().id(2L).email("student@test.com").build();

        quizAssessment = Assessment.builder()
                .id(10L)
                .assessmentType(Assessment.AssessmentType.QUIZ)
                .title("N5 Grammar Quiz")
                .topic("Grammar")
                .jlptLevel(StudentUser.JlptLevel.N5)
                .durationMin(15)
                .passScore(5)
                .totalScore(10)
                .status(Kanji.ContentStatus.PUBLISHED)
                .createdBy(staffUser)
                .build();
    }

    // ── createQuiz ────────────────────────────────────────────────────────────

    @Test
    void createQuiz_success_savesAssessmentAndReturnsResponse() {
        QuizRequest req = new QuizRequest();
        req.setTitle("N5 Vocabulary Quiz");
        req.setTopic("Vocab");
        req.setJlptLevel("N5");
        req.setDurationMin(10);
        req.setPassScore(6);

        when(assessmentRepository.save(any(Assessment.class))).thenAnswer(i -> {
            Assessment a = i.getArgument(0);
            a.setId(100L);
            return a;
        });

        QuizResponse res = quizService.createQuiz(req, staffUser);

        assertNotNull(res);
        assertEquals(100L, res.getId());
        assertEquals("N5 Vocabulary Quiz", res.getTitle());
        verify(assessmentRepository).save(any(Assessment.class));
    }

    // ── updateAssessment ──────────────────────────────────────────────────────

    @Test
    void updateAssessment_notFound_throwsResourceNotFoundException() {
        when(assessmentRepository.findByIdAndIsDeletedFalse(99L)).thenReturn(Optional.empty());
        QuizRequest req = new QuizRequest();

        assertThrows(ResourceNotFoundException.class, () -> quizService.updateAssessment(99L, req));
    }

    @Test
    void updateAssessment_success_updatesFields() {
        when(assessmentRepository.findByIdAndIsDeletedFalse(10L)).thenReturn(Optional.of(quizAssessment));
        when(assessmentRepository.save(any())).thenReturn(quizAssessment);

        QuizRequest req = new QuizRequest();
        req.setTitle("Updated Title");
        req.setTopic("Updated Topic");
        req.setJlptLevel("N4");
        req.setDurationMin(20);
        req.setPassScore(8);

        QuizResponse res = quizService.updateAssessment(10L, req);

        assertEquals("Updated Title", quizAssessment.getTitle());
        assertEquals(StudentUser.JlptLevel.N4, quizAssessment.getJlptLevel());
        verify(assessmentRepository).save(quizAssessment);
    }

    // ── softDeleteAssessment ──────────────────────────────────────────────────

    @Test
    void softDeleteAssessment_notFound_throwsResourceNotFoundException() {
        when(assessmentRepository.findByIdAndIsDeletedFalse(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> quizService.softDeleteAssessment(99L));
    }

    @Test
    void softDeleteAssessment_success_setsDeletedTrueAndLogs() {
        when(assessmentRepository.findByIdAndIsDeletedFalse(10L)).thenReturn(Optional.of(quizAssessment));

        quizService.softDeleteAssessment(10L);

        assertTrue(quizAssessment.getIsDeleted());
        verify(assessmentRepository).save(quizAssessment);
        verify(adminAuditLogRepository).save(any(AdminAuditLog.class));
    }

    // ── addQuestions ──────────────────────────────────────────────────────────

    @Test
    void addQuestions_invalidQuestionType_throwsBusinessRuleException() {
        when(assessmentRepository.findByIdAndIsDeletedFalse(10L)).thenReturn(Optional.of(quizAssessment));

        QuestionRequest qReq = new QuestionRequest();
        qReq.setQuestionType("INVALID_TYPE");
        qReq.setSkill("GRAMMAR");
        qReq.setJlptLevel("N5");

        BusinessRuleException ex = assertThrows(
                BusinessRuleException.class, () -> quizService.addQuestions(10L, List.of(qReq), staffUser));
        assertTrue(ex.getMessage().contains("questionType không hợp lệ"));
    }

    @Test
    void addQuestions_success_savesQuestionsAndAssignments() {
        when(assessmentRepository.findByIdAndIsDeletedFalse(10L)).thenReturn(Optional.of(quizAssessment));
        when(questionRepository.save(any())).thenAnswer(i -> {
            Question q = i.getArgument(0);
            q.setId(50L);
            return q;
        });

        QuestionRequest qReq = new QuestionRequest();
        qReq.setQuestionText("What is あ?");
        qReq.setQuestionType("MULTIPLE_CHOICE");
        qReq.setSkill("VOCABULARY");
        qReq.setJlptLevel("N5");
        qReq.setOptionA("a");
        qReq.setOptionB("i");
        qReq.setCorrectOption("A");
        qReq.setScore(BigDecimal.valueOf(2.0));

        quizService.addQuestions(10L, List.of(qReq), staffUser);

        verify(questionRepository).save(any(Question.class));
        verify(questionAssignmentRepository).save(any(QuestionAssignment.class));
    }

    // ── startQuiz ─────────────────────────────────────────────────────────────

    @Test
    void startQuiz_quizNotFound_throwsResourceNotFoundException() {
        when(assessmentRepository.findByIdAndAssessmentTypeAndStatus(
                        10L, Assessment.AssessmentType.QUIZ, Kanji.ContentStatus.PUBLISHED))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> quizService.startQuiz(10L, studentUser));
    }

    @Test
    void startQuiz_noQuestions_throwsResourceNotFoundException() {
        when(assessmentRepository.findByIdAndAssessmentTypeAndStatus(
                        10L, Assessment.AssessmentType.QUIZ, Kanji.ContentStatus.PUBLISHED))
                .thenReturn(Optional.of(quizAssessment));
        when(questionAssignmentRepository.findByParentTypeAndParentIdOrderByDisplayOrder(
                        QuestionAssignment.ParentType.ASSESSMENT, 10L))
                .thenReturn(List.of());

        assertThrows(ResourceNotFoundException.class, () -> quizService.startQuiz(10L, studentUser));
    }

    @Test
    void startQuiz_success_createsAttempt() {
        when(assessmentRepository.findByIdAndAssessmentTypeAndStatus(
                        10L, Assessment.AssessmentType.QUIZ, Kanji.ContentStatus.PUBLISHED))
                .thenReturn(Optional.of(quizAssessment));

        Question q = Question.builder()
                .id(1L)
                .questionType(Question.QuestionType.MULTIPLE_CHOICE)
                .build();
        QuestionAssignment qa = QuestionAssignment.builder()
                .question(q)
                .sectionName("Section 1")
                .build();
        when(questionAssignmentRepository.findByParentTypeAndParentIdOrderByDisplayOrder(
                        QuestionAssignment.ParentType.ASSESSMENT, 10L))
                .thenReturn(List.of(qa));

        when(testAttemptRepository.save(any())).thenAnswer(i -> {
            TestAttempt ta = i.getArgument(0);
            ta.setId(1000L);
            return ta;
        });

        ExamStartResponse res = quizService.startQuiz(10L, studentUser);

        assertNotNull(res);
        assertEquals(1000L, res.getAttemptId());
        verify(testAttemptRepository).save(any(TestAttempt.class));
    }

    // ── submitQuiz ────────────────────────────────────────────────────────────

    @Test
    void submitQuiz_notYourAttempt_throwsForbiddenException() {
        StudentUser otherStudent = StudentUser.builder().id(99L).build();
        TestAttempt attempt =
                TestAttempt.builder().id(100L).student(otherStudent).build();

        when(assessmentRepository.findById(10L)).thenReturn(Optional.of(quizAssessment));
        when(testAttemptRepository.findByIdForUpdate(100L)).thenReturn(Optional.of(attempt));

        assertThrows(ForbiddenException.class, () -> quizService.submitQuiz(10L, 2L, 100L, List.of()));
    }

    @Test
    void submitQuiz_alreadySubmitted_throwsAttemptAlreadySubmittedException() {
        TestAttempt attempt = TestAttempt.builder()
                .id(100L)
                .student(studentUser)
                .status(TestAttempt.AttemptStatus.SUBMITTED)
                .build();

        when(assessmentRepository.findById(10L)).thenReturn(Optional.of(quizAssessment));
        when(testAttemptRepository.findByIdForUpdate(100L)).thenReturn(Optional.of(attempt));

        assertThrows(AttemptAlreadySubmittedException.class, () -> quizService.submitQuiz(10L, 2L, 100L, List.of()));
    }

    @Test
    void submitQuiz_success_calculatesScoreAndSavesAttempt() {
        TestAttempt attempt = TestAttempt.builder()
                .id(100L)
                .student(studentUser)
                .status(TestAttempt.AttemptStatus.IN_PROGRESS)
                .startedAt(LocalDateTime.now().minusMinutes(5))
                .build();

        Question q = Question.builder()
                .id(1L)
                .questionType(Question.QuestionType.MULTIPLE_CHOICE)
                .correctOption("A")
                .explanation("Ex")
                .build();
        QuestionAssignment qa = QuestionAssignment.builder()
                .question(q)
                .score(BigDecimal.valueOf(10.0))
                .build();

        when(assessmentRepository.findById(10L)).thenReturn(Optional.of(quizAssessment));
        when(testAttemptRepository.findByIdForUpdate(100L)).thenReturn(Optional.of(attempt));
        when(questionAssignmentRepository.findByParentTypeAndParentIdOrderByDisplayOrder(
                        QuestionAssignment.ParentType.ASSESSMENT, 10L))
                .thenReturn(List.of(qa));

        AnswerRequest ans = new AnswerRequest();
        ans.setQuestionId(1L);
        ans.setSelectedOption("A");

        ScoreResponse res = quizService.submitQuiz(10L, 2L, 100L, List.of(ans));

        assertNotNull(res);
        assertEquals(BigDecimal.valueOf(10.0), res.getScore());
        assertEquals(TestAttempt.AttemptStatus.SUBMITTED, attempt.getStatus());
        verify(attemptAnswerRepository).saveAll(any());
        verify(testAttemptRepository).save(attempt);
        verify(adminAuditLogRepository).save(any(AdminAuditLog.class));
    }
}
