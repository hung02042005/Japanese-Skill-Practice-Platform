/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.staffcontent.exam;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.*;

import com.jlpt.feature.staff.StaffUser;
import com.jlpt.feature.staff.StaffUserRepository;
import com.jlpt.feature.staffcontent.exam.dto.CreateExamRequest;
import com.jlpt.feature.staffcontent.exam.dto.ExamAssignQuestionsRequest;
import com.jlpt.feature.staffcontent.exam.dto.ExamAssignResultResponse;
import com.jlpt.feature.staffcontent.exam.dto.ExamDetailResponse;
import com.jlpt.feature.staffcontent.exam.dto.ExamListResponse;
import com.jlpt.feature.staffcontent.exam.dto.UpdateExamRequest;
import com.jlpt.feature.staffcontent.exam.entity.ExamAssessmentEntity;
import com.jlpt.feature.staffcontent.exam.entity.ExamAssignmentEntity;
import com.jlpt.feature.staffcontent.exam.entity.ExamQuestionRefEntity;
import com.jlpt.feature.staffcontent.exam.exception.ExamBusinessException;
import com.jlpt.feature.staffcontent.exam.repository.ExamAssessmentRepository;
import com.jlpt.feature.staffcontent.exam.repository.ExamAssignmentRepository;
import com.jlpt.feature.staffcontent.exam.repository.ExamQuestionRefRepository;
import com.jlpt.feature.staffcontent.exam.service.StaffExamService;
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
 * UC-28 đề thi thử — phủ NHÁNH: validate level/điểm (FR-28-02..04), chặn Staff tự publish
 * (FR-28-33), quyền sở hữu (FR-28-34), chỉ sửa khi draft/rejected (FR-28-17/29) và toàn bộ luật
 * gán câu hỏi (trùng, section sai, câu chưa publish, lệch cấp độ — FR-28-21/22/25).
 */
@ExtendWith(MockitoExtension.class)
class StaffExamServiceCoverageTest {

    private static final String STAFF_EMAIL = "staff@sakuji.com";
    private static final String MANAGER_EMAIL = "manager@sakuji.com";

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

    private ExamAssessmentEntity exam(String status, Long owner) {
        return ExamAssessmentEntity.builder()
                .id(10L)
                .assessmentType("exam")
                .title("Đề thi N5")
                .jlptLevel("N5")
                .durationMin(90)
                .passScore(80)
                .totalScore(180)
                .status(status)
                .createdBy(owner)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    private CreateExamRequest createRequest() {
        CreateExamRequest req = new CreateExamRequest();
        req.setTitle("  Đề thi N5  ");
        req.setJlptLevel("  n5  ");
        req.setDurationMin(90);
        req.setPassScore(80);
        req.setTotalScore(180);
        return req;
    }

    private UpdateExamRequest updateRequest() {
        UpdateExamRequest req = new UpdateExamRequest();
        req.setTitle("  Đề mới  ");
        req.setJlptLevel("n4");
        req.setDurationMin(60);
        req.setPassScore(50);
        req.setTotalScore(100);
        return req;
    }

    private ExamAssignQuestionsRequest.ExamAssignmentItem item(
            Long questionId, String section, int order, String score) {
        ExamAssignQuestionsRequest.ExamAssignmentItem it = new ExamAssignQuestionsRequest.ExamAssignmentItem();
        it.setQuestionId(questionId);
        it.setSectionName(section);
        it.setDisplayOrder(order);
        it.setScore(new BigDecimal(score));
        return it;
    }

    private ExamAssignQuestionsRequest assignRequest(ExamAssignQuestionsRequest.ExamAssignmentItem... items) {
        ExamAssignQuestionsRequest req = new ExamAssignQuestionsRequest();
        req.setAssignments(List.of(items));
        return req;
    }

    /**
     * ExamQuestionRefEntity chỉ có @Getter — không builder, không setter. Dùng mock ở đây sẽ hỏng vì
     * helper này được gọi lồng trong thenReturn(...) (Mockito báo UnfinishedStubbing), nên dựng
     * instance thật rồi set field bằng reflection.
     */
    private ExamQuestionRefEntity questionRef(Long id, String status, String level) {
        ExamQuestionRefEntity ref = new ExamQuestionRefEntity();
        ReflectionTestUtils.setField(ref, "id", id);
        ReflectionTestUtils.setField(ref, "questionText", "Câu hỏi " + id);
        ReflectionTestUtils.setField(ref, "status", status);
        ReflectionTestUtils.setField(ref, "jlptLevel", level);
        return ref;
    }

    // ── resolveStaff ─────────────────────────────────────────────────────────

    @Test
    void anyOperation_unknownStaff_throwsForbidden() {
        when(staffUserRepository.findByEmail("ghost@sakuji.com")).thenReturn(Optional.empty());
        CreateExamRequest req = createRequest();

        assertThrows(ForbiddenException.class, () -> service.createExam(req, "ghost@sakuji.com"));
    }

    // ── createExam: guardNoPublish ───────────────────────────────────────────

    @ParameterizedTest
    @ValueSource(strings = {"published", "PUBLISHED", "  archived  "})
    void createExam_requestingPublishOrArchive_isRejected(String status) {
        stubStaff();
        CreateExamRequest req = createRequest();
        req.setStatus(status);

        assertThrows(ExamBusinessException.class, () -> service.createExam(req, STAFF_EMAIL));
        verify(assessmentRepository, never()).save(any());
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"draft", "rejected"})
    void createExam_harmlessStatus_isAllowed(String status) {
        stubStaff();
        when(assessmentRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        CreateExamRequest req = createRequest();
        req.setStatus(status);

        assertNotNull(service.createExam(req, STAFF_EMAIL));
    }

    // ── createExam: validateLevel ────────────────────────────────────────────

    @ParameterizedTest
    @ValueSource(strings = {"N5", "n4", "  N3  ", "n2", "N1"})
    void createExam_everyValidLevel_isAcceptedAndUppercased(String level) {
        stubStaff();
        when(assessmentRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        CreateExamRequest req = createRequest();
        req.setJlptLevel(level);

        ExamDetailResponse response = service.createExam(req, STAFF_EMAIL);

        assertEquals(level.trim().toUpperCase(), response.getJlptLevel());
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"N9", "JLPT", ""})
    void createExam_invalidLevel_isRejected(String level) {
        stubStaff();
        CreateExamRequest req = createRequest();
        req.setJlptLevel(level);

        assertThrows(ExamBusinessException.class, () -> service.createExam(req, STAFF_EMAIL));
    }

    // ── createExam: validateScoreRange (từng vế của điều kiện OR) ────────────

    @ParameterizedTest
    @CsvSource(
            value = {
                "null,80,180",
                "0,80,180",
                "-5,80,180",
                "90,80,null",
                "90,80,0",
                "90,null,180",
                "90,-1,180",
                "90,200,180"
            },
            nullValues = "null")
    void createExam_invalidScoreCombination_isRejected(Integer duration, Integer pass, Integer total) {
        stubStaff();
        CreateExamRequest req = createRequest();
        req.setDurationMin(duration);
        req.setPassScore(pass);
        req.setTotalScore(total);

        assertThrows(ExamBusinessException.class, () -> service.createExam(req, STAFF_EMAIL));
    }

    @Test
    void createExam_passScoreEqualToTotal_isAccepted() {
        stubStaff();
        when(assessmentRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        CreateExamRequest req = createRequest();
        req.setPassScore(180);

        assertNotNull(service.createExam(req, STAFF_EMAIL));
    }

    @Test
    void createExam_zeroPassScore_isAccepted() {
        stubStaff();
        when(assessmentRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        CreateExamRequest req = createRequest();
        req.setPassScore(0);

        assertNotNull(service.createExam(req, STAFF_EMAIL));
    }

    @Test
    void createExam_forcesTypeExamAndDraftStatusAndTrimsFields() {
        stubStaff();
        when(assessmentRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        CreateExamRequest req = createRequest();
        req.setAssessmentType("quiz"); // client cố tình gửi sai — phải bị ép về 'exam'
        req.setDescription("   ");

        ExamDetailResponse response = service.createExam(req, STAFF_EMAIL);

        assertEquals("exam", response.getAssessmentType());
        assertEquals("draft", response.getStatus());
        assertEquals("Đề thi N5", response.getTitle());
        assertNull(response.getDescription());
        assertEquals(1L, response.getCreatedBy());
        assertFalse(response.isScoreMatched(), "chưa gán câu hỏi nên tổng điểm chưa khớp");
    }

    // ── listExams ────────────────────────────────────────────────────────────

    @Test
    void listExams_normalizesFiltersAndCountsQuestions() {
        stubStaff();
        when(assessmentRepository.findExamsWithFilters(eq("N5"), eq("draft"), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(exam("draft", 1L))));
        when(assignmentRepository.countByParentTypeAndParentId("assessment", 10L))
                .thenReturn(12L);

        ExamListResponse response = service.listExams("  n5  ", "  DRAFT  ", 0, 20, STAFF_EMAIL);

        assertEquals(1, response.getTotalElements());
        assertEquals(12L, response.getContent().get(0).getQuestionCount());
        assertEquals("1", response.getContent().get(0).getCreatedBy());
    }

    @Test
    void listExams_blankFilters_becomeNull() {
        stubStaff();
        when(assessmentRepository.findExamsWithFilters(isNull(), isNull(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));

        assertEquals(0, service.listExams("  ", null, 0, 20, STAFF_EMAIL).getTotalElements());
    }

    @Test
    void listExams_sizeAndPageAreClamped() {
        stubStaff();
        when(assessmentRepository.findExamsWithFilters(isNull(), isNull(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));

        service.listExams(null, null, -5, 999, STAFF_EMAIL);

        verify(assessmentRepository)
                .findExamsWithFilters(
                        isNull(), isNull(), argThat(p -> p.getPageSize() == 100 && p.getPageNumber() == 0));
    }

    @Test
    void listExams_sizeBelowOne_isRaisedToOne() {
        stubStaff();
        when(assessmentRepository.findExamsWithFilters(isNull(), isNull(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));

        service.listExams(null, null, 0, 0, STAFF_EMAIL);

        verify(assessmentRepository).findExamsWithFilters(isNull(), isNull(), argThat(p -> p.getPageSize() == 1));
    }

    // ── getExam ──────────────────────────────────────────────────────────────

    @Test
    void getExam_buildsSectionsAndQuestionsWithScoreSum() {
        stubStaff();
        when(assessmentRepository.findByIdAndAssessmentTypeAndStatusNot(10L, "exam", "deleted"))
                .thenReturn(Optional.of(exam("draft", 1L)));
        ExamAssignmentEntity a1 = ExamAssignmentEntity.builder()
                .id(1L)
                .questionId(100L)
                .sectionName("vocabulary")
                .displayOrder(1)
                .score(new BigDecimal("100"))
                .build();
        ExamAssignmentEntity a2 = ExamAssignmentEntity.builder()
                .id(2L)
                .questionId(101L)
                .sectionName("grammar")
                .displayOrder(2)
                .score(new BigDecimal("80"))
                .build();
        when(assignmentRepository.findByParentTypeAndParentIdOrderByDisplayOrderAsc("assessment", 10L))
                .thenReturn(List.of(a1, a2));
        when(assignmentRepository.sumBySectionGrouped("assessment", 10L))
                .thenReturn(List.of(
                        new Object[] {"vocabulary", new BigDecimal("100"), 1L},
                        new Object[] {"grammar", new BigDecimal("80"), 1L}));
        when(questionRefRepository.findById(100L)).thenReturn(Optional.of(questionRef(100L, "published", "N5")));
        when(questionRefRepository.findById(101L)).thenReturn(Optional.empty());

        ExamDetailResponse response = service.getExam(10L, STAFF_EMAIL);

        assertEquals(new BigDecimal("180"), response.getAssignedScoreSum());
        assertTrue(response.isScoreMatched());
        assertEquals("grammar", response.getSections().get(0).getSectionName(), "section sắp xếp theo tên");
        assertEquals("Câu hỏi 100", response.getQuestions().get(0).getQuestionText());
        assertNull(response.getQuestions().get(1).getQuestionText(), "câu hỏi đã bị xoá → text null");
    }

    @Test
    void getExam_missing_throwsExamNotFound() {
        stubStaff();
        when(assessmentRepository.findByIdAndAssessmentTypeAndStatusNot(404L, "exam", "deleted"))
                .thenReturn(Optional.empty());

        assertThrows(ExamBusinessException.class, () -> service.getExam(404L, STAFF_EMAIL));
    }

    // ── updateExam ───────────────────────────────────────────────────────────

    @Test
    void updateExam_draftExam_isUpdated() {
        stubStaff();
        ExamAssessmentEntity existing = exam("draft", 1L);
        when(assessmentRepository.findByIdAndAssessmentTypeAndStatusNot(10L, "exam", "deleted"))
                .thenReturn(Optional.of(existing));
        when(assessmentRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(assignmentRepository.findByParentTypeAndParentIdOrderByDisplayOrderAsc("assessment", 10L))
                .thenReturn(List.of());
        when(assignmentRepository.sumBySectionGrouped("assessment", 10L)).thenReturn(List.of());

        ExamDetailResponse response = service.updateExam(10L, updateRequest(), STAFF_EMAIL);

        assertEquals("Đề mới", response.getTitle());
        assertEquals("N4", response.getJlptLevel());
        assertEquals(60, response.getDurationMin());
    }

    @ParameterizedTest
    @ValueSource(strings = {"pending_review", "published"})
    void updateExam_nonEditableStatus_isRejected(String status) {
        stubStaff();
        when(assessmentRepository.findByIdAndAssessmentTypeAndStatusNot(10L, "exam", "deleted"))
                .thenReturn(Optional.of(exam(status, 1L)));

        UpdateExamRequest req = updateRequest();
        assertThrows(ExamBusinessException.class, () -> service.updateExam(10L, req, STAFF_EMAIL));
    }

    @Test
    void updateExam_rejectedExam_isEditable() {
        stubStaff();
        when(assessmentRepository.findByIdAndAssessmentTypeAndStatusNot(10L, "exam", "deleted"))
                .thenReturn(Optional.of(exam("rejected", 1L)));
        when(assessmentRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(assignmentRepository.findByParentTypeAndParentIdOrderByDisplayOrderAsc("assessment", 10L))
                .thenReturn(List.of());
        when(assignmentRepository.sumBySectionGrouped("assessment", 10L)).thenReturn(List.of());

        assertNotNull(service.updateExam(10L, updateRequest(), STAFF_EMAIL));
    }

    @Test
    void updateExam_ofAnotherStaff_throwsOwnershipDenied() {
        stubStaff();
        when(assessmentRepository.findByIdAndAssessmentTypeAndStatusNot(10L, "exam", "deleted"))
                .thenReturn(Optional.of(exam("draft", 99L)));

        UpdateExamRequest req = updateRequest();
        assertThrows(ExamBusinessException.class, () -> service.updateExam(10L, req, STAFF_EMAIL));
    }

    @Test
    void updateExam_examWithoutOwner_throwsOwnershipDenied() {
        stubStaff();
        when(assessmentRepository.findByIdAndAssessmentTypeAndStatusNot(10L, "exam", "deleted"))
                .thenReturn(Optional.of(exam("draft", null)));

        UpdateExamRequest req = updateRequest();
        assertThrows(ExamBusinessException.class, () -> service.updateExam(10L, req, STAFF_EMAIL));
    }

    @Test
    void updateExam_asManagerOnOtherStaffExam_isAllowed() {
        when(staffUserRepository.findByEmail(MANAGER_EMAIL)).thenReturn(Optional.of(manager));
        when(assessmentRepository.findByIdAndAssessmentTypeAndStatusNot(10L, "exam", "deleted"))
                .thenReturn(Optional.of(exam("draft", 99L)));
        when(assessmentRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(assignmentRepository.findByParentTypeAndParentIdOrderByDisplayOrderAsc("assessment", 10L))
                .thenReturn(List.of());
        when(assignmentRepository.sumBySectionGrouped("assessment", 10L)).thenReturn(List.of());

        assertNotNull(service.updateExam(10L, updateRequest(), MANAGER_EMAIL));
    }

    // ── assignQuestions ──────────────────────────────────────────────────────

    @Test
    void assignQuestions_validPayload_replacesAndReportsScoreMatch() {
        stubStaff();
        when(assessmentRepository.findByIdAndAssessmentTypeAndStatusNot(10L, "exam", "deleted"))
                .thenReturn(Optional.of(exam("draft", 1L)));
        when(questionRefRepository.findById(100L)).thenReturn(Optional.of(questionRef(100L, "published", "N5")));
        when(questionRefRepository.findById(101L)).thenReturn(Optional.of(questionRef(101L, "published", "n5")));

        ExamAssignResultResponse response = service.assignQuestions(
                10L,
                assignRequest(item(100L, "  VOCABULARY  ", 1, "100"), item(101L, "grammar", 2, "80")),
                STAFF_EMAIL);

        assertEquals(2, response.getAssignedCount());
        assertEquals(new BigDecimal("180"), response.getAssignedScoreSum());
        assertTrue(response.isScoreMatched());
        assertEquals("grammar", response.getSectionSummaries().get(0).getSectionName());
        assertEquals(1, response.getSectionSummaries().get(0).getQuestionCount());
        verify(assignmentRepository).deleteByParent("assessment", 10L);
        verify(assignmentRepository).saveAll(any());
    }

    @Test
    void assignQuestions_scoreSumMismatch_reportsFalseWithoutThrowing() {
        stubStaff();
        when(assessmentRepository.findByIdAndAssessmentTypeAndStatusNot(10L, "exam", "deleted"))
                .thenReturn(Optional.of(exam("draft", 1L)));
        when(questionRefRepository.findById(100L)).thenReturn(Optional.of(questionRef(100L, "published", "N5")));

        ExamAssignResultResponse response =
                service.assignQuestions(10L, assignRequest(item(100L, "reading", 1, "50")), STAFF_EMAIL);

        assertFalse(response.isScoreMatched());
        assertEquals(new BigDecimal("50"), response.getAssignedScoreSum());
    }

    @Test
    void assignQuestions_emptyPayload_clearsAllAssignments() {
        stubStaff();
        when(assessmentRepository.findByIdAndAssessmentTypeAndStatusNot(10L, "exam", "deleted"))
                .thenReturn(Optional.of(exam("draft", 1L)));

        ExamAssignResultResponse response = service.assignQuestions(10L, assignRequest(), STAFF_EMAIL);

        assertEquals(0, response.getAssignedCount());
        assertEquals(BigDecimal.ZERO, response.getAssignedScoreSum());
        verify(assignmentRepository).deleteByParent("assessment", 10L);
    }

    @Test
    void assignQuestions_duplicateQuestionInPayload_isRejected() {
        stubStaff();
        when(assessmentRepository.findByIdAndAssessmentTypeAndStatusNot(10L, "exam", "deleted"))
                .thenReturn(Optional.of(exam("draft", 1L)));
        ExamAssignQuestionsRequest req =
                assignRequest(item(100L, "vocabulary", 1, "90"), item(100L, "grammar", 2, "90"));

        assertThrows(ExamBusinessException.class, () -> service.assignQuestions(10L, req, STAFF_EMAIL));
        verify(assignmentRepository, never()).deleteByParent(anyString(), anyLong());
    }

    @Test
    void assignQuestions_invalidSectionName_isRejected() {
        stubStaff();
        when(assessmentRepository.findByIdAndAssessmentTypeAndStatusNot(10L, "exam", "deleted"))
                .thenReturn(Optional.of(exam("draft", 1L)));
        ExamAssignQuestionsRequest req = assignRequest(item(100L, "speaking", 1, "180"));

        assertThrows(ExamBusinessException.class, () -> service.assignQuestions(10L, req, STAFF_EMAIL));
    }

    @ParameterizedTest
    @ValueSource(strings = {"vocabulary", "grammar", "kanji", "reading", "listening"})
    void assignQuestions_everyValidSection_isAccepted(String section) {
        stubStaff();
        when(assessmentRepository.findByIdAndAssessmentTypeAndStatusNot(10L, "exam", "deleted"))
                .thenReturn(Optional.of(exam("draft", 1L)));
        when(questionRefRepository.findById(100L)).thenReturn(Optional.of(questionRef(100L, "published", "N5")));

        assertEquals(
                1,
                service.assignQuestions(10L, assignRequest(item(100L, section, 1, "180")), STAFF_EMAIL)
                        .getAssignedCount());
    }

    @Test
    void assignQuestions_questionNotFound_isRejected() {
        stubStaff();
        when(assessmentRepository.findByIdAndAssessmentTypeAndStatusNot(10L, "exam", "deleted"))
                .thenReturn(Optional.of(exam("draft", 1L)));
        when(questionRefRepository.findById(404L)).thenReturn(Optional.empty());
        ExamAssignQuestionsRequest req = assignRequest(item(404L, "vocabulary", 1, "180"));

        assertThrows(ExamBusinessException.class, () -> service.assignQuestions(10L, req, STAFF_EMAIL));
    }

    @Test
    void assignQuestions_deletedQuestion_isTreatedAsNotFound() {
        stubStaff();
        when(assessmentRepository.findByIdAndAssessmentTypeAndStatusNot(10L, "exam", "deleted"))
                .thenReturn(Optional.of(exam("draft", 1L)));
        when(questionRefRepository.findById(100L)).thenReturn(Optional.of(questionRef(100L, "DELETED", "N5")));
        ExamAssignQuestionsRequest req = assignRequest(item(100L, "vocabulary", 1, "180"));

        assertThrows(ExamBusinessException.class, () -> service.assignQuestions(10L, req, STAFF_EMAIL));
    }

    @Test
    void assignQuestions_questionWithNullStatus_isTreatedAsNotFound() {
        stubStaff();
        when(assessmentRepository.findByIdAndAssessmentTypeAndStatusNot(10L, "exam", "deleted"))
                .thenReturn(Optional.of(exam("draft", 1L)));
        when(questionRefRepository.findById(100L)).thenReturn(Optional.of(questionRef(100L, null, "N5")));
        ExamAssignQuestionsRequest req = assignRequest(item(100L, "vocabulary", 1, "180"));

        assertThrows(ExamBusinessException.class, () -> service.assignQuestions(10L, req, STAFF_EMAIL));
    }

    @Test
    void assignQuestions_draftQuestion_isRejectedAsNotPublished() {
        stubStaff();
        when(assessmentRepository.findByIdAndAssessmentTypeAndStatusNot(10L, "exam", "deleted"))
                .thenReturn(Optional.of(exam("draft", 1L)));
        when(questionRefRepository.findById(100L)).thenReturn(Optional.of(questionRef(100L, "draft", "N5")));
        ExamAssignQuestionsRequest req = assignRequest(item(100L, "vocabulary", 1, "180"));

        assertThrows(ExamBusinessException.class, () -> service.assignQuestions(10L, req, STAFF_EMAIL));
    }

    @Test
    void assignQuestions_questionOfDifferentLevel_isRejected() {
        stubStaff();
        when(assessmentRepository.findByIdAndAssessmentTypeAndStatusNot(10L, "exam", "deleted"))
                .thenReturn(Optional.of(exam("draft", 1L)));
        when(questionRefRepository.findById(100L)).thenReturn(Optional.of(questionRef(100L, "published", "N2")));
        ExamAssignQuestionsRequest req = assignRequest(item(100L, "vocabulary", 1, "180"));

        assertThrows(ExamBusinessException.class, () -> service.assignQuestions(10L, req, STAFF_EMAIL));
    }

    /** Câu hỏi không gán cấp độ → không chặn (chỉ chặn khi có level và lệch). */
    @Test
    void assignQuestions_questionWithoutLevel_isAccepted() {
        stubStaff();
        when(assessmentRepository.findByIdAndAssessmentTypeAndStatusNot(10L, "exam", "deleted"))
                .thenReturn(Optional.of(exam("draft", 1L)));
        when(questionRefRepository.findById(100L)).thenReturn(Optional.of(questionRef(100L, "published", null)));

        assertEquals(
                1,
                service.assignQuestions(10L, assignRequest(item(100L, "vocabulary", 1, "180")), STAFF_EMAIL)
                        .getAssignedCount());
    }

    @Test
    void assignQuestions_publishedExam_isRejected() {
        stubStaff();
        when(assessmentRepository.findByIdAndAssessmentTypeAndStatusNot(10L, "exam", "deleted"))
                .thenReturn(Optional.of(exam("published", 1L)));
        ExamAssignQuestionsRequest req = assignRequest(item(100L, "vocabulary", 1, "180"));

        assertThrows(ExamBusinessException.class, () -> service.assignQuestions(10L, req, STAFF_EMAIL));
    }

    @Test
    void assignQuestions_pendingReviewExam_isRejected() {
        stubStaff();
        when(assessmentRepository.findByIdAndAssessmentTypeAndStatusNot(10L, "exam", "deleted"))
                .thenReturn(Optional.of(exam("pending_review", 1L)));
        ExamAssignQuestionsRequest req = assignRequest(item(100L, "vocabulary", 1, "180"));

        assertThrows(ExamBusinessException.class, () -> service.assignQuestions(10L, req, STAFF_EMAIL));
    }

    @Test
    void assignQuestions_ofAnotherStaff_throwsOwnershipDenied() {
        stubStaff();
        when(assessmentRepository.findByIdAndAssessmentTypeAndStatusNot(10L, "exam", "deleted"))
                .thenReturn(Optional.of(exam("draft", 99L)));
        ExamAssignQuestionsRequest req = assignRequest(item(100L, "vocabulary", 1, "180"));

        assertThrows(ExamBusinessException.class, () -> service.assignQuestions(10L, req, STAFF_EMAIL));
    }

    // ── submitForReview ──────────────────────────────────────────────────────

    @Test
    void submitForReview_draftExamWithQuestions_transitionsToPending() {
        stubStaff();
        ExamAssessmentEntity existing = exam("draft", 1L);
        when(assessmentRepository.findByIdAndAssessmentTypeAndStatusNot(10L, "exam", "deleted"))
                .thenReturn(Optional.of(existing));
        when(assignmentRepository.countByParentTypeAndParentId("assessment", 10L))
                .thenReturn(5L);

        assertEquals("pending_review", service.submitForReview(10L, STAFF_EMAIL).getStatus());
        assertEquals("pending_review", existing.getStatus());
        verify(assessmentRepository).save(existing);
    }

    @Test
    void submitForReview_rejectedExam_isAllowed() {
        stubStaff();
        when(assessmentRepository.findByIdAndAssessmentTypeAndStatusNot(10L, "exam", "deleted"))
                .thenReturn(Optional.of(exam("rejected", 1L)));
        when(assignmentRepository.countByParentTypeAndParentId("assessment", 10L))
                .thenReturn(5L);

        assertEquals("exam", service.submitForReview(10L, STAFF_EMAIL).getContentType());
    }

    @Test
    void submitForReview_scoreMismatch_isStillAllowedAtThisStage() {
        stubStaff();
        ExamAssessmentEntity existing = exam("draft", 1L);
        existing.setTotalScore(999);
        when(assessmentRepository.findByIdAndAssessmentTypeAndStatusNot(10L, "exam", "deleted"))
                .thenReturn(Optional.of(existing));
        when(assignmentRepository.countByParentTypeAndParentId("assessment", 10L))
                .thenReturn(1L);

        assertDoesNotThrow(() -> service.submitForReview(10L, STAFF_EMAIL));
    }

    @ParameterizedTest
    @ValueSource(strings = {"pending_review", "published"})
    void submitForReview_nonEditableStatus_isRejected(String status) {
        stubStaff();
        when(assessmentRepository.findByIdAndAssessmentTypeAndStatusNot(10L, "exam", "deleted"))
                .thenReturn(Optional.of(exam(status, 1L)));
        when(assignmentRepository.countByParentTypeAndParentId("assessment", 10L))
                .thenReturn(5L);

        assertThrows(ExamBusinessException.class, () -> service.submitForReview(10L, STAFF_EMAIL));
    }

    @Test
    void submitForReview_ofAnotherStaff_throwsOwnershipDenied() {
        stubStaff();
        when(assessmentRepository.findByIdAndAssessmentTypeAndStatusNot(10L, "exam", "deleted"))
                .thenReturn(Optional.of(exam("draft", 99L)));

        assertThrows(ExamBusinessException.class, () -> service.submitForReview(10L, STAFF_EMAIL));
    }

    private static <T> T argThat(org.mockito.ArgumentMatcher<T> matcher) {
        return org.mockito.ArgumentMatchers.argThat(matcher);
    }
}
