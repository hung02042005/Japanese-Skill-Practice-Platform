/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.staffcontent.quiz.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.jlpt.feature.speaking.dto.SpeakingLessonMutationResponse;
import com.jlpt.feature.speaking.service.SpeakingAuthoringService;
import com.jlpt.feature.staffcontent.exam.dto.ExamSubmitReviewResponse;
import com.jlpt.feature.staffcontent.exam.service.StaffExamService;
import com.jlpt.feature.staffcontent.grammar.dto.GrammarSubmitReviewResponse;
import com.jlpt.feature.staffcontent.grammar.service.StaffGrammarService;
import com.jlpt.feature.staffcontent.learning.dto.SubmitReviewRequest;
import com.jlpt.feature.staffcontent.learning.dto.SubmitReviewResponse;
import com.jlpt.feature.staffcontent.learning.service.LearningContentService;
import com.jlpt.feature.staffcontent.quiz.dto.QuizSubmitReviewResponse;
import com.jlpt.feature.staffcontent.quiz.service.StaffQuizService;
import com.jlpt.shared.common.ApiResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

/**
 * Endpoint gửi duyệt dùng chung (UC-26/27/28): mỗi contentType phải được định tuyến sang đúng
 * service và map về cùng một khuôn phản hồi; contentType lạ trả 400 thay vì 500.
 */
@ExtendWith(MockitoExtension.class)
class StaffQuizSubmitReviewControllerTest {

    private static final String STAFF_EMAIL = "staff@sakuji.com";

    @Mock
    private StaffQuizService staffQuizService;

    @Mock
    private StaffExamService staffExamService;

    @Mock
    private LearningContentService learningContentService;

    @Mock
    private StaffGrammarService staffGrammarService;

    @Mock
    private SpeakingAuthoringService speakingAuthoringService;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private StaffQuizSubmitReviewController controller;

    @BeforeEach
    void setUp() {
        lenient().when(authentication.getName()).thenReturn(STAFF_EMAIL);
    }

    private StaffQuizSubmitReviewController.QuizSubmitReviewRequest request(String contentType, Long contentId) {
        StaffQuizSubmitReviewController.QuizSubmitReviewRequest req =
                new StaffQuizSubmitReviewController.QuizSubmitReviewRequest();
        req.setContentType(contentType);
        req.setContentId(contentId);
        return req;
    }

    @Test
    void assessment_routesToQuizService() {
        when(staffQuizService.submitForReview(5L, STAFF_EMAIL))
                .thenReturn(QuizSubmitReviewResponse.builder()
                        .contentId(5L)
                        .contentType("assessment")
                        .status("pending_review")
                        .build());

        ResponseEntity<ApiResponse<QuizSubmitReviewResponse>> response =
                controller.submitReview(request("assessment", 5L), authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("assessment", response.getBody().getData().getContentType());
        assertEquals("pending_review", response.getBody().getData().getStatus());
        verifyNoInteractions(staffExamService, learningContentService, staffGrammarService, speakingAuthoringService);
    }

    @Test
    void assessment_contentTypeIsCaseInsensitive() {
        when(staffQuizService.submitForReview(5L, STAFF_EMAIL))
                .thenReturn(QuizSubmitReviewResponse.builder()
                        .contentId(5L)
                        .contentType("assessment")
                        .status("pending_review")
                        .build());

        assertEquals(
                HttpStatus.OK,
                controller
                        .submitReview(request("ASSESSMENT", 5L), authentication)
                        .getStatusCode());
    }

    @Test
    void exam_routesToExamServiceAndMapsResponse() {
        when(staffExamService.submitForReview(6L, STAFF_EMAIL))
                .thenReturn(ExamSubmitReviewResponse.builder()
                        .contentId(6L)
                        .contentType("exam")
                        .status("pending_review")
                        .build());

        ResponseEntity<ApiResponse<QuizSubmitReviewResponse>> response =
                controller.submitReview(request("exam", 6L), authentication);

        assertEquals(6L, response.getBody().getData().getContentId());
        assertEquals("exam", response.getBody().getData().getContentType());
    }

    @Test
    void grammar_routesToGrammarServiceAndMapsResponse() {
        when(staffGrammarService.submitForReview(7L, STAFF_EMAIL))
                .thenReturn(GrammarSubmitReviewResponse.builder()
                        .contentId(7L)
                        .contentType("grammar")
                        .status("pending_review")
                        .build());

        ResponseEntity<ApiResponse<QuizSubmitReviewResponse>> response =
                controller.submitReview(request("grammar", 7L), authentication);

        assertEquals(7L, response.getBody().getData().getContentId());
        assertEquals("grammar", response.getBody().getData().getContentType());
    }

    @Test
    void speaking_routesToSpeakingServiceAndLabelsContentTypeSpeaking() {
        when(speakingAuthoringService.submitForReview(8L, STAFF_EMAIL))
                .thenReturn(SpeakingLessonMutationResponse.builder()
                        .lessonId(8L)
                        .status("pending_review")
                        .build());

        ResponseEntity<ApiResponse<QuizSubmitReviewResponse>> response =
                controller.submitReview(request("speaking", 8L), authentication);

        assertEquals(8L, response.getBody().getData().getContentId());
        assertEquals("speaking", response.getBody().getData().getContentType());
        assertEquals("pending_review", response.getBody().getData().getStatus());
    }

    @Test
    void lesson_routesToLearningContentServiceWithLowercasedType() {
        when(learningContentService.submitForReview(any(SubmitReviewRequest.class), eq(STAFF_EMAIL)))
                .thenReturn(SubmitReviewResponse.builder()
                        .contentId(9L)
                        .contentType("lesson")
                        .status("pending_review")
                        .build());

        ResponseEntity<ApiResponse<QuizSubmitReviewResponse>> response =
                controller.submitReview(request("LESSON", 9L), authentication);

        ArgumentCaptor<SubmitReviewRequest> captor = ArgumentCaptor.forClass(SubmitReviewRequest.class);
        verify(learningContentService).submitForReview(captor.capture(), eq(STAFF_EMAIL));
        assertEquals("lesson", captor.getValue().getContentType());
        assertEquals(9L, captor.getValue().getContentId());
        assertEquals("lesson", response.getBody().getData().getContentType());
    }

    @Test
    void vocabulary_routesToLearningContentService() {
        when(learningContentService.submitForReview(any(SubmitReviewRequest.class), eq(STAFF_EMAIL)))
                .thenReturn(SubmitReviewResponse.builder()
                        .contentId(10L)
                        .contentType("vocabulary")
                        .status("pending_review")
                        .build());

        assertEquals(
                "vocabulary",
                controller
                        .submitReview(request("vocabulary", 10L), authentication)
                        .getBody()
                        .getData()
                        .getContentType());
    }

    @Test
    void kanji_routesToLearningContentService() {
        when(learningContentService.submitForReview(any(SubmitReviewRequest.class), eq(STAFF_EMAIL)))
                .thenReturn(SubmitReviewResponse.builder()
                        .contentId(11L)
                        .contentType("kanji")
                        .status("pending_review")
                        .build());

        assertEquals(
                "kanji",
                controller
                        .submitReview(request("kanji", 11L), authentication)
                        .getBody()
                        .getData()
                        .getContentType());
    }

    @Test
    void nullContentType_returns400() {
        ResponseEntity<ApiResponse<QuizSubmitReviewResponse>> response =
                controller.submitReview(request(null, 1L), authentication);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(400, response.getBody().getStatus());
        assertEquals("contentType là bắt buộc", response.getBody().getMessage());
    }

    @Test
    void unsupportedContentType_returns400ListingSupportedValues() {
        ResponseEntity<ApiResponse<QuizSubmitReviewResponse>> response =
                controller.submitReview(request("podcast", 1L), authentication);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(400, response.getBody().getStatus());
        assertTrue(response.getBody().getMessage().contains("assessment"));
        assertTrue(response.getBody().getMessage().contains("kanji"));
        verifyNoInteractions(
                staffQuizService,
                staffExamService,
                learningContentService,
                staffGrammarService,
                speakingAuthoringService);
    }

    private static <T> T any(Class<T> type) {
        return org.mockito.ArgumentMatchers.any(type);
    }

    private static String eq(String value) {
        return org.mockito.ArgumentMatchers.eq(value);
    }
}
