/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.assessment;

import com.jlpt.feature.assessment.dto.request.MockTestRequest;
import com.jlpt.feature.assessment.dto.request.QuestionRequest;
import com.jlpt.feature.assessment.dto.request.QuizRequest;
import com.jlpt.feature.assessment.dto.response.MockTestResponse;
import com.jlpt.feature.assessment.dto.response.QuizResponse;
import com.jlpt.feature.staff.StaffUser;
import com.jlpt.feature.staff.StaffUserRepository;
import com.jlpt.shared.common.ApiResponse;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/staff")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
public class StaffAssessmentController {

    private final QuizService quizService;
    private final MockTestService mockTestService;
    private final AssessmentRepository assessmentRepository;
    private final StaffUserRepository staffUserRepository;

    @PostMapping("/assessments/quiz")
    public ResponseEntity<ApiResponse<QuizResponse>> createQuiz(
            @Valid @RequestBody QuizRequest request, @AuthenticationPrincipal UserDetails userDetails) {
        StaffUser staffUser =
                staffUserRepository.findByEmail(userDetails.getUsername()).orElse(null);
        return ResponseEntity.ok(ApiResponse.success("Quiz created", quizService.createQuiz(request, staffUser)));
    }

    @PostMapping("/assessments/mock-test")
    public ResponseEntity<ApiResponse<MockTestResponse>> createMockTest(
            @Valid @RequestBody MockTestRequest request, @AuthenticationPrincipal UserDetails userDetails) {
        StaffUser staffUser =
                staffUserRepository.findByEmail(userDetails.getUsername()).orElse(null);
        return ResponseEntity.ok(
                ApiResponse.success("Mock test created", mockTestService.generateMockTest(request, staffUser)));
    }

    @PostMapping("/assessments/{id}/questions")
    public ResponseEntity<ApiResponse<String>> addQuestions(
            @PathVariable Long id, @Valid @RequestBody List<QuestionRequest> requests) {
        quizService.addQuestions(id, requests);
        return ResponseEntity.ok(ApiResponse.success("Questions added successfully"));
    }

    @DeleteMapping("/assessments/{id}")
    public ResponseEntity<ApiResponse<String>> deleteAssessment(@PathVariable Long id) {
        assessmentRepository.deleteById(id);
        return ResponseEntity.ok(ApiResponse.success("Assessment deleted successfully"));
    }
}
