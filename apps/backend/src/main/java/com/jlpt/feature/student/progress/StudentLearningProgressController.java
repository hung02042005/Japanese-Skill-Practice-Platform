/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.student.progress;

import com.jlpt.feature.student.progress.dto.LearningProgressRequest;
import com.jlpt.feature.student.progress.dto.LearningProgressResponse;
import com.jlpt.shared.common.ApiResponse;
import com.jlpt.shared.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/learning-progress")
@RequiredArgsConstructor
@PreAuthorize("hasRole('STUDENT')")
public class StudentLearningProgressController {

    private final StudentLearningProgressService progressService;

    @PostMapping
    public ResponseEntity<ApiResponse<LearningProgressResponse>> markProgress(
            @RequestBody LearningProgressRequest request, @AuthenticationPrincipal UserDetailsImpl userDetails) {

        LearningProgressResponse response = progressService.markProgress(
                request, userDetails.getStudentUser().getId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/reset")
    public ResponseEntity<ApiResponse<Void>> resetProgress(
            @RequestParam String contentType, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        progressService.resetProgress(contentType, userDetails.getStudentUser().getId());
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
