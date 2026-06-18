/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.assessment;

import com.jlpt.feature.assessment.dto.request.AnswerSubmissionRequest;
import com.jlpt.feature.assessment.dto.response.ScoreResponse;
import com.jlpt.shared.common.ApiResponse;
import com.jlpt.shared.security.UserDetailsImpl;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@PreAuthorize("hasRole('STUDENT')")
public class StudentAssessmentController {

    private final QuizService quizService;
    private final MockTestService mockTestService;

    @GetMapping("/assessments")
    public ResponseEntity<ApiResponse<String>> getAssessments() {
        return ResponseEntity.ok(ApiResponse.success("Danh sách bài thi", "")); // Mock response
    }

    @PostMapping("/assessments/{id}/submit")
    public ResponseEntity<ApiResponse<ScoreResponse>> submitQuiz(
            @PathVariable Long id,
            @RequestParam Long attemptId,
            @Valid @RequestBody List<AnswerSubmissionRequest> answers,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        ScoreResponse response =
                quizService.submitQuiz(id, userDetails.getStudentUser().getId(), attemptId, answers);
        return ResponseEntity.ok(ApiResponse.success("Nộp bài thành công", response));
    }

    @GetMapping("/assessments/{id}/result")
    public ResponseEntity<ApiResponse<String>> getQuizResult(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Kết quả bài thi", "")); // Mock response
    }

    @GetMapping("/mock-tests")
    public ResponseEntity<ApiResponse<String>> getMockTests() {
        return ResponseEntity.ok(ApiResponse.success("Danh sách bài thi thử", "")); // Mock response
    }

    @PostMapping("/mock-tests/{id}/submit")
    public ResponseEntity<ApiResponse<ScoreResponse>> submitMockTest(
            @PathVariable Long id,
            @RequestParam Long attemptId,
            @Valid @RequestBody List<AnswerSubmissionRequest> answers,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        ScoreResponse response =
                mockTestService.gradeMockTest(id, userDetails.getStudentUser().getId(), attemptId, answers);
        return ResponseEntity.ok(ApiResponse.success("Nộp bài thi thử thành công", response));
    }
}
