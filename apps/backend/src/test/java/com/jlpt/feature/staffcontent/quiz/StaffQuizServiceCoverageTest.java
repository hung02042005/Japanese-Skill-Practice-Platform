/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.staffcontent.quiz;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.*;

import com.jlpt.feature.learning.Lesson;
import com.jlpt.feature.learning.LessonRepository;
import com.jlpt.feature.staff.StaffUser;
import com.jlpt.feature.staff.StaffUserRepository;
import com.jlpt.feature.staffcontent.quiz.dto.AssignQuestionsRequest;
import com.jlpt.feature.staffcontent.quiz.dto.AssignResultResponse;
import com.jlpt.feature.staffcontent.quiz.dto.CreateQuizRequest;
import com.jlpt.feature.staffcontent.quiz.dto.QuizDetailResponse;
import com.jlpt.feature.staffcontent.quiz.dto.QuizListResponse;
import com.jlpt.feature.staffcontent.quiz.dto.UpdateQuizRequest;
import com.jlpt.feature.staffcontent.quiz.entity.QuizAssessmentEntity;
import com.jlpt.feature.staffcontent.quiz.entity.QuizAssignmentEntity;
import com.jlpt.feature.staffcontent.quiz.entity.QuizQuestionRefEntity;
import com.jlpt.feature.staffcontent.quiz.exception.QuizBusinessException;
import com.jlpt.feature.staffcontent.quiz.repository.QuizAssessmentRepository;
import com.jlpt.feature.staffcontent.quiz.repository.QuizAssignmentRepository;
import com.jlpt.feature.staffcontent.quiz.repository.QuizQuestionRefRepository;
import com.jlpt.feature.staffcontent.quiz.service.StaffQuizService;
import com.jlpt.shared.exception.ForbiddenException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * UC-26 quiz — phủ NHÁNH: validate level/điểm/lessonId-hoặc-topic (FR-26-03/04/05), chặn Staff tự
 * publish (FR-26-30), quyền sở hữu (FR-26-31), chỉ sửa khi draft/rejected (FR-26-16/25) và luật gán
 * câu hỏi (trùng, chưa publish, đã xoá — FR-26-21/22/23).
 */
@ExtendWith(MockitoExtension.class)
class StaffQuizServiceCoverageTest {

    private static final String STAFF_EMAIL = "staff@sakuji.com";
    private static final String MANAGER_EMAIL = "manager@sakuji.com";

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
    private StaffUser manager;

    @BeforeEach
    void setUp() {
        staff = StaffUser.builder()
                .id(1L)
                .email(STAFF_EMAIL)
                .staffRole(StaffUser.StaffRole.STAFF)
                .build();
        manager = StaffUser.builder()
                .id(2L)
                .email(MANAGER_EMAIL)
                .staffRole(StaffUser.StaffRole.STAFF_MANAGER)
                .build();
    }

    private void stubStaff() {
        when(staffUserRepository.findByEmail(STAFF_EMAIL)).thenReturn(Optional.of(staff));
    }

    private QuizAssessmentEntity quiz(String status, Long owner) {
        return QuizAssessmentEntity.builder()
                .id(10L)
                .assessmentType("quiz")
                .title("Quiz N5")
                .topic("Gia đình")
                .jlptLevel("N5")
                .durationMin(15)
                .passScore(6)
                .totalScore(10)
                .status(status)
                .createdBy(owner)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    private CreateQuizRequest createRequest() {
        CreateQuizRequest req = new CreateQuizRequest();
        req.setTitle("  Quiz N5  ");
        req.setTopic("  Gia đình  ");
        req.setJlptLevel("  n5  ");
        req.setDurationMin(15);
        req.setPassScore(6);
        req.setTotalScore(10);
        return req;
    }

    private UpdateQuizRequest updateRequest() {
        UpdateQuizRequest req = new UpdateQuizRequest();
        req.setTitle("  Quiz mới  ");
        req.setTopic("Chủ đề mới");
        req.setJlptLevel("n4");
        req.setDurationMin(20);
        req.setPassScore(5);
        req.setTotalScore(10);
        return req;
    }

    private AssignQuestionsRequest.AssignmentItem item(Long questionId, int order, String score, String section) {
        AssignQuestionsRequest.AssignmentItem it = new AssignQuestionsRequest.AssignmentItem();
        it.setQuestionId(questionId);
        it.setDisplayOrder(order);
        it.setScore(new BigDecimal(score));
        it.setSectionName(section);
        return it;
    }

    private AssignQuestionsRequest assignRequest(AssignQuestionsRequest.AssignmentItem... items) {
        AssignQuestionsRequest req = new AssignQuestionsRequest();
        req.setAssignments(List.of(items));
        return req;
    }

    /** QuizQuestionRefEntity chỉ có @Getter — dựng thật bằng reflection (xem F-06 trong TEST_FAILURE_LOG). */
    private QuizQuestionRefEntity questionRef(Long id, String status) {
        QuizQuestionRefEntity ref = new QuizQuestionRefEntity();
        ReflectionTestUtils.setField(ref, "id", id);
        ReflectionTestUtils.setField(ref, "questionText", "Câu hỏi " + id);
        ReflectionTestUtils.setField(ref, "status", status);
        return ref;
    }

    // ── resolveStaff ─────────────────────────────────────────────────────────

    @Test
    void anyOperation_unknownStaff_throwsForbidden() {
        when(staffUserRepository.findByEmail("ghost@sakuji.com")).thenReturn(Optional.empty());
        CreateQuizRequest req = createRequest();

        assertThrows(ForbiddenException.class, () -> service.createQuiz(req, "ghost@sakuji.com"));
    }

    // ── createQuiz: guardNoPublish ───────────────────────────────────────────

    @ParameterizedTest
    @ValueSource(strings = {"published", "PUBLISHED", "  archived  "})
    void createQuiz_requestingPublishOrArchive_isRejected(String status) {
        stubStaff();
        CreateQuizRequest req = createRequest();
        req.setStatus(status);

        assertThrows(QuizBusinessException.class, () -> service.createQuiz(req, STAFF_EMAIL));
        verify(assessmentRepository, never()).save(any());
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"draft", "rejected"})
    void createQuiz_harmlessStatus_isAllowed(String status) {
        stubStaff();
        when(assessmentRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        CreateQuizRequest req = createRequest();
        req.setStatus(status);

        assertNotNull(service.createQuiz(req, STAFF_EMAIL));
    }

    // ── createQuiz: validateLevel ────────────────────────────────────────────

    @ParameterizedTest
    @ValueSource(strings = {"N5", "n4", "  N3  ", "n2", "N1"})
    void createQuiz_everyValidLevel_isAcceptedAndUppercased(String level) {
        stubStaff();
        when(assessmentRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        CreateQuizRequest req = createRequest();
        req.setJlptLevel(level);

        assertEquals(
                level.trim().toUpperCase(), service.createQuiz(req, STAFF_EMAIL).getJlptLevel());
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"N9", "JLPT", ""})
    void createQuiz_invalidLevel_isRejected(String level) {
        stubStaff();
        CreateQuizRequest req = createRequest();
        req.setJlptLevel(level);

        assertThrows(QuizBusinessException.class, () -> service.createQuiz(req, STAFF_EMAIL));
    }

    // ── createQuiz: validateLessonOrTopic (FR-26-03) ─────────────────────────

    @Test
    void createQuiz_neitherLessonNorTopic_isRejected() {
        stubStaff();
        CreateQuizRequest req = createRequest();
        req.setTopic("   ");
        req.setLessonId(null);

        assertThrows(QuizBusinessException.class, () -> service.createQuiz(req, STAFF_EMAIL));
    }

    @Test
    void createQuiz_lessonOnlyWithoutTopic_isAccepted() {
        stubStaff();
        when(lessonRepository.findByIdAndStatusNot(5L, Lesson.LessonStatus.DELETED))
                .thenReturn(Optional.of(Lesson.builder().id(5L).build()));
        when(assessmentRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        CreateQuizRequest req = createRequest();
        req.setTopic(null);
        req.setLessonId(5L);

        QuizDetailResponse response = service.createQuiz(req, STAFF_EMAIL);

        assertEquals(5L, response.getLessonId());
        assertNull(response.getTopic());
    }

    @Test
    void createQuiz_topicOnlyWithoutLesson_skipsLessonLookup() {
        stubStaff();
        when(assessmentRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        QuizDetailResponse response = service.createQuiz(createRequest(), STAFF_EMAIL);

        assertEquals("Gia đình", response.getTopic());
        verifyNoInteractions(lessonRepository);
    }

    @Test
    void createQuiz_unknownLesson_isRejected() {
        stubStaff();
        when(lessonRepository.findByIdAndStatusNot(404L, Lesson.LessonStatus.DELETED))
                .thenReturn(Optional.empty());
        CreateQuizRequest req = createRequest();
        req.setLessonId(404L);

        assertThrows(QuizBusinessException.class, () -> service.createQuiz(req, STAFF_EMAIL));
    }

    // ── createQuiz: validateScoreRange ───────────────────────────────────────

    @ParameterizedTest
    @CsvSource(
            value = {"null,6,10", "0,6,10", "-1,6,10", "15,6,null", "15,6,0", "15,null,10", "15,-1,10", "15,11,10"},
            nullValues = "null")
    void createQuiz_invalidScoreCombination_isRejected(Integer duration, Integer pass, Integer total) {
        stubStaff();
        CreateQuizRequest req = createRequest();
        req.setDurationMin(duration);
        req.setPassScore(pass);
        req.setTotalScore(total);

        assertThrows(QuizBusinessException.class, () -> service.createQuiz(req, STAFF_EMAIL));
    }

    @ParameterizedTest
    @CsvSource({"0,10", "10,10"})
    void createQuiz_passScoreAtBoundaries_isAccepted(Integer pass, Integer total) {
        stubStaff();
        when(assessmentRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        CreateQuizRequest req = createRequest();
        req.setPassScore(pass);
        req.setTotalScore(total);

        assertNotNull(service.createQuiz(req, STAFF_EMAIL));
    }

    @Test
    void createQuiz_forcesTypeQuizAndDraftStatusAndTrims() {
        stubStaff();
        when(assessmentRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        CreateQuizRequest req = createRequest();
        req.setAssessmentType("exam"); // client gửi sai — phải bị ép về 'quiz'

        QuizDetailResponse response = service.createQuiz(req, STAFF_EMAIL);

        assertEquals("quiz", response.getAssessmentType());
        assertEquals("draft", response.getStatus());
        assertEquals("Quiz N5", response.getTitle());
        assertEquals("N5", response.getJlptLevel());
        assertEquals(1L, response.getCreatedBy());
        assertFalse(response.isScoreMatched());
    }

    // ── listQuizzes ──────────────────────────────────────────────────────────

    @Test
    void listQuizzes_normalizesFiltersAndCountsQuestions() {
        stubStaff();
        when(assessmentRepository.findQuizzesWithFilters(eq("N5"), eq("draft"), eq(5L), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(quiz("draft", 1L))));
        when(assignmentRepository.countByParentTypeAndParentId("assessment", 10L))
                .thenReturn(4L);

        QuizListResponse response = service.listQuizzes("  n5  ", "  DRAFT  ", 5L, 0, 20, STAFF_EMAIL);

        assertEquals(1, response.getTotalElements());
        assertEquals(4L, response.getContent().get(0).getQuestionCount());
    }

    @Test
    void listQuizzes_blankFilters_becomeNull() {
        stubStaff();
        when(assessmentRepository.findQuizzesWithFilters(isNull(), isNull(), isNull(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));

        assertEquals(
                0, service.listQuizzes("  ", null, null, 0, 20, STAFF_EMAIL).getTotalElements());
    }

    @Test
    void listQuizzes_pageAndSizeAreClamped() {
        stubStaff();
        when(assessmentRepository.findQuizzesWithFilters(isNull(), isNull(), isNull(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));

        service.listQuizzes(null, null, null, -3, 999, STAFF_EMAIL);

        verify(assessmentRepository)
                .findQuizzesWithFilters(
                        isNull(), isNull(), isNull(), argThat(p -> p.getPageSize() == 100 && p.getPageNumber() == 0));
    }

    @Test
    void listQuizzes_sizeBelowOne_isRaisedToOne() {
        stubStaff();
        when(assessmentRepository.findQuizzesWithFilters(isNull(), isNull(), isNull(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));

        service.listQuizzes(null, null, null, 0, 0, STAFF_EMAIL);

        verify(assessmentRepository)
                .findQuizzesWithFilters(isNull(), isNull(), isNull(), argThat(p -> p.getPageSize() == 1));
    }

    // ── getQuiz ──────────────────────────────────────────────────────────────

    @Test
    void getQuiz_buildsQuestionListAndScoreSum() {
        stubStaff();
        when(assessmentRepository.findByIdAndAssessmentTypeAndStatusNot(10L, "quiz", "deleted"))
                .thenReturn(Optional.of(quiz("draft", 1L)));
        QuizAssignmentEntity a1 = QuizAssignmentEntity.builder()
                .id(1L)
                .questionId(100L)
                .displayOrder(1)
                .score(new BigDecimal("6"))
                .build();
        QuizAssignmentEntity a2 = QuizAssignmentEntity.builder()
                .id(2L)
                .questionId(101L)
                .displayOrder(2)
                .score(new BigDecimal("4"))
                .build();
        when(assignmentRepository.findByParentTypeAndParentIdOrderByDisplayOrderAsc("assessment", 10L))
                .thenReturn(List.of(a1, a2));
        when(questionRefRepository.findById(100L)).thenReturn(Optional.of(questionRef(100L, "published")));
        when(questionRefRepository.findById(101L)).thenReturn(Optional.empty());

        QuizDetailResponse response = service.getQuiz(10L, STAFF_EMAIL);

        assertEquals(new BigDecimal("10"), response.getAssignedScoreSum());
        assertTrue(response.isScoreMatched());
        assertEquals("Câu hỏi 100", response.getQuestions().get(0).getQuestionText());
        assertNull(response.getQuestions().get(1).getQuestionText(), "câu hỏi đã bị xoá → text null");
    }

    @Test
    void getQuiz_missing_throwsAssessmentNotFound() {
        stubStaff();
        when(assessmentRepository.findByIdAndAssessmentTypeAndStatusNot(404L, "quiz", "deleted"))
                .thenReturn(Optional.empty());

        assertThrows(QuizBusinessException.class, () -> service.getQuiz(404L, STAFF_EMAIL));
    }

    // ── updateQuiz ───────────────────────────────────────────────────────────

    @Test
    void updateQuiz_draftQuiz_isUpdated() {
        stubStaff();
        when(assessmentRepository.findByIdAndAssessmentTypeAndStatusNot(10L, "quiz", "deleted"))
                .thenReturn(Optional.of(quiz("draft", 1L)));
        when(assessmentRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(assignmentRepository.findByParentTypeAndParentIdOrderByDisplayOrderAsc("assessment", 10L))
                .thenReturn(List.of());

        QuizDetailResponse response = service.updateQuiz(10L, updateRequest(), STAFF_EMAIL);

        assertEquals("Quiz mới", response.getTitle());
        assertEquals("N4", response.getJlptLevel());
        assertEquals(20, response.getDurationMin());
    }

    @Test
    void updateQuiz_withLessonId_validatesLessonExists() {
        stubStaff();
        when(assessmentRepository.findByIdAndAssessmentTypeAndStatusNot(10L, "quiz", "deleted"))
                .thenReturn(Optional.of(quiz("draft", 1L)));
        when(lessonRepository.findByIdAndStatusNot(404L, Lesson.LessonStatus.DELETED))
                .thenReturn(Optional.empty());
        UpdateQuizRequest req = updateRequest();
        req.setLessonId(404L);

        assertThrows(QuizBusinessException.class, () -> service.updateQuiz(10L, req, STAFF_EMAIL));
    }

    @Test
    void updateQuiz_requestingPublish_isRejectedBeforeLoadingQuiz() {
        stubStaff();
        UpdateQuizRequest req = updateRequest();
        req.setStatus("published");

        assertThrows(QuizBusinessException.class, () -> service.updateQuiz(10L, req, STAFF_EMAIL));
        verifyNoInteractions(assessmentRepository);
    }

    @ParameterizedTest
    @ValueSource(strings = {"pending_review", "published"})
    void updateQuiz_nonEditableStatus_isRejected(String status) {
        stubStaff();
        when(assessmentRepository.findByIdAndAssessmentTypeAndStatusNot(10L, "quiz", "deleted"))
                .thenReturn(Optional.of(quiz(status, 1L)));

        UpdateQuizRequest req = updateRequest();
        assertThrows(QuizBusinessException.class, () -> service.updateQuiz(10L, req, STAFF_EMAIL));
    }

    @Test
    void updateQuiz_rejectedQuiz_isEditable() {
        stubStaff();
        when(assessmentRepository.findByIdAndAssessmentTypeAndStatusNot(10L, "quiz", "deleted"))
                .thenReturn(Optional.of(quiz("rejected", 1L)));
        when(assessmentRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(assignmentRepository.findByParentTypeAndParentIdOrderByDisplayOrderAsc("assessment", 10L))
                .thenReturn(List.of());

        assertNotNull(service.updateQuiz(10L, updateRequest(), STAFF_EMAIL));
    }

    @Test
    void updateQuiz_ofAnotherStaff_throwsOwnershipDenied() {
        stubStaff();
        when(assessmentRepository.findByIdAndAssessmentTypeAndStatusNot(10L, "quiz", "deleted"))
                .thenReturn(Optional.of(quiz("draft", 99L)));

        UpdateQuizRequest req = updateRequest();
        assertThrows(QuizBusinessException.class, () -> service.updateQuiz(10L, req, STAFF_EMAIL));
    }

    @Test
    void updateQuiz_quizWithoutOwner_throwsOwnershipDenied() {
        stubStaff();
        when(assessmentRepository.findByIdAndAssessmentTypeAndStatusNot(10L, "quiz", "deleted"))
                .thenReturn(Optional.of(quiz("draft", null)));

        UpdateQuizRequest req = updateRequest();
        assertThrows(QuizBusinessException.class, () -> service.updateQuiz(10L, req, STAFF_EMAIL));
    }

    @Test
    void updateQuiz_asManagerOnOtherStaffQuiz_isAllowed() {
        when(staffUserRepository.findByEmail(MANAGER_EMAIL)).thenReturn(Optional.of(manager));
        when(assessmentRepository.findByIdAndAssessmentTypeAndStatusNot(10L, "quiz", "deleted"))
                .thenReturn(Optional.of(quiz("draft", 99L)));
        when(assessmentRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(assignmentRepository.findByParentTypeAndParentIdOrderByDisplayOrderAsc("assessment", 10L))
                .thenReturn(List.of());

        assertNotNull(service.updateQuiz(10L, updateRequest(), MANAGER_EMAIL));
    }

    // ── assignQuestions ──────────────────────────────────────────────────────

    @Test
    void assignQuestions_validPayload_replacesAndReportsScoreMatch() {
        stubStaff();
        when(assessmentRepository.findByIdAndAssessmentTypeAndStatusNot(10L, "quiz", "deleted"))
                .thenReturn(Optional.of(quiz("draft", 1L)));
        when(questionRefRepository.findById(100L)).thenReturn(Optional.of(questionRef(100L, "published")));
        when(questionRefRepository.findById(101L)).thenReturn(Optional.of(questionRef(101L, "PUBLISHED")));

        AssignResultResponse response = service.assignQuestions(
                10L, assignRequest(item(100L, 1, "6", "  phần 1  "), item(101L, 2, "4", "   ")), STAFF_EMAIL);

        assertEquals(2, response.getAssignedCount());
        assertEquals(new BigDecimal("10"), response.getAssignedScoreSum());
        assertTrue(response.isScoreMatched());
        verify(assignmentRepository).deleteByParent("assessment", 10L);
        verify(assignmentRepository).saveAll(any());
    }

    @Test
    void assignQuestions_scoreSumMismatch_reportsFalseWithoutThrowing() {
        stubStaff();
        when(assessmentRepository.findByIdAndAssessmentTypeAndStatusNot(10L, "quiz", "deleted"))
                .thenReturn(Optional.of(quiz("draft", 1L)));
        when(questionRefRepository.findById(100L)).thenReturn(Optional.of(questionRef(100L, "published")));

        AssignResultResponse response =
                service.assignQuestions(10L, assignRequest(item(100L, 1, "3", null)), STAFF_EMAIL);

        assertFalse(response.isScoreMatched());
        assertEquals(new BigDecimal("3"), response.getAssignedScoreSum());
    }

    @Test
    void assignQuestions_emptyPayload_clearsAllAssignments() {
        stubStaff();
        when(assessmentRepository.findByIdAndAssessmentTypeAndStatusNot(10L, "quiz", "deleted"))
                .thenReturn(Optional.of(quiz("draft", 1L)));

        AssignResultResponse response = service.assignQuestions(10L, assignRequest(), STAFF_EMAIL);

        assertEquals(0, response.getAssignedCount());
        assertEquals(BigDecimal.ZERO, response.getAssignedScoreSum());
        verify(assignmentRepository).deleteByParent("assessment", 10L);
    }

    @Test
    void assignQuestions_duplicateQuestionInPayload_isRejected() {
        stubStaff();
        when(assessmentRepository.findByIdAndAssessmentTypeAndStatusNot(10L, "quiz", "deleted"))
                .thenReturn(Optional.of(quiz("draft", 1L)));
        AssignQuestionsRequest req = assignRequest(item(100L, 1, "5", null), item(100L, 2, "5", null));

        assertThrows(QuizBusinessException.class, () -> service.assignQuestions(10L, req, STAFF_EMAIL));
        verify(assignmentRepository, never()).deleteByParent(anyString(), anyLong());
    }

    @Test
    void assignQuestions_questionNotFound_isRejected() {
        stubStaff();
        when(assessmentRepository.findByIdAndAssessmentTypeAndStatusNot(10L, "quiz", "deleted"))
                .thenReturn(Optional.of(quiz("draft", 1L)));
        when(questionRefRepository.findById(404L)).thenReturn(Optional.empty());
        AssignQuestionsRequest req = assignRequest(item(404L, 1, "10", null));

        assertThrows(QuizBusinessException.class, () -> service.assignQuestions(10L, req, STAFF_EMAIL));
    }

    @Test
    void assignQuestions_deletedQuestion_isTreatedAsNotFound() {
        stubStaff();
        when(assessmentRepository.findByIdAndAssessmentTypeAndStatusNot(10L, "quiz", "deleted"))
                .thenReturn(Optional.of(quiz("draft", 1L)));
        when(questionRefRepository.findById(100L)).thenReturn(Optional.of(questionRef(100L, "DELETED")));
        AssignQuestionsRequest req = assignRequest(item(100L, 1, "10", null));

        assertThrows(QuizBusinessException.class, () -> service.assignQuestions(10L, req, STAFF_EMAIL));
    }

    @Test
    void assignQuestions_questionWithNullStatus_isTreatedAsNotFound() {
        stubStaff();
        when(assessmentRepository.findByIdAndAssessmentTypeAndStatusNot(10L, "quiz", "deleted"))
                .thenReturn(Optional.of(quiz("draft", 1L)));
        when(questionRefRepository.findById(100L)).thenReturn(Optional.of(questionRef(100L, null)));
        AssignQuestionsRequest req = assignRequest(item(100L, 1, "10", null));

        assertThrows(QuizBusinessException.class, () -> service.assignQuestions(10L, req, STAFF_EMAIL));
    }

    @Test
    void assignQuestions_draftQuestion_isRejectedAsNotPublished() {
        stubStaff();
        when(assessmentRepository.findByIdAndAssessmentTypeAndStatusNot(10L, "quiz", "deleted"))
                .thenReturn(Optional.of(quiz("draft", 1L)));
        when(questionRefRepository.findById(100L)).thenReturn(Optional.of(questionRef(100L, "draft")));
        AssignQuestionsRequest req = assignRequest(item(100L, 1, "10", null));

        assertThrows(QuizBusinessException.class, () -> service.assignQuestions(10L, req, STAFF_EMAIL));
    }

    @Test
    void assignQuestions_publishedQuiz_isRejected() {
        stubStaff();
        when(assessmentRepository.findByIdAndAssessmentTypeAndStatusNot(10L, "quiz", "deleted"))
                .thenReturn(Optional.of(quiz("published", 1L)));
        AssignQuestionsRequest req = assignRequest(item(100L, 1, "10", null));

        assertThrows(QuizBusinessException.class, () -> service.assignQuestions(10L, req, STAFF_EMAIL));
    }

    @Test
    void assignQuestions_pendingReviewQuiz_isRejected() {
        stubStaff();
        when(assessmentRepository.findByIdAndAssessmentTypeAndStatusNot(10L, "quiz", "deleted"))
                .thenReturn(Optional.of(quiz("pending_review", 1L)));
        AssignQuestionsRequest req = assignRequest(item(100L, 1, "10", null));

        assertThrows(QuizBusinessException.class, () -> service.assignQuestions(10L, req, STAFF_EMAIL));
    }

    @Test
    void assignQuestions_ofAnotherStaff_throwsOwnershipDenied() {
        stubStaff();
        when(assessmentRepository.findByIdAndAssessmentTypeAndStatusNot(10L, "quiz", "deleted"))
                .thenReturn(Optional.of(quiz("draft", 99L)));
        AssignQuestionsRequest req = assignRequest(item(100L, 1, "10", null));

        assertThrows(QuizBusinessException.class, () -> service.assignQuestions(10L, req, STAFF_EMAIL));
    }

    // ── submitForReview ──────────────────────────────────────────────────────

    @Test
    void submitForReview_draftQuizWithQuestions_transitionsToPending() {
        stubStaff();
        QuizAssessmentEntity existing = quiz("draft", 1L);
        when(assessmentRepository.findByIdAndAssessmentTypeAndStatusNot(10L, "quiz", "deleted"))
                .thenReturn(Optional.of(existing));
        when(assignmentRepository.countByParentTypeAndParentId("assessment", 10L))
                .thenReturn(3L);

        assertEquals("pending_review", service.submitForReview(10L, STAFF_EMAIL).getStatus());
        assertEquals("pending_review", existing.getStatus());
        verify(assessmentRepository).save(existing);
    }

    @Test
    void submitForReview_emptyQuiz_isRejected() {
        stubStaff();
        when(assessmentRepository.findByIdAndAssessmentTypeAndStatusNot(10L, "quiz", "deleted"))
                .thenReturn(Optional.of(quiz("draft", 1L)));
        when(assignmentRepository.countByParentTypeAndParentId("assessment", 10L))
                .thenReturn(0L);

        assertThrows(QuizBusinessException.class, () -> service.submitForReview(10L, STAFF_EMAIL));
        verify(assessmentRepository, never()).save(any());
    }

    @Test
    void submitForReview_rejectedQuiz_isAllowed() {
        stubStaff();
        when(assessmentRepository.findByIdAndAssessmentTypeAndStatusNot(10L, "quiz", "deleted"))
                .thenReturn(Optional.of(quiz("rejected", 1L)));
        when(assignmentRepository.countByParentTypeAndParentId("assessment", 10L))
                .thenReturn(3L);

        assertEquals("assessment", service.submitForReview(10L, STAFF_EMAIL).getContentType());
    }

    @Test
    void submitForReview_scoreMismatch_isStillAllowedAtThisStage() {
        stubStaff();
        QuizAssessmentEntity existing = quiz("draft", 1L);
        existing.setTotalScore(999);
        when(assessmentRepository.findByIdAndAssessmentTypeAndStatusNot(10L, "quiz", "deleted"))
                .thenReturn(Optional.of(existing));
        when(assignmentRepository.countByParentTypeAndParentId("assessment", 10L))
                .thenReturn(1L);

        assertDoesNotThrow(() -> service.submitForReview(10L, STAFF_EMAIL));
    }

    @ParameterizedTest
    @ValueSource(strings = {"pending_review", "published"})
    void submitForReview_nonEditableStatus_isRejected(String status) {
        stubStaff();
        when(assessmentRepository.findByIdAndAssessmentTypeAndStatusNot(10L, "quiz", "deleted"))
                .thenReturn(Optional.of(quiz(status, 1L)));
        when(assignmentRepository.countByParentTypeAndParentId("assessment", 10L))
                .thenReturn(3L);

        assertThrows(QuizBusinessException.class, () -> service.submitForReview(10L, STAFF_EMAIL));
    }

    @Test
    void submitForReview_ofAnotherStaff_throwsOwnershipDenied() {
        stubStaff();
        when(assessmentRepository.findByIdAndAssessmentTypeAndStatusNot(10L, "quiz", "deleted"))
                .thenReturn(Optional.of(quiz("draft", 99L)));

        assertThrows(QuizBusinessException.class, () -> service.submitForReview(10L, STAFF_EMAIL));
    }
}
