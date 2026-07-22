/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.staffcontent.feedback.controller;

import com.jlpt.feature.staffcontent.feedback.dto.ReviewFeedbackResponse;
import com.jlpt.feature.staffcontent.feedback.service.StaffReviewFeedbackService;
import com.jlpt.shared.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Staff xem phản hồi (từ chối / yêu cầu chỉnh sửa) của manager cho nội dung của mình. */
@RestController
@RequestMapping("/api/staff")
@RequiredArgsConstructor
@PreAuthorize("hasRole('STAFF')")
public class StaffReviewFeedbackController {

    private final StaffReviewFeedbackService feedbackService;

    @GetMapping("/content/{contentId}/feedback")
    public ResponseEntity<ApiResponse<ReviewFeedbackResponse>> getContentFeedback(
            @PathVariable Long contentId, @RequestParam String contentType, Authentication authentication) {
        ReviewFeedbackResponse data =
                feedbackService.getContentFeedback(contentId, contentType, authentication.getName());
        return ResponseEntity.ok(ApiResponse.success("OK", data));
    }
}
