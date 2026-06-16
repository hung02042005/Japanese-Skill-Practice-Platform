/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.staffcontent.quiz;

import com.jlpt.feature.staffcontent.quiz.dto.AssignQuestionsRequest;
import com.jlpt.feature.staffcontent.quiz.dto.AssignResultResponse;
import com.jlpt.feature.staffcontent.quiz.dto.CreateQuizRequest;
import com.jlpt.feature.staffcontent.quiz.dto.QuizDetailResponse;
import com.jlpt.feature.staffcontent.quiz.dto.QuizListResponse;
import com.jlpt.feature.staffcontent.quiz.dto.UpdateQuizRequest;
import com.jlpt.shared.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * UC-26 — Staff endpoints for managing quizzes ({@code assessment_type = 'quiz'}).
 * ADD-ONLY MODE: new controller; the base path {@code /api/staff/assessments} was previously unmapped.
 */
@RestController
@RequestMapping("/api/staff/assessments")
@RequiredArgsConstructor
@PreAuthorize("hasRole('STAFF')")
public class StaffQuizController {

    private final StaffQuizService staffQuizService;

    @PostMapping
    public ResponseEntity<ApiResponse<QuizDetailResponse>> createQuiz(
            @Valid @RequestBody CreateQuizRequest request, Authentication authentication) {
        QuizDetailResponse data = staffQuizService.createQuiz(request, authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.<QuizDetailResponse>builder()
                        .status(201)
                        .message("Tạo bài trắc nghiệm thành công")
                        .data(data)
                        .build());
    }

    @GetMapping
    public ResponseEntity<ApiResponse<QuizListResponse>> listQuizzes(
            @RequestParam(required = false) String jlptLevel,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long lessonId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication) {
        QuizListResponse data =
                staffQuizService.listQuizzes(jlptLevel, status, lessonId, page, size, authentication.getName());
        return ResponseEntity.ok(ApiResponse.success("OK", data));
    }

    @GetMapping("/{assessmentId}")
    public ResponseEntity<ApiResponse<QuizDetailResponse>> getQuiz(
            @PathVariable Long assessmentId, Authentication authentication) {
        QuizDetailResponse data = staffQuizService.getQuiz(assessmentId, authentication.getName());
        return ResponseEntity.ok(ApiResponse.success("OK", data));
    }

    @PutMapping("/{assessmentId}")
    public ResponseEntity<ApiResponse<QuizDetailResponse>> updateQuiz(
            @PathVariable Long assessmentId,
            @Valid @RequestBody UpdateQuizRequest request,
            Authentication authentication) {
        QuizDetailResponse data = staffQuizService.updateQuiz(assessmentId, request, authentication.getName());
        return ResponseEntity.ok(ApiResponse.success("Cập nhật bài trắc nghiệm thành công", data));
    }

    @PostMapping("/{assessmentId}/assign-questions")
    public ResponseEntity<ApiResponse<AssignResultResponse>> assignQuestions(
            @PathVariable Long assessmentId,
            @Valid @RequestBody AssignQuestionsRequest request,
            Authentication authentication) {
        AssignResultResponse data = staffQuizService.assignQuestions(assessmentId, request, authentication.getName());
        return ResponseEntity.ok(ApiResponse.success("Gán câu hỏi vào bài trắc nghiệm thành công", data));
    }
}
