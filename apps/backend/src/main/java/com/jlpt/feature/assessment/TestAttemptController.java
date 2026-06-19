/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.assessment;

import com.jlpt.feature.assessment.dto.response.ExamHistoryResponse;
import com.jlpt.shared.common.ApiResponse;
import com.jlpt.shared.security.UserDetailsImpl;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/test-attempts")
@RequiredArgsConstructor
@PreAuthorize("hasRole('STUDENT')")
public class TestAttemptController {

    private final MockExamService mockExamService;

    @GetMapping("/{attemptId}/status")
    public ResponseEntity<ApiResponse<?>> getStatus(
            @PathVariable Long attemptId, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        var response = mockExamService.getExamStatus(
                attemptId, userDetails.getStudentUser().getId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{attemptId}/review")
    public ResponseEntity<ApiResponse<?>> getReview(
            @PathVariable Long attemptId, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        var response = mockExamService.getExamReview(
                attemptId, userDetails.getStudentUser().getId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> listHistory(
            @RequestParam String type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        Page<ExamHistoryResponse> history =
                mockExamService.getExamHistory(userDetails.getStudentUser().getId(), PageRequest.of(page, size));

        Map<String, Object> data = Map.of(
                "content", history.getContent(),
                "totalElements", history.getTotalElements(),
                "totalPages", history.getTotalPages());
        return ResponseEntity.ok(ApiResponse.success(data));
    }
}
