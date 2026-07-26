/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.staffcontent.question.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.jlpt.feature.staff.StaffUser;
import com.jlpt.feature.staff.StaffUserRepository;
import com.jlpt.feature.staffcontent.question.dto.CreateQuestionRequest;
import com.jlpt.feature.staffcontent.question.dto.QuestionResponse;
import com.jlpt.feature.staffcontent.question.dto.StaffQuestionSubmitReviewResponse;
import com.jlpt.feature.staffcontent.question.dto.UpdateQuestionRequest;
import com.jlpt.feature.staffcontent.question.entity.StaffContentQuestionEntity;
import com.jlpt.feature.staffcontent.question.exception.StaffQuestionBusinessException;
import com.jlpt.feature.staffcontent.question.repository.StaffContentAttemptAnswerRepository;
import com.jlpt.feature.staffcontent.question.repository.StaffContentQuestionRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit tests cho StaffQuestionServiceImpl.
 */
@ExtendWith(MockitoExtension.class)
class StaffQuestionServiceImplTest {

    @Mock
    private StaffContentQuestionRepository questionRepository;

    @Mock
    private StaffContentAttemptAnswerRepository attemptAnswerRepository;

    @Mock
    private StaffUserRepository staffUserRepository;

    @InjectMocks
    private StaffQuestionServiceImpl staffQuestionService;

    private StaffUser staff;

    @BeforeEach
    void setUp() {
        staff = StaffUser.builder()
                .id(1L)
                .email("staff@test.com")
                .staffRole(StaffUser.StaffRole.STAFF)
                .build();
    }

    // ── createQuestion ────────────────────────────────────────────────────────

    @Test
    void createQuestion_missingMultipleChoiceOptions_throwsException() {
        when(staffUserRepository.findByEmail("staff@test.com")).thenReturn(Optional.of(staff));

        CreateQuestionRequest req = new CreateQuestionRequest();
        req.setQuestionText("Test question?");
        req.setQuestionType("multiple_choice");
        req.setOptionA("A"); // Missing B, C, D, correctOption

        assertThrows(
                StaffQuestionBusinessException.class, () -> staffQuestionService.createQuestion(req, "staff@test.com"));
    }

    @Test
    void createQuestion_invalidCorrectOption_throwsException() {
        when(staffUserRepository.findByEmail("staff@test.com")).thenReturn(Optional.of(staff));

        CreateQuestionRequest req = new CreateQuestionRequest();
        req.setQuestionText("Test question?");
        req.setQuestionType("multiple_choice");
        req.setOptionA("A");
        req.setOptionB("B");
        req.setOptionC("C");
        req.setOptionD("D");
        req.setCorrectOption("Z"); // Invalid option label

        assertThrows(
                StaffQuestionBusinessException.class, () -> staffQuestionService.createQuestion(req, "staff@test.com"));
    }

    @Test
    void createQuestion_success_savesDraftQuestion() {
        when(staffUserRepository.findByEmail("staff@test.com")).thenReturn(Optional.of(staff));
        when(questionRepository.save(any())).thenAnswer(i -> {
            StaffContentQuestionEntity q = i.getArgument(0);
            q.setId(100L);
            return q;
        });

        CreateQuestionRequest req = new CreateQuestionRequest();
        req.setQuestionText("Test question?");
        req.setQuestionType("multiple_choice");
        req.setSkill("vocabulary");
        req.setJlptLevel("N5");
        req.setOptionA("A");
        req.setOptionB("B");
        req.setOptionC("C");
        req.setOptionD("D");
        req.setCorrectOption("A");

        QuestionResponse res = staffQuestionService.createQuestion(req, "staff@test.com");

        assertNotNull(res);
        assertEquals(100L, res.getQuestionId());
        assertEquals("draft", res.getStatus());
        verify(questionRepository).save(any(StaffContentQuestionEntity.class));
    }

    // ── updateQuestion ────────────────────────────────────────────────────────

    @Test
    void updateQuestion_questionLockedByAttempts_throwsException() {
        StaffContentQuestionEntity entity = StaffContentQuestionEntity.builder()
                .id(100L)
                .createdBy(1L)
                .status("draft")
                .build();

        when(staffUserRepository.findByEmail("staff@test.com")).thenReturn(Optional.of(staff));
        when(questionRepository.findByIdAndStatusNot(100L, "deleted")).thenReturn(Optional.of(entity));
        when(attemptAnswerRepository.existsByQuestionId(100L)).thenReturn(true);

        UpdateQuestionRequest req = new UpdateQuestionRequest();

        assertThrows(
                StaffQuestionBusinessException.class,
                () -> staffQuestionService.updateQuestion(100L, req, "staff@test.com"));
    }

    // ── submitForReview ───────────────────────────────────────────────────────

    @Test
    void submitForReview_success_transitionsToPendingReview() {
        StaffContentQuestionEntity entity = StaffContentQuestionEntity.builder()
                .id(100L)
                .createdBy(1L)
                .status("draft")
                .questionType("fill_blank")
                .correctAnswerText("answer")
                .build();

        when(staffUserRepository.findByEmail("staff@test.com")).thenReturn(Optional.of(staff));
        when(questionRepository.findByIdAndStatusNot(100L, "deleted")).thenReturn(Optional.of(entity));

        StaffQuestionSubmitReviewResponse res = staffQuestionService.submitForReview(100L, "staff@test.com");

        assertNotNull(res);
        assertEquals("pending_review", res.getStatus());
        assertEquals("pending_review", entity.getStatus());
        verify(questionRepository).save(entity);
    }
}
