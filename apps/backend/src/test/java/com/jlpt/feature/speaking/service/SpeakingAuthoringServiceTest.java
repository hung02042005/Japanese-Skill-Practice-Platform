/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.speaking.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.jlpt.feature.learning.Lesson;
import com.jlpt.feature.learning.LessonRepository;
import com.jlpt.feature.speaking.dto.SpeakingLessonCreateRequest;
import com.jlpt.feature.speaking.dto.SpeakingQuestionDto;
import com.jlpt.feature.speaking.entity.SpeakingQuestion;
import com.jlpt.feature.speaking.exception.SpeakingBusinessException;
import com.jlpt.feature.speaking.repository.SpeakingQuestionRepository;
import com.jlpt.feature.staff.StaffUser;
import com.jlpt.feature.staff.StaffUserRepository;
import com.jlpt.feature.student.StudentUser;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SpeakingAuthoringServiceTest {

    @Mock
    private LessonRepository lessonRepository;

    @Mock
    private SpeakingQuestionRepository questionRepository;

    @Mock
    private StaffUserRepository staffUserRepository;

    private SpeakingAuthoringService service;
    private StaffUser staff;

    @BeforeEach
    void setUp() {
        service = new SpeakingAuthoringService(lessonRepository, questionRepository, staffUserRepository);
        staff = StaffUser.builder()
                .id(7L)
                .email("staff@example.com")
                .status(StaffUser.StaffStatus.ACTIVE)
                .staffRole(StaffUser.StaffRole.STAFF)
                .build();
        lenient().when(staffUserRepository.findByEmail(staff.getEmail())).thenReturn(Optional.of(staff));
    }

    @Test
    void createForcesSpeakingDraftAndPersistsQuestionsInDisplayOrder() {
        SpeakingLessonCreateRequest request = request(List.of(question("Câu hai", 2), question("Câu một", 1)));
        when(lessonRepository.save(any(Lesson.class))).thenAnswer(invocation -> {
            Lesson lesson = invocation.getArgument(0);
            lesson.setId(42L);
            return lesson;
        });

        var response = service.create(request, staff.getEmail());

        ArgumentCaptor<Lesson> lessonCaptor = ArgumentCaptor.forClass(Lesson.class);
        verify(lessonRepository).save(lessonCaptor.capture());
        Lesson savedLesson = lessonCaptor.getValue();
        assertEquals(Lesson.LessonType.SPEAKING, savedLesson.getLessonType());
        assertEquals(Lesson.LessonStatus.DRAFT, savedLesson.getStatus());
        assertEquals(staff, savedLesson.getCreatedBy());
        assertEquals("Câu một", savedLesson.getContentText());
        assertEquals(42L, response.getLessonId());
        assertEquals("draft", response.getStatus());

        ArgumentCaptor<SpeakingQuestion> questionCaptor = ArgumentCaptor.forClass(SpeakingQuestion.class);
        verify(questionRepository, times(2)).save(questionCaptor.capture());
        assertEquals(
                List.of("Câu một", "Câu hai"),
                questionCaptor.getAllValues().stream()
                        .map(SpeakingQuestion::getPromptText)
                        .toList());
    }

    @Test
    void createRejectsEmptyQuestionList() {
        SpeakingLessonCreateRequest request = request(List.of());

        SpeakingBusinessException exception =
                assertThrows(SpeakingBusinessException.class, () -> service.create(request, staff.getEmail()));

        assertEquals(400, exception.getStatus());
        assertEquals("VALIDATION_FAILED", exception.getErrorCode());
        verifyNoInteractions(lessonRepository, questionRepository);
    }

    @Test
    void updateRejectsPendingReview() {
        Lesson lesson = ownedLesson(Lesson.LessonStatus.PENDING_REVIEW);
        when(lessonRepository.findByIdAndStatusNot(42L, Lesson.LessonStatus.DELETED))
                .thenReturn(Optional.of(lesson));

        SpeakingBusinessException exception = assertThrows(
                SpeakingBusinessException.class,
                () -> service.update(42L, request(List.of(question("新しい文", 1))), staff.getEmail()));

        assertEquals(409, exception.getStatus());
        assertEquals("INVALID_STATE_TRANSITION", exception.getErrorCode());
        verify(questionRepository, never()).deleteByLesson_Id(any());
    }

    @Test
    void updateAtomicallyReplacesQuestions() {
        Lesson lesson = ownedLesson(Lesson.LessonStatus.REJECTED);
        when(lessonRepository.findByIdAndStatusNot(42L, Lesson.LessonStatus.DELETED))
                .thenReturn(Optional.of(lesson));
        when(questionRepository.findByLesson_IdOrderByDisplayOrderAsc(42L))
                .thenReturn(List.of(SpeakingQuestion.builder()
                        .id(10L)
                        .lesson(lesson)
                        .promptText("更新済み")
                        .displayOrder(1)
                        .build()));

        var response = service.update(42L, request(List.of(question("更新済み", 1))), staff.getEmail());

        verify(questionRepository).deleteByLesson_Id(42L);
        verify(questionRepository).flush();
        verify(questionRepository).save(any(SpeakingQuestion.class));
        assertEquals("更新済み", lesson.getContentText());
        assertEquals("更新済み", response.getQuestions().get(0).getPromptText());
    }

    @Test
    void getOwnDetailReturnsLegacyContentAsOneQuestion() {
        Lesson lesson = ownedLesson(Lesson.LessonStatus.DRAFT);
        lesson.setContentText("はじめまして");
        when(lessonRepository.findByIdAndStatusNot(42L, Lesson.LessonStatus.DELETED))
                .thenReturn(Optional.of(lesson));
        when(questionRepository.findByLesson_IdOrderByDisplayOrderAsc(42L)).thenReturn(List.of());

        var response = service.getOwnDetail(42L, staff.getEmail());

        assertEquals(1, response.getQuestions().size());
        assertEquals("はじめまして", response.getQuestions().get(0).getPromptText());
    }

    private SpeakingLessonCreateRequest request(List<SpeakingQuestionDto> questions) {
        SpeakingLessonCreateRequest request = new SpeakingLessonCreateRequest();
        request.setJlptLevel("N5");
        request.setTitle("Luyện nói N5");
        request.setQuestions(questions);
        return request;
    }

    private SpeakingQuestionDto question(String prompt, int displayOrder) {
        return SpeakingQuestionDto.builder()
                .promptText(prompt)
                .displayOrder(displayOrder)
                .build();
    }

    private Lesson ownedLesson(Lesson.LessonStatus status) {
        return Lesson.builder()
                .id(42L)
                .title("Bài nói")
                .lessonType(Lesson.LessonType.SPEAKING)
                .jlptLevel(StudentUser.JlptLevel.N5)
                .status(status)
                .createdBy(staff)
                .build();
    }
}
