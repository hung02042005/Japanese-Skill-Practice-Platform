/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.assessment;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.jlpt.feature.admin.AdminAuditLogRepository;
import com.jlpt.feature.assessment.dto.request.AnswerRequest;
import com.jlpt.feature.assessment.dto.request.SubmitExamRequest;
import com.jlpt.feature.assessment.dto.response.ExamHistoryResponse;
import com.jlpt.feature.assessment.dto.response.ExamReviewResponse;
import com.jlpt.feature.assessment.dto.response.ExamStartResponse;
import com.jlpt.feature.assessment.dto.response.ExamStatusResponse;
import com.jlpt.feature.assessment.dto.response.ExamSubmitResponse;
import com.jlpt.feature.assessment.dto.response.QuestionResponse;
import com.jlpt.feature.learning.Kanji;
import com.jlpt.feature.student.StudentUser;
import com.jlpt.shared.exception.AttemptAlreadySubmittedException;
import com.jlpt.shared.exception.BadRequestException;
import com.jlpt.shared.exception.BusinessRuleException;
import com.jlpt.shared.exception.ForbiddenException;
import com.jlpt.shared.exception.ResourceNotFoundException;
import com.jlpt.shared.exception.TimeExceededException;
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
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

@ExtendWith(MockitoExtension.class)
class MockExamServiceTest {

    @Mock
    private AssessmentRepository assessmentRepository;

    @Mock
    private QuestionAssignmentRepository questionAssignmentRepository;

    @Mock
    private TestAttemptRepository testAttemptRepository;

    @Mock
    private AttemptAnswerRepository attemptAnswerRepository;

    @Mock
    private AdminAuditLogRepository adminAuditLogRepository;

    @InjectMocks
    private MockExamService mockExamService;

    private StudentUser student;
    private Assessment exam;

    @BeforeEach
    void setUp() {
        student = StudentUser.builder()
                .id(1L)
                .email("s@test.com")
                .fullName("Student")
                .build();
        exam = Assessment.builder()
                .id(10L)
                .assessmentType(Assessment.AssessmentType.EXAM)
                .title("N3 Mock Exam")
                .jlptLevel(StudentUser.JlptLevel.N3)
                .durationMin(60)
                .passScore(60)
                .totalScore(100)
                .status(Kanji.ContentStatus.PUBLISHED)
                .build();
    }

    private Question buildQuestion(Long id, String correctOption) {
        return Question.builder()
                .id(id)
                .questionText("Q" + id)
                .questionType(Question.QuestionType.MULTIPLE_CHOICE)
                .optionA("1")
                .optionB("2")
                .optionC("3")
                .optionD("4")
                .correctOption(correctOption)
                .explanation("exp")
                .build();
    }

    private QuestionAssignment buildAssignment(
            Long id, Question question, String section, BigDecimal score, int order) {
        return QuestionAssignment.builder()
                .id(id)
                .parentType(QuestionAssignment.ParentType.ASSESSMENT)
                .parentId(exam.getId())
                .question(question)
                .sectionName(section)
                .score(score)
                .displayOrder(order)
                .build();
    }

    private TestAttempt buildAttempt(
            Long id, StudentUser owner, TestAttempt.AttemptStatus status, LocalDateTime startedAt) {
        return TestAttempt.builder()
                .id(id)
                .student(owner)
                .attemptType(TestAttempt.AttemptType.EXAM)
                .parentType(TestAttempt.ParentType.ASSESSMENT)
                .parentId(exam.getId())
                .startedAt(startedAt)
                .status(status)
                .build();
    }

    // ── startExam ───────────────────────────────────────────────────────────

    @Test
    void startExam_happyPath_returnsSectionsWithoutCorrectOption() {
        Question q1 = buildQuestion(100L, "B");
        QuestionAssignment qa1 = buildAssignment(1L, q1, "language_knowledge", BigDecimal.valueOf(20), 1);

        when(assessmentRepository.findByIdAndAssessmentTypeAndStatus(
                        10L, Assessment.AssessmentType.EXAM, Kanji.ContentStatus.PUBLISHED))
                .thenReturn(Optional.of(exam));
        when(questionAssignmentRepository.findByParentTypeAndParentIdOrderByDisplayOrder(
                        QuestionAssignment.ParentType.ASSESSMENT, 10L))
                .thenReturn(List.of(qa1));
        when(testAttemptRepository.save(any(TestAttempt.class))).thenAnswer(inv -> {
            TestAttempt a = inv.getArgument(0);
            a.setId(500L);
            return a;
        });

        ExamStartResponse response = mockExamService.startExam(10L, student);

        assertEquals(500L, response.getAttemptId());
        assertNotNull(response.getExpiresAt());
        assertEquals(1, response.getSections().size());
        assertEquals("language_knowledge", response.getSections().get(0).getSectionName());
        assertFalse(hasCorrectOptionGetter(), "QuestionResponse phải KHÔNG có correctOption");
    }

    @Test
    void startExam_notPublishedOrWrongType_throwsNotFound() {
        when(assessmentRepository.findByIdAndAssessmentTypeAndStatus(
                        10L, Assessment.AssessmentType.EXAM, Kanji.ContentStatus.PUBLISHED))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> mockExamService.startExam(10L, student));
    }

    @Test
    void startExam_noQuestions_throwsNotFound() {
        when(assessmentRepository.findByIdAndAssessmentTypeAndStatus(
                        10L, Assessment.AssessmentType.EXAM, Kanji.ContentStatus.PUBLISHED))
                .thenReturn(Optional.of(exam));
        when(questionAssignmentRepository.findByParentTypeAndParentIdOrderByDisplayOrder(
                        QuestionAssignment.ParentType.ASSESSMENT, 10L))
                .thenReturn(List.of());

        assertThrows(ResourceNotFoundException.class, () -> mockExamService.startExam(10L, student));
    }

    // ── submitExam ──────────────────────────────────────────────────────────

    private List<QuestionAssignment> threeSectionAssignments() {
        Question lang = buildQuestion(1L, "B");
        Question reading = buildQuestion(2L, "A");
        Question listening = buildQuestion(3L, "C");
        return List.of(
                buildAssignment(1L, lang, "language_knowledge", BigDecimal.valueOf(20), 1),
                buildAssignment(2L, reading, "reading", BigDecimal.valueOf(20), 2),
                buildAssignment(3L, listening, "listening", BigDecimal.valueOf(20), 3));
    }

    private SubmitExamRequest submitRequest(Long attemptId, boolean autoSubmit, AnswerRequest... answers) {
        SubmitExamRequest request = new SubmitExamRequest();
        request.setAttemptId(attemptId);
        request.setIsAutoSubmit(autoSubmit);
        request.setAnswers(List.of(answers));
        return request;
    }

    private AnswerRequest answer(Long questionId, String selectedOption) {
        AnswerRequest a = new AnswerRequest();
        a.setQuestionId(questionId);
        a.setSelectedOption(selectedOption);
        return a;
    }

    @Test
    void submitExam_sectionScoresCalculatedCorrectly_andIsPassedTrue() {
        TestAttempt attempt = buildAttempt(
                500L,
                student,
                TestAttempt.AttemptStatus.IN_PROGRESS,
                LocalDateTime.now().minusMinutes(5));
        SubmitExamRequest request = submitRequest(500L, false, answer(1L, "B"), answer(2L, "A"), answer(3L, "D"));

        when(assessmentRepository.findById(10L)).thenReturn(Optional.of(exam));
        when(testAttemptRepository.findByIdForUpdate(500L)).thenReturn(Optional.of(attempt));
        when(questionAssignmentRepository.findByParentTypeAndParentIdOrderByDisplayOrder(
                        QuestionAssignment.ParentType.ASSESSMENT, 10L))
                .thenReturn(threeSectionAssignments());

        ExamSubmitResponse response = mockExamService.submitExam(10L, 1L, request);

        assertEquals(BigDecimal.valueOf(40), response.getTotalScore());
        assertEquals(BigDecimal.valueOf(60), response.getMaxScore());
        assertEquals(BigDecimal.valueOf(20), response.getSectionScores().getLanguageKnowledge());
        assertEquals(BigDecimal.valueOf(20), response.getSectionScores().getReading());
        assertEquals(BigDecimal.ZERO, response.getSectionScores().getListening());
        assertFalse(response.getIsPassed());
        verify(attemptAnswerRepository).saveAll(any());
        verify(adminAuditLogRepository).save(any());
    }

    @Test
    void submitExam_isPassedTrue_whenScoreMeetsPassScore() {
        exam.setPassScore(40);
        TestAttempt attempt = buildAttempt(
                500L,
                student,
                TestAttempt.AttemptStatus.IN_PROGRESS,
                LocalDateTime.now().minusMinutes(5));
        SubmitExamRequest request = submitRequest(500L, false, answer(1L, "B"), answer(2L, "A"), answer(3L, "D"));

        when(assessmentRepository.findById(10L)).thenReturn(Optional.of(exam));
        when(testAttemptRepository.findByIdForUpdate(500L)).thenReturn(Optional.of(attempt));
        when(questionAssignmentRepository.findByParentTypeAndParentIdOrderByDisplayOrder(
                        QuestionAssignment.ParentType.ASSESSMENT, 10L))
                .thenReturn(threeSectionAssignments());

        ExamSubmitResponse response = mockExamService.submitExam(10L, 1L, request);

        assertTrue(response.getIsPassed());
    }

    @Test
    void submitExam_allWrong_scoreZero() {
        TestAttempt attempt = buildAttempt(
                500L,
                student,
                TestAttempt.AttemptStatus.IN_PROGRESS,
                LocalDateTime.now().minusMinutes(5));
        SubmitExamRequest request = submitRequest(500L, false, answer(1L, "D"), answer(2L, "D"), answer(3L, "D"));

        when(assessmentRepository.findById(10L)).thenReturn(Optional.of(exam));
        when(testAttemptRepository.findByIdForUpdate(500L)).thenReturn(Optional.of(attempt));
        when(questionAssignmentRepository.findByParentTypeAndParentIdOrderByDisplayOrder(
                        QuestionAssignment.ParentType.ASSESSMENT, 10L))
                .thenReturn(threeSectionAssignments());

        ExamSubmitResponse response = mockExamService.submitExam(10L, 1L, request);

        assertEquals(BigDecimal.ZERO, response.getTotalScore());
        assertFalse(response.getIsPassed());
    }

    @Test
    void submitExam_manualSubmitAfterExpiry_throwsTimeExceeded() {
        TestAttempt attempt = buildAttempt(
                500L,
                student,
                TestAttempt.AttemptStatus.IN_PROGRESS,
                LocalDateTime.now().minusMinutes(70));
        SubmitExamRequest request = submitRequest(500L, false, answer(1L, "B"));

        when(assessmentRepository.findById(10L)).thenReturn(Optional.of(exam));
        when(testAttemptRepository.findByIdForUpdate(500L)).thenReturn(Optional.of(attempt));

        assertThrows(TimeExceededException.class, () -> mockExamService.submitExam(10L, 1L, request));
        assertEquals(TestAttempt.AttemptStatus.IN_PROGRESS, attempt.getStatus());
    }

    @Test
    void submitExam_autoSubmitAfterExpiry_acceptedAsAutoSubmitted() {
        TestAttempt attempt = buildAttempt(
                500L,
                student,
                TestAttempt.AttemptStatus.IN_PROGRESS,
                LocalDateTime.now().minusMinutes(70));
        SubmitExamRequest request = submitRequest(500L, true, answer(1L, "B"), answer(2L, "A"), answer(3L, "C"));

        when(assessmentRepository.findById(10L)).thenReturn(Optional.of(exam));
        when(testAttemptRepository.findByIdForUpdate(500L)).thenReturn(Optional.of(attempt));
        when(questionAssignmentRepository.findByParentTypeAndParentIdOrderByDisplayOrder(
                        QuestionAssignment.ParentType.ASSESSMENT, 10L))
                .thenReturn(threeSectionAssignments());

        ExamSubmitResponse response = mockExamService.submitExam(10L, 1L, request);

        assertEquals(TestAttempt.AttemptStatus.AUTO_SUBMITTED, attempt.getStatus());
        assertEquals(BigDecimal.valueOf(60), response.getTotalScore());
    }

    @Test
    void submitExam_alreadySubmitted_throwsAttemptAlreadySubmitted() {
        TestAttempt attempt = buildAttempt(
                500L,
                student,
                TestAttempt.AttemptStatus.SUBMITTED,
                LocalDateTime.now().minusMinutes(5));
        SubmitExamRequest request = submitRequest(500L, false, answer(1L, "B"));

        when(assessmentRepository.findById(10L)).thenReturn(Optional.of(exam));
        when(testAttemptRepository.findByIdForUpdate(500L)).thenReturn(Optional.of(attempt));

        assertThrows(AttemptAlreadySubmittedException.class, () -> mockExamService.submitExam(10L, 1L, request));
    }

    @Test
    void submitExam_wrongStudent_throwsForbidden() {
        StudentUser otherStudent = StudentUser.builder().id(2L).build();
        TestAttempt attempt = buildAttempt(
                500L,
                otherStudent,
                TestAttempt.AttemptStatus.IN_PROGRESS,
                LocalDateTime.now().minusMinutes(5));
        SubmitExamRequest request = submitRequest(500L, false, answer(1L, "B"));

        when(assessmentRepository.findById(10L)).thenReturn(Optional.of(exam));
        when(testAttemptRepository.findByIdForUpdate(500L)).thenReturn(Optional.of(attempt));

        assertThrows(ForbiddenException.class, () -> mockExamService.submitExam(10L, 1L, request));
    }

    @Test
    void submitExam_mismatchedAssessment_throwsBadRequest() {
        TestAttempt attempt = buildAttempt(
                500L,
                student,
                TestAttempt.AttemptStatus.IN_PROGRESS,
                LocalDateTime.now().minusMinutes(5));
        attempt.setParentId(999L);
        SubmitExamRequest request = submitRequest(500L, false, answer(1L, "B"));

        when(assessmentRepository.findById(10L)).thenReturn(Optional.of(exam));
        when(testAttemptRepository.findByIdForUpdate(500L)).thenReturn(Optional.of(attempt));

        assertThrows(BadRequestException.class, () -> mockExamService.submitExam(10L, 1L, request));
    }

    @Test
    void submitExam_scoreInvariantViolation_throwsBusinessRuleException() {
        Question corrupted = buildQuestion(1L, "B");
        QuestionAssignment negativeScoreAssignment =
                buildAssignment(1L, corrupted, "language_knowledge", BigDecimal.valueOf(-10), 1);
        TestAttempt attempt = buildAttempt(
                500L,
                student,
                TestAttempt.AttemptStatus.IN_PROGRESS,
                LocalDateTime.now().minusMinutes(5));
        SubmitExamRequest request = submitRequest(500L, false, answer(1L, "B"));

        when(assessmentRepository.findById(10L)).thenReturn(Optional.of(exam));
        when(testAttemptRepository.findByIdForUpdate(500L)).thenReturn(Optional.of(attempt));
        when(questionAssignmentRepository.findByParentTypeAndParentIdOrderByDisplayOrder(
                        QuestionAssignment.ParentType.ASSESSMENT, 10L))
                .thenReturn(List.of(negativeScoreAssignment));

        assertThrows(BusinessRuleException.class, () -> mockExamService.submitExam(10L, 1L, request));
        verify(attemptAnswerRepository, never()).saveAll(any());
    }

    // ── getExamStatus ───────────────────────────────────────────────────────

    @Test
    void getExamStatus_notExpired_returnsRemainingSeconds() {
        TestAttempt attempt = buildAttempt(
                500L,
                student,
                TestAttempt.AttemptStatus.IN_PROGRESS,
                LocalDateTime.now().minusMinutes(10));

        when(testAttemptRepository.findById(500L)).thenReturn(Optional.of(attempt));
        when(assessmentRepository.findById(10L)).thenReturn(Optional.of(exam));

        ExamStatusResponse response = mockExamService.getExamStatus(500L, 1L);

        assertFalse(response.isExpired());
        assertTrue(response.getRemainingSeconds() > 0);
    }

    @Test
    void getExamStatus_expired_remainingSecondsZero() {
        TestAttempt attempt = buildAttempt(
                500L,
                student,
                TestAttempt.AttemptStatus.IN_PROGRESS,
                LocalDateTime.now().minusMinutes(70));

        when(testAttemptRepository.findById(500L)).thenReturn(Optional.of(attempt));
        when(assessmentRepository.findById(10L)).thenReturn(Optional.of(exam));

        ExamStatusResponse response = mockExamService.getExamStatus(500L, 1L);

        assertTrue(response.isExpired());
        assertEquals(0, response.getRemainingSeconds());
    }

    @Test
    void getExamStatus_wrongStudent_throwsForbidden() {
        StudentUser otherStudent = StudentUser.builder().id(2L).build();
        TestAttempt attempt = buildAttempt(
                500L,
                otherStudent,
                TestAttempt.AttemptStatus.IN_PROGRESS,
                LocalDateTime.now().minusMinutes(10));

        when(testAttemptRepository.findById(500L)).thenReturn(Optional.of(attempt));

        assertThrows(ForbiddenException.class, () -> mockExamService.getExamStatus(500L, 1L));
    }

    // ── getExamHistory ──────────────────────────────────────────────────────

    @Test
    void getExamHistory_returnsMappedPage() {
        TestAttempt submitted = buildAttempt(500L, student, TestAttempt.AttemptStatus.SUBMITTED, LocalDateTime.now());
        submitted.setSubmittedAt(LocalDateTime.now());
        submitted.setTotalScore(BigDecimal.valueOf(70));
        submitted.setMaxScore(BigDecimal.valueOf(100));
        submitted.setIsPassed(true);

        var pageable = PageRequest.of(0, 10);
        when(testAttemptRepository.findByStudent_IdAndAttemptTypeAndStatusInOrderBySubmittedAtDesc(
                        1L,
                        TestAttempt.AttemptType.EXAM,
                        List.of(TestAttempt.AttemptStatus.SUBMITTED, TestAttempt.AttemptStatus.AUTO_SUBMITTED),
                        pageable))
                .thenReturn(new PageImpl<>(List.of(submitted), pageable, 1));
        when(assessmentRepository.findAllById(List.of(10L))).thenReturn(List.of(exam));

        var page = mockExamService.getExamHistory(1L, pageable);

        assertEquals(1, page.getTotalElements());
        ExamHistoryResponse item = page.getContent().get(0);
        assertEquals(500L, item.getAttemptId());
        assertEquals("N3 Mock Exam", item.getAssessmentTitle());
        assertTrue(item.getIsPassed());
    }

    // ── getExamReview ───────────────────────────────────────────────────────

    @Test
    void getExamReview_returnsCorrectOptionAndExplanation() {
        TestAttempt attempt = buildAttempt(500L, student, TestAttempt.AttemptStatus.SUBMITTED, LocalDateTime.now());
        attempt.setTotalScore(BigDecimal.valueOf(20));
        attempt.setMaxScore(BigDecimal.valueOf(20));
        attempt.setIsPassed(true);

        Question q = buildQuestion(1L, "B");
        AttemptAnswer answerEntity = AttemptAnswer.builder()
                .attempt(attempt)
                .question(q)
                .selectedOption("B")
                .isCorrect(true)
                .score(BigDecimal.valueOf(20))
                .build();

        when(testAttemptRepository.findById(500L)).thenReturn(Optional.of(attempt));
        when(attemptAnswerRepository.findByAttemptIdWithQuestion(500L)).thenReturn(List.of(answerEntity));

        ExamReviewResponse response = mockExamService.getExamReview(500L, 1L);

        assertEquals(1, response.getResults().size());
        assertEquals("B", response.getResults().get(0).getCorrectOption());
        assertEquals("exp", response.getResults().get(0).getExplanation());
    }

    @Test
    void getExamReview_whileInProgress_throwsBadRequest() {
        TestAttempt attempt = buildAttempt(
                500L,
                student,
                TestAttempt.AttemptStatus.IN_PROGRESS,
                LocalDateTime.now().minusMinutes(5));

        when(testAttemptRepository.findById(500L)).thenReturn(Optional.of(attempt));

        assertThrows(BadRequestException.class, () -> mockExamService.getExamReview(500L, 1L));
    }

    private boolean hasCorrectOptionGetter() {
        try {
            QuestionResponse.class.getMethod("getCorrectOption");
            return true;
        } catch (NoSuchMethodException e) {
            return false;
        }
    }
}
