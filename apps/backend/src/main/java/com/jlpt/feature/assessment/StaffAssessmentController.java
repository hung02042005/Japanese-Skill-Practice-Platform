/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.assessment;

import com.jlpt.feature.assessment.dto.request.MockTestRequest;
import com.jlpt.feature.assessment.dto.request.QuestionRequest;
import com.jlpt.feature.assessment.dto.request.QuizRequest;
import com.jlpt.feature.assessment.dto.response.AssessmentSummaryResponse;
import com.jlpt.feature.assessment.dto.response.MockTestResponse;
import com.jlpt.feature.assessment.dto.response.QuestionResponse;
import com.jlpt.feature.assessment.dto.response.QuizResponse;
import com.jlpt.feature.learning.Kanji;
import com.jlpt.feature.staff.StaffUser;
import com.jlpt.feature.staff.StaffUserRepository;
import com.jlpt.feature.student.StudentUser;
import com.jlpt.shared.common.ApiResponse;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/staff/v2")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
public class StaffAssessmentController {

    private final QuizService quizService;
    private final MockTestService mockTestService;
    private final StaffUserRepository staffUserRepository;

    // -----------------------------------------------------------
    // Quiz Management
    // -----------------------------------------------------------

    /** Tạo quiz mới (DRAFT). */
    @PostMapping("/assessments/quiz")
    public ResponseEntity<ApiResponse<QuizResponse>> createQuiz(
            @Valid @RequestBody QuizRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        StaffUser staffUser = resolveStaffUser(userDetails);
        return ResponseEntity.ok(ApiResponse.success("Quiz created", quizService.createQuiz(request, staffUser)));
    }

    /** Tạo đề thi mock test mới (DRAFT). */
    @PostMapping("/assessments/mock-test")
    public ResponseEntity<ApiResponse<MockTestResponse>> createMockTest(
            @Valid @RequestBody MockTestRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        StaffUser staffUser = resolveStaffUser(userDetails);
        return ResponseEntity.ok(
                ApiResponse.success("Mock test created", mockTestService.generateMockTest(request, staffUser)));
    }

    /** Thêm câu hỏi vào assessment. */
    @PostMapping("/assessments/{id}/questions")
    public ResponseEntity<ApiResponse<String>> addQuestions(
            @PathVariable Long id,
            @Valid @RequestBody List<QuestionRequest> requests,
            @AuthenticationPrincipal UserDetails userDetails) {
        StaffUser staffUser = resolveStaffUser(userDetails);
        quizService.addQuestions(id, requests, staffUser);
        return ResponseEntity.ok(ApiResponse.success("Questions added successfully"));
    }

    /**
     * Lấy danh sách assessments do staff quản lý (có thể filter theo type, status, jlptLevel).
     * GET /api/staff/assessments?type=QUIZ&status=DRAFT&jlptLevel=N5&page=0&size=10
     */
    @GetMapping("/assessments")
    public ResponseEntity<ApiResponse<Map<String, Object>>> listAssessments(
            @RequestParam(defaultValue = "QUIZ") String type,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String jlptLevel,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Assessment.AssessmentType assessmentType;
        try {
            assessmentType = Assessment.AssessmentType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(400, "type không hợp lệ: " + type));
        }

        Kanji.ContentStatus contentStatus = null;
        if (status != null) {
            try {
                contentStatus = Kanji.ContentStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest().body(ApiResponse.error(400, "status không hợp lệ: " + status));
            }
        }

        StudentUser.JlptLevel level = null;
        if (jlptLevel != null) {
            try {
                level = StudentUser.JlptLevel.valueOf(jlptLevel.toUpperCase());
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest().body(ApiResponse.error(400, "jlptLevel không hợp lệ: " + jlptLevel));
            }
        }

        Page<AssessmentSummaryResponse> result = quizService.listAssessmentsForStaff(
                assessmentType, contentStatus, level, PageRequest.of(page, size));

        Map<String, Object> data = Map.of(
                "content", result.getContent(),
                "totalElements", result.getTotalElements(),
                "totalPages", result.getTotalPages());
        return ResponseEntity.ok(ApiResponse.success(data));
    }

    /** Cập nhật thông tin assessment (title, topic, jlptLevel, durationMin, passScore). */
    @PutMapping("/assessments/{id}")
    public ResponseEntity<ApiResponse<QuizResponse>> updateAssessment(
            @PathVariable Long id,
            @Valid @RequestBody QuizRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Assessment updated", quizService.updateAssessment(id, request)));
    }

    /** Lấy danh sách câu hỏi trong một assessment. */
    @GetMapping("/assessments/{id}/questions")
    public ResponseEntity<ApiResponse<List<QuestionResponse>>> getQuestions(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(quizService.getQuestionsOfAssessment(id)));
    }

    /** Soft delete assessment (is_deleted = true) — KHÔNG hard delete. */
    @DeleteMapping("/assessments/{id}")
    public ResponseEntity<ApiResponse<String>> deleteAssessment(@PathVariable Long id) {
        quizService.softDeleteAssessment(id);
        return ResponseEntity.ok(ApiResponse.success("Assessment deleted successfully"));
    }

    // -----------------------------------------------------------
    // Private helpers
    // -----------------------------------------------------------

    private StaffUser resolveStaffUser(UserDetails userDetails) {
        return staffUserRepository.findByEmail(userDetails.getUsername()).orElse(null);
    }
}
