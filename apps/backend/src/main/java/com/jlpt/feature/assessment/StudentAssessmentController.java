/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.assessment;

import com.jlpt.feature.assessment.dto.response.AssessmentSummaryResponse;
import com.jlpt.feature.learning.Kanji;
import com.jlpt.feature.student.StudentUser;
import com.jlpt.shared.common.ApiResponse;
import com.jlpt.shared.exception.ResourceNotFoundException;
import com.jlpt.shared.security.UserDetailsImpl;
import jakarta.validation.Valid;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.jlpt.feature.assessment.dto.request.SubmitExamRequest;

@RestController
@RequestMapping("/api/assessments")
@RequiredArgsConstructor
@PreAuthorize("hasRole('STUDENT')")
public class StudentAssessmentController {

    private final QuizService quizService;
    private final MockExamService mockExamService;
    private final AssessmentRepository assessmentRepository;

    /**
     * Danh sách assessments công khai (PUBLISHED) cho student.
     * GET /api/assessments?type=QUIZ&level=N5&topic=grammar&page=0&size=10
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> listAssessments(
            @RequestParam String type,
            @RequestParam(required = false) String level,
            @RequestParam(required = false) String topic,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Assessment.AssessmentType assessmentType = Assessment.AssessmentType.valueOf(type.toUpperCase());
        StudentUser.JlptLevel jlptLevel = level != null ? StudentUser.JlptLevel.valueOf(level.toUpperCase()) : null;

        Page<Assessment> assessments = assessmentRepository.findAllByAssessmentTypeAndJlptLevelAndStatus(
                assessmentType, Kanji.ContentStatus.PUBLISHED, jlptLevel, topic, PageRequest.of(page, size));
        Page<AssessmentSummaryResponse> mapped = assessments.map(quizService::toSummaryResponse);

        Map<String, Object> data = Map.of(
                "content", mapped.getContent(),
                "totalElements", mapped.getTotalElements(),
                "totalPages", mapped.getTotalPages());
        return ResponseEntity.ok(ApiResponse.success(data));
    }

    /**
     * Bắt đầu làm bài (tạo TestAttempt, trả về danh sách câu hỏi theo section).
     * POST /api/assessments/{id}/start
     */
    @PostMapping("/{id}/start")
    public ResponseEntity<ApiResponse<Object>> startAssessment(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        StudentUser student = userDetails.getStudentUser();
        Assessment assessment = assessmentRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Assessment", id));

        Object response = assessment.getAssessmentType() == Assessment.AssessmentType.EXAM
                ? mockExamService.startExam(id, student)
                : quizService.startQuiz(id, student);
        return ResponseEntity.ok(ApiResponse.success("Bắt đầu làm bài", response));
    }

    /**
     * Nộp bài (submit answers, tính điểm server-side).
     * POST /api/assessments/{id}/submit
     */
    @PostMapping("/{id}/submit")
    public ResponseEntity<ApiResponse<Object>> submitAssessment(
            @PathVariable Long id,
            @Valid @RequestBody SubmitExamRequest request,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        Long studentId = userDetails.getStudentUser().getId();
        Assessment assessment = assessmentRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Assessment", id));

        Object response = assessment.getAssessmentType() == Assessment.AssessmentType.EXAM
                ? mockExamService.submitExam(id, studentId, request)
                : quizService.submitQuiz(id, studentId, request.getAttemptId(), request.getAnswers());
        return ResponseEntity.ok(ApiResponse.success("Nộp bài thành công", response));
    }

    /**
     * Xem kết quả bài làm gần nhất đã SUBMITTED.
     * GET /api/assessments/{id}/result
     */
    @GetMapping("/{id}/result")
    public ResponseEntity<ApiResponse<Object>> getResult(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        Long studentId = userDetails.getStudentUser().getId();
        Assessment assessment = assessmentRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Assessment", id));

        Object response = assessment.getAssessmentType() == Assessment.AssessmentType.EXAM
                ? mockExamService.getExamHistory(studentId, PageRequest.of(0, 1))
                : quizService.getQuizResult(id, studentId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
