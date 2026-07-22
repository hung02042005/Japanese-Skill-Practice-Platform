/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.staffcontent.quiz.controller;

import com.jlpt.feature.speaking.dto.SpeakingLessonMutationResponse;
import com.jlpt.feature.speaking.service.SpeakingAuthoringService;
import com.jlpt.feature.staffcontent.exam.service.StaffExamService;
import com.jlpt.feature.staffcontent.grammar.dto.GrammarSubmitReviewResponse;
import com.jlpt.feature.staffcontent.grammar.service.StaffGrammarService;
import com.jlpt.feature.staffcontent.learning.dto.SubmitReviewRequest;
import com.jlpt.feature.staffcontent.learning.dto.SubmitReviewResponse;
import com.jlpt.feature.staffcontent.learning.service.LearningContentService;
import com.jlpt.feature.staffcontent.quiz.dto.QuizSubmitReviewResponse;
import com.jlpt.feature.staffcontent.quiz.service.StaffQuizService;
import com.jlpt.shared.common.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * UC-26 / UC-28 / UC-27 — Central submit-review endpoint.
 * Fulfills {@code POST /api/staff/contents/submit-review} with {@code contentType}:
 * {@code assessment} (quiz, UC-26), {@code exam} (UC-28),
 * or {@code lesson} / {@code vocabulary} / {@code kanji} (learning content, UC-27).
 */
@RestController
@RequestMapping("/api/staff/contents")
@RequiredArgsConstructor
@PreAuthorize("hasRole('STAFF')")
public class StaffQuizSubmitReviewController {

    private final StaffQuizService staffQuizService;
    private final StaffExamService staffExamService;
    private final LearningContentService learningContentService;
    private final StaffGrammarService staffGrammarService;
    private final SpeakingAuthoringService speakingAuthoringService;

    @Data
    public static class QuizSubmitReviewRequest {
        @NotNull(message = "Thiếu contentType") private String contentType;

        @NotNull(message = "Thiếu contentId") private Long contentId;
    }

    @PostMapping("/submit-review")
    public ResponseEntity<ApiResponse<QuizSubmitReviewResponse>> submitReview(
            @Valid @RequestBody QuizSubmitReviewRequest request, Authentication authentication) {
        String contentType = request.getContentType();
        if (contentType == null) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.<QuizSubmitReviewResponse>builder()
                            .status(400)
                            .message("contentType là bắt buộc")
                            .build());
        }
        if ("assessment".equalsIgnoreCase(contentType)) {
            QuizSubmitReviewResponse data =
                    staffQuizService.submitForReview(request.getContentId(), authentication.getName());
            return ResponseEntity.ok(ApiResponse.success("Đã gửi bài trắc nghiệm để phê duyệt", data));
        }
        if ("exam".equalsIgnoreCase(contentType)) {
            com.jlpt.feature.staffcontent.exam.dto.ExamSubmitReviewResponse data =
                    staffExamService.submitForReview(request.getContentId(), authentication.getName());
            QuizSubmitReviewResponse mapped = QuizSubmitReviewResponse.builder()
                    .contentId(data.getContentId())
                    .contentType(data.getContentType())
                    .status(data.getStatus())
                    .build();
            return ResponseEntity.ok(ApiResponse.success("Đã gửi đề thi thử để phê duyệt", mapped));
        }
        if ("grammar".equalsIgnoreCase(contentType)) {
            GrammarSubmitReviewResponse grammarData =
                    staffGrammarService.submitForReview(request.getContentId(), authentication.getName());
            QuizSubmitReviewResponse mapped = QuizSubmitReviewResponse.builder()
                    .contentId(grammarData.getContentId())
                    .contentType(grammarData.getContentType())
                    .status(grammarData.getStatus())
                    .build();
            return ResponseEntity.ok(ApiResponse.success("Đã gửi duyệt ngữ pháp thành công", mapped));
        }
        if ("speaking".equalsIgnoreCase(contentType)) {
            SpeakingLessonMutationResponse speakingData =
                    speakingAuthoringService.submitForReview(request.getContentId(), authentication.getName());
            QuizSubmitReviewResponse mapped = QuizSubmitReviewResponse.builder()
                    .contentId(speakingData.getLessonId())
                    .contentType("speaking")
                    .status(speakingData.getStatus())
                    .build();
            return ResponseEntity.ok(ApiResponse.success("Đã gửi bài nói đi duyệt", mapped));
        }
        if ("lesson".equalsIgnoreCase(contentType)
                || "vocabulary".equalsIgnoreCase(contentType)
                || "kanji".equalsIgnoreCase(contentType)) {
            SubmitReviewRequest lcRequest = new SubmitReviewRequest();
            lcRequest.setContentType(contentType.toLowerCase());
            lcRequest.setContentId(request.getContentId());
            SubmitReviewResponse data = learningContentService.submitForReview(lcRequest, authentication.getName());
            QuizSubmitReviewResponse mapped = QuizSubmitReviewResponse.builder()
                    .contentId(data.getContentId())
                    .contentType(data.getContentType())
                    .status(data.getStatus())
                    .build();
            return ResponseEntity.ok(ApiResponse.success("Đã gửi nội dung để phê duyệt", mapped));
        }
        return ResponseEntity.badRequest()
                .body(ApiResponse.<QuizSubmitReviewResponse>builder()
                        .status(400)
                        .message(
                                "contentType không hợp lệ. Hỗ trợ: assessment, exam, grammar, lesson, speaking, vocabulary, kanji")
                        .build());
    }
}
