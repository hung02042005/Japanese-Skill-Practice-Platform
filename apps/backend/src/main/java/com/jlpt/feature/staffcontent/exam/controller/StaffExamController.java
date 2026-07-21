/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.staffcontent.exam.controller;

import com.jlpt.feature.staffcontent.exam.service.StaffExamService;

import com.jlpt.feature.staffcontent.exam.dto.CreateExamRequest;
import com.jlpt.feature.staffcontent.exam.dto.ExamAssignQuestionsRequest;
import com.jlpt.feature.staffcontent.exam.dto.ExamAssignResultResponse;
import com.jlpt.feature.staffcontent.exam.dto.ExamDetailResponse;
import com.jlpt.feature.staffcontent.exam.dto.ExamListResponse;
import com.jlpt.feature.staffcontent.exam.dto.UpdateExamRequest;
import com.jlpt.shared.common.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * UC-28 — Staff endpoints for managing JLPT mock exams ({@code assessment_type = 'exam'}).
 * ADD-ONLY MODE: new controller at its own base path; quiz controller handles
 * {@code /api/staff/assessments}.
 */
@RestController
@RequestMapping("/api/staff/exams")
@RequiredArgsConstructor
@PreAuthorize("hasRole('STAFF')")
@Validated
public class StaffExamController {

    private final StaffExamService staffExamService;

    @PostMapping
    public ResponseEntity<ApiResponse<ExamDetailResponse>> createExam(
            @Valid @RequestBody CreateExamRequest request, Authentication authentication) {
        ExamDetailResponse data = staffExamService.createExam(request, authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created("Tạo đề thi thử thành công", data));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<ExamListResponse>> listExams(
            @RequestParam(required = false) String jlptLevel,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") @Min(value = 0, message = "page phải >= 0") int page,
            @RequestParam(defaultValue = "20")
                    @Min(value = 1, message = "size phải >= 1")
                    @Max(value = 100, message = "size tối đa 100")
                    int size,
            Authentication authentication) {
        ExamListResponse data = staffExamService.listExams(jlptLevel, status, page, size, authentication.getName());
        return ResponseEntity.ok(ApiResponse.success("OK", data));
    }

    @GetMapping("/{assessmentId}")
    public ResponseEntity<ApiResponse<ExamDetailResponse>> getExam(
            @PathVariable Long assessmentId, Authentication authentication) {
        ExamDetailResponse data = staffExamService.getExam(assessmentId, authentication.getName());
        return ResponseEntity.ok(ApiResponse.success("OK", data));
    }

    @PutMapping("/{assessmentId}")
    public ResponseEntity<ApiResponse<ExamDetailResponse>> updateExam(
            @PathVariable Long assessmentId,
            @Valid @RequestBody UpdateExamRequest request,
            Authentication authentication) {
        ExamDetailResponse data = staffExamService.updateExam(assessmentId, request, authentication.getName());
        return ResponseEntity.ok(ApiResponse.success("Cập nhật đề thi thử thành công", data));
    }

    @PostMapping("/{assessmentId}/assign-questions")
    public ResponseEntity<ApiResponse<ExamAssignResultResponse>> assignQuestions(
            @PathVariable Long assessmentId,
            @Valid @RequestBody ExamAssignQuestionsRequest request,
            Authentication authentication) {
        ExamAssignResultResponse data =
                staffExamService.assignQuestions(assessmentId, request, authentication.getName());
        return ResponseEntity.ok(ApiResponse.success("Gán câu hỏi vào đề thi thành công", data));
    }
}
