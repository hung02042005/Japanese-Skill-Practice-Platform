/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.support.controller;

import com.jlpt.feature.support.dto.GradeResponse;
import com.jlpt.feature.support.dto.ManualGradeRequest;
import com.jlpt.feature.support.dto.SubmissionResponse;
import com.jlpt.feature.support.service.SupportTicketService;
import com.jlpt.shared.common.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * Staff — hàng đợi chấm bài nói (UC-31, override điểm AI).
 * Expose các nghiệp vụ đã có ở {@link SupportTicketService}: duyệt danh sách, xem chi tiết, chấm thủ công.
 */
@RestController
@RequestMapping("/api/staff/submissions")
@RequiredArgsConstructor
@PreAuthorize("hasRole('STAFF')")
@Validated
public class StaffGradingController {

    private final SupportTicketService supportTicketService;

    /** GET /api/staff/submissions?type=speaking&status=ai_graded&page=0&size=20 */
    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> listSubmissions(
            @RequestParam(required = false, defaultValue = "speaking") String type,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") @Min(value = 0, message = "page phải >= 0") int page,
            @RequestParam(defaultValue = "20")
                    @Min(value = 1, message = "size phải >= 1")
                    @Max(value = 100, message = "size tối đa 100")
                    int size) {
        Page<SubmissionResponse> result = supportTicketService.getAllSubmissions(type, status, page, size);
        return ResponseEntity.ok(ApiResponse.success(Map.of(
                "content", result.getContent(),
                "totalElements", result.getTotalElements(),
                "totalPages", result.getTotalPages())));
    }

    /** GET /api/staff/submissions/{submissionId} — chi tiết bài nộp + điểm AI. */
    @GetMapping("/{submissionId}")
    public ResponseEntity<ApiResponse<SubmissionResponse>> getSubmission(@PathVariable Long submissionId) {
        return ResponseEntity.ok(ApiResponse.success(supportTicketService.getSubmissionDetail(submissionId)));
    }

    /** POST /api/staff/submissions/{submissionId}/grade — chấm điểm thủ công (override AI). */
    @PostMapping("/{submissionId}/grade")
    public ResponseEntity<ApiResponse<GradeResponse>> gradeSubmission(
            Authentication authentication,
            @PathVariable Long submissionId,
            @Valid @RequestBody ManualGradeRequest req) {
        GradeResponse result = supportTicketService.manualGrade(submissionId, authentication.getName(), req);
        return ResponseEntity.ok(ApiResponse.success("Đã lưu điểm. Học viên sẽ nhận thông báo kết quả.", result));
    }
}
