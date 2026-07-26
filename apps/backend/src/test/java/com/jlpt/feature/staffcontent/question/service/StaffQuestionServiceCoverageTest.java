/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.staffcontent.question.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.*;

import com.jlpt.feature.staff.StaffUser;
import com.jlpt.feature.staff.StaffUserRepository;
import com.jlpt.feature.staffcontent.question.dto.CreateQuestionRequest;
import com.jlpt.feature.staffcontent.question.dto.QuestionResponse;
import com.jlpt.feature.staffcontent.question.dto.UpdateQuestionRequest;
import com.jlpt.feature.staffcontent.question.entity.StaffContentQuestionEntity;
import com.jlpt.feature.staffcontent.question.exception.StaffQuestionBusinessException;
import com.jlpt.feature.staffcontent.question.repository.StaffContentAttemptAnswerRepository;
import com.jlpt.feature.staffcontent.question.repository.StaffContentQuestionRepository;
import com.jlpt.shared.exception.ForbiddenException;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

/**
 * UC-24 — ngân hàng câu hỏi. Tập trung phủ NHÁNH: validateQuestionFields (3 loại câu hỏi, mỗi loại
 * nhiều điều kiện OR), applyUpdate (giữ / ghi đè / xoá về null), guardOwnership, khoá câu hỏi khi
 * đã có lượt làm bài (FR-24-17, LESSON-005) và chặn sửa khi không ở draft/rejected (FR-24-18).
 */
@ExtendWith(MockitoExtension.class)
class StaffQuestionServiceCoverageTest {

    private static final String STAFF_EMAIL = "staff@sakuji.com";
    private static final String MANAGER_EMAIL = "manager@sakuji.com";

    @Mock
    private StaffContentQuestionRepository questionRepository;

    @Mock
    private StaffContentAttemptAnswerRepository attemptAnswerRepository;

    @Mock
    private StaffUserRepository staffUserRepository;

    @InjectMocks
    private StaffQuestionServiceImpl service;

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

    private CreateQuestionRequest multipleChoiceRequest() {
        CreateQuestionRequest req = new CreateQuestionRequest();
        req.setQuestionText("  これは何ですか  ");
        req.setQuestionType("multiple_choice");
        req.setSkill("vocabulary");
        req.setJlptLevel("N5");
        req.setOptionA("A");
        req.setOptionB("B");
        req.setOptionC("C");
        req.setOptionD("D");
        req.setCorrectOption("A");
        return req;
    }

    private StaffContentQuestionEntity entity(String status, Long owner) {
        return StaffContentQuestionEntity.builder()
                .id(10L)
                .questionText("これは何ですか")
                .questionType("multiple_choice")
                .skill("vocabulary")
                .jlptLevel("N5")
                .optionA("A")
                .optionB("B")
                .optionC("C")
                .optionD("D")
                .correctOption("A")
                .status(status)
                .createdBy(owner)
                .build();
    }

    // ── validateQuestionFields: multiple_choice ──────────────────────────────

    /** Mỗi lần thiếu đúng MỘT option → phủ từng vế của điều kiện OR. */
    @ParameterizedTest
    @ValueSource(strings = {"optionA", "optionB", "optionC", "optionD", "correctOption"})
    void createQuestion_multipleChoiceMissingOneField_throwsMissingOptions(String missingField) {
        stubStaff();
        CreateQuestionRequest req = multipleChoiceRequest();
        switch (missingField) {
            case "optionA" -> req.setOptionA("  ");
            case "optionB" -> req.setOptionB(null);
            case "optionC" -> req.setOptionC("");
            case "optionD" -> req.setOptionD(null);
            default -> req.setCorrectOption("  ");
        }

        assertThrows(StaffQuestionBusinessException.class, () -> service.createQuestion(req, STAFF_EMAIL));
        verify(questionRepository, never()).save(any());
    }

    @ParameterizedTest
    @ValueSource(strings = {"E", "a", "AB", "1"})
    void createQuestion_correctOptionNotSingleUppercaseLetter_throwsInvalidCorrectOption(String value) {
        stubStaff();
        CreateQuestionRequest req = multipleChoiceRequest();
        req.setCorrectOption(value);

        assertThrows(StaffQuestionBusinessException.class, () -> service.createQuestion(req, STAFF_EMAIL));
    }

    @ParameterizedTest
    @ValueSource(strings = {"A", "B", "C", "D"})
    void createQuestion_everyValidCorrectOption_isAccepted(String value) {
        stubStaff();
        when(questionRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        CreateQuestionRequest req = multipleChoiceRequest();
        req.setCorrectOption(value);

        assertEquals(value, service.createQuestion(req, STAFF_EMAIL).getCorrectOption());
    }

    // ── validateQuestionFields: true_false ───────────────────────────────────

    @ParameterizedTest
    @CsvSource({"true", "false", "TRUE", "False", "  true  "})
    void createQuestion_trueFalseWithValidAnswer_isAccepted(String answer) {
        stubStaff();
        when(questionRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        CreateQuestionRequest req = multipleChoiceRequest();
        req.setQuestionType("true_false");
        req.setCorrectAnswerText(answer);

        assertNotNull(service.createQuestion(req, STAFF_EMAIL));
    }

    @ParameterizedTest
    @ValueSource(strings = {"yes", "1", "đúng"})
    void createQuestion_trueFalseWithInvalidAnswer_throwsMissingOptions(String answer) {
        stubStaff();
        CreateQuestionRequest req = multipleChoiceRequest();
        req.setQuestionType("true_false");
        req.setCorrectAnswerText(answer);

        assertThrows(StaffQuestionBusinessException.class, () -> service.createQuestion(req, STAFF_EMAIL));
    }

    @Test
    void createQuestion_trueFalseWithoutAnswer_throwsMissingOptions() {
        stubStaff();
        CreateQuestionRequest req = multipleChoiceRequest();
        req.setQuestionType("true_false");
        req.setCorrectAnswerText("   ");

        assertThrows(StaffQuestionBusinessException.class, () -> service.createQuestion(req, STAFF_EMAIL));
    }

    // ── validateQuestionFields: fill_blank & loại khác ───────────────────────

    @Test
    void createQuestion_fillBlankWithAnswer_isAccepted() {
        stubStaff();
        when(questionRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        CreateQuestionRequest req = multipleChoiceRequest();
        req.setQuestionType("fill_blank");
        req.setCorrectAnswerText("こんにちは");

        assertEquals("こんにちは", service.createQuestion(req, STAFF_EMAIL).getCorrectAnswerText());
    }

    @Test
    void createQuestion_fillBlankWithoutAnswer_throwsMissingOptions() {
        stubStaff();
        CreateQuestionRequest req = multipleChoiceRequest();
        req.setQuestionType("fill_blank");
        req.setCorrectAnswerText(null);

        assertThrows(StaffQuestionBusinessException.class, () -> service.createQuestion(req, STAFF_EMAIL));
    }

    /** Loại không nằm trong 3 nhánh đã biết → bỏ qua validate, vẫn tạo được. */
    @Test
    void createQuestion_unknownQuestionType_skipsTypeValidation() {
        stubStaff();
        when(questionRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        CreateQuestionRequest req = multipleChoiceRequest();
        req.setQuestionType("essay");
        req.setOptionA(null);
        req.setCorrectOption(null);

        assertNotNull(service.createQuestion(req, STAFF_EMAIL));
    }

    // ── createQuestion: trim & mặc định ──────────────────────────────────────

    @Test
    void createQuestion_trimsTextAndNullsOutBlankOptionalFields() {
        stubStaff();
        when(questionRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        CreateQuestionRequest req = multipleChoiceRequest();
        req.setExplanation("   ");
        req.setAudioUrl("  https://cdn/a.mp3  ");
        req.setImageUrl(null);

        QuestionResponse response = service.createQuestion(req, STAFF_EMAIL);

        assertEquals("これは何ですか", response.getQuestionText());
        assertNull(response.getExplanation());
        assertEquals("https://cdn/a.mp3", response.getAudioUrl());
        assertNull(response.getImageUrl());
        assertEquals("draft", response.getStatus());
        assertEquals(1L, response.getCreatedBy());
        assertFalse(response.getIsLocked());
    }

    @Test
    void createQuestion_unknownStaff_throwsForbidden() {
        when(staffUserRepository.findByEmail("ghost@sakuji.com")).thenReturn(Optional.empty());
        CreateQuestionRequest req = multipleChoiceRequest();

        assertThrows(ForbiddenException.class, () -> service.createQuestion(req, "ghost@sakuji.com"));
    }

    // ── listQuestions ────────────────────────────────────────────────────────

    @Test
    void listQuestions_trimsFiltersAndMarksLockedQuestions() {
        when(questionRepository.findFiltered(
                        eq("nan"), eq("grammar"), eq("N5"), eq("multiple_choice"), eq("draft"), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(entity("draft", 1L))));
        when(attemptAnswerRepository.existsByQuestionId(10L)).thenReturn(true);

        QuestionResponse row = service.listQuestions(
                        "  nan  ", "  grammar  ", "  N5  ", "  multiple_choice  ", "draft", PageRequest.of(0, 10))
                .getContent()
                .get(0);

        assertTrue(row.getIsLocked());
    }

    @Test
    void listQuestions_blankFiltersBecomeNull() {
        when(questionRepository.findFiltered(isNull(), isNull(), isNull(), isNull(), isNull(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(entity("draft", 1L))));
        when(attemptAnswerRepository.existsByQuestionId(10L)).thenReturn(false);

        QuestionResponse row = service.listQuestions("  ", "", null, "  ", "   ", PageRequest.of(0, 10))
                .getContent()
                .get(0);

        assertFalse(row.getIsLocked());
    }

    // ── getQuestion ──────────────────────────────────────────────────────────

    @Test
    void getQuestion_existing_returnsWithLockFlag() {
        when(questionRepository.findByIdAndStatusNot(10L, "deleted")).thenReturn(Optional.of(entity("draft", 1L)));
        when(attemptAnswerRepository.existsByQuestionId(10L)).thenReturn(true);

        assertTrue(service.getQuestion(10L).getIsLocked());
    }

    @Test
    void getQuestion_deletedOrMissing_throwsNotFound() {
        when(questionRepository.findByIdAndStatusNot(404L, "deleted")).thenReturn(Optional.empty());

        assertThrows(StaffQuestionBusinessException.class, () -> service.getQuestion(404L));
    }

    // ── updateQuestion: applyUpdate 3 nhánh ──────────────────────────────────

    @Test
    void updateQuestion_nullField_keepsOldValue() {
        stubStaff();
        StaffContentQuestionEntity existing = entity("draft", 1L);
        existing.setExplanation("giải thích cũ");
        when(questionRepository.findByIdAndStatusNot(10L, "deleted")).thenReturn(Optional.of(existing));
        when(attemptAnswerRepository.existsByQuestionId(10L)).thenReturn(false);
        when(questionRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        QuestionResponse response = service.updateQuestion(10L, new UpdateQuestionRequest(), STAFF_EMAIL);

        assertEquals("giải thích cũ", response.getExplanation());
        assertEquals("これは何ですか", response.getQuestionText());
    }

    @Test
    void updateQuestion_blankField_clearsValueToNull() {
        stubStaff();
        StaffContentQuestionEntity existing = entity("draft", 1L);
        existing.setExplanation("giải thích cũ");
        when(questionRepository.findByIdAndStatusNot(10L, "deleted")).thenReturn(Optional.of(existing));
        when(attemptAnswerRepository.existsByQuestionId(10L)).thenReturn(false);
        when(questionRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        UpdateQuestionRequest req = new UpdateQuestionRequest();
        req.setExplanation("   ");

        assertNull(service.updateQuestion(10L, req, STAFF_EMAIL).getExplanation());
    }

    @Test
    void updateQuestion_newValue_isTrimmedAndReplaces() {
        stubStaff();
        when(questionRepository.findByIdAndStatusNot(10L, "deleted")).thenReturn(Optional.of(entity("draft", 1L)));
        when(attemptAnswerRepository.existsByQuestionId(10L)).thenReturn(false);
        when(questionRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        UpdateQuestionRequest req = new UpdateQuestionRequest();
        req.setQuestionText("  câu hỏi mới  ");
        req.setSkill("  grammar  ");
        req.setJlptLevel("N4");
        req.setExplanation("  giải thích mới  ");
        req.setImageUrl("  https://cdn/i.png  ");

        QuestionResponse response = service.updateQuestion(10L, req, STAFF_EMAIL);

        assertEquals("câu hỏi mới", response.getQuestionText());
        assertEquals("  grammar  ", response.getSkill());
        assertEquals("N4", response.getJlptLevel());
        assertEquals("giải thích mới", response.getExplanation());
        assertEquals("https://cdn/i.png", response.getImageUrl());
    }

    @Test
    void updateQuestion_changingTypeRevalidatesAgainstNewType() {
        stubStaff();
        StaffContentQuestionEntity existing = entity("draft", 1L);
        when(questionRepository.findByIdAndStatusNot(10L, "deleted")).thenReturn(Optional.of(existing));
        when(attemptAnswerRepository.existsByQuestionId(10L)).thenReturn(false);
        UpdateQuestionRequest req = new UpdateQuestionRequest();
        req.setQuestionType("fill_blank"); // chưa có correctAnswerText → phải chặn

        assertThrows(StaffQuestionBusinessException.class, () -> service.updateQuestion(10L, req, STAFF_EMAIL));
        verify(questionRepository, never()).save(any());
    }

    @Test
    void updateQuestion_lockedByExistingAttempts_isRejected() {
        stubStaff();
        when(questionRepository.findByIdAndStatusNot(10L, "deleted")).thenReturn(Optional.of(entity("draft", 1L)));
        when(attemptAnswerRepository.existsByQuestionId(10L)).thenReturn(true);
        UpdateQuestionRequest req = new UpdateQuestionRequest();

        assertThrows(StaffQuestionBusinessException.class, () -> service.updateQuestion(10L, req, STAFF_EMAIL));
        verify(questionRepository, never()).save(any());
    }

    @ParameterizedTest
    @ValueSource(strings = {"pending_review", "published", "archived"})
    void updateQuestion_statusNotEditable_isRejected(String status) {
        stubStaff();
        when(questionRepository.findByIdAndStatusNot(10L, "deleted")).thenReturn(Optional.of(entity(status, 1L)));
        when(attemptAnswerRepository.existsByQuestionId(10L)).thenReturn(false);
        UpdateQuestionRequest req = new UpdateQuestionRequest();

        assertThrows(StaffQuestionBusinessException.class, () -> service.updateQuestion(10L, req, STAFF_EMAIL));
    }

    @Test
    void updateQuestion_rejectedStatus_isEditable() {
        stubStaff();
        when(questionRepository.findByIdAndStatusNot(10L, "deleted")).thenReturn(Optional.of(entity("rejected", 1L)));
        when(attemptAnswerRepository.existsByQuestionId(10L)).thenReturn(false);
        when(questionRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        assertEquals(
                "rejected",
                service.updateQuestion(10L, new UpdateQuestionRequest(), STAFF_EMAIL)
                        .getStatus());
    }

    // ── guardOwnership ───────────────────────────────────────────────────────

    @Test
    void updateQuestion_ofAnotherStaff_throwsForbidden() {
        stubStaff();
        when(questionRepository.findByIdAndStatusNot(10L, "deleted")).thenReturn(Optional.of(entity("draft", 77L)));
        UpdateQuestionRequest req = new UpdateQuestionRequest();

        assertThrows(StaffQuestionBusinessException.class, () -> service.updateQuestion(10L, req, STAFF_EMAIL));
    }

    @Test
    void updateQuestion_withoutOwner_throwsForbidden() {
        stubStaff();
        when(questionRepository.findByIdAndStatusNot(10L, "deleted")).thenReturn(Optional.of(entity("draft", null)));
        UpdateQuestionRequest req = new UpdateQuestionRequest();

        assertThrows(StaffQuestionBusinessException.class, () -> service.updateQuestion(10L, req, STAFF_EMAIL));
    }

    @Test
    void updateQuestion_asManagerOnOtherStaffContent_isAllowed() {
        when(staffUserRepository.findByEmail(MANAGER_EMAIL)).thenReturn(Optional.of(manager));
        when(questionRepository.findByIdAndStatusNot(10L, "deleted")).thenReturn(Optional.of(entity("draft", 77L)));
        when(attemptAnswerRepository.existsByQuestionId(10L)).thenReturn(false);
        when(questionRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        assertNotNull(service.updateQuestion(10L, new UpdateQuestionRequest(), MANAGER_EMAIL));
    }

    // ── submitForReview ──────────────────────────────────────────────────────

    @Test
    void submitForReview_draftQuestion_transitionsToPendingReview() {
        stubStaff();
        StaffContentQuestionEntity existing = entity("draft", 1L);
        when(questionRepository.findByIdAndStatusNot(10L, "deleted")).thenReturn(Optional.of(existing));

        assertEquals("pending_review", service.submitForReview(10L, STAFF_EMAIL).getStatus());
        assertEquals("pending_review", existing.getStatus());
        verify(questionRepository).save(existing);
    }

    @Test
    void submitForReview_rejectedQuestion_isAllowed() {
        stubStaff();
        when(questionRepository.findByIdAndStatusNot(10L, "deleted")).thenReturn(Optional.of(entity("rejected", 1L)));

        assertEquals("question", service.submitForReview(10L, STAFF_EMAIL).getContentType());
    }

    @ParameterizedTest
    @ValueSource(strings = {"pending_review", "published"})
    void submitForReview_statusNotSubmittable_isRejected(String status) {
        stubStaff();
        when(questionRepository.findByIdAndStatusNot(10L, "deleted")).thenReturn(Optional.of(entity(status, 1L)));

        assertThrows(StaffQuestionBusinessException.class, () -> service.submitForReview(10L, STAFF_EMAIL));
    }

    @Test
    void submitForReview_incompleteQuestion_isRejected() {
        stubStaff();
        StaffContentQuestionEntity existing = entity("draft", 1L);
        existing.setOptionC(null);
        when(questionRepository.findByIdAndStatusNot(10L, "deleted")).thenReturn(Optional.of(existing));

        assertThrows(StaffQuestionBusinessException.class, () -> service.submitForReview(10L, STAFF_EMAIL));
        verify(questionRepository, never()).save(any());
    }

    @Test
    void submitForReview_ofAnotherStaff_throwsForbidden() {
        stubStaff();
        when(questionRepository.findByIdAndStatusNot(10L, "deleted")).thenReturn(Optional.of(entity("draft", 77L)));

        assertThrows(StaffQuestionBusinessException.class, () -> service.submitForReview(10L, STAFF_EMAIL));
    }

    @Test
    void submitForReview_missing_throwsNotFound() {
        stubStaff();
        when(questionRepository.findByIdAndStatusNot(404L, "deleted")).thenReturn(Optional.empty());

        assertThrows(StaffQuestionBusinessException.class, () -> service.submitForReview(404L, STAFF_EMAIL));
    }
}
