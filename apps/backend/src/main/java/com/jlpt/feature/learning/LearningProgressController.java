/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.learning;

import com.jlpt.feature.learning.dto.request.MarkLearningProgressRequest;
import com.jlpt.feature.learning.dto.response.LearningProgressResponse;
import com.jlpt.shared.common.ApiResponse;
import com.jlpt.shared.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** UC-06 — Đánh dấu/cập nhật tiến độ học tập (FR-LEARN-03, FR-LEARN-40). */
@RestController
@RequestMapping("/api/learning-progress")
@RequiredArgsConstructor
@PreAuthorize("hasRole('STUDENT')")
public class LearningProgressController {

    private final LearningProgressService learningProgressService;

    @PostMapping
    public ResponseEntity<ApiResponse<LearningProgressResponse>> markProgress(
            @RequestBody MarkLearningProgressRequest request, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        LearningProgressResponse data = learningProgressService.markProgress(
                request, userDetails.getStudentUser().getId());
        return ResponseEntity.ok(ApiResponse.success(data));
    }
}
