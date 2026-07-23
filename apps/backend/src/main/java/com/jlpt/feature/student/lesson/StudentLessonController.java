/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.student.lesson;

import com.jlpt.feature.student.lesson.dto.LessonDetailResponse;
import com.jlpt.shared.common.ApiResponse;
import com.jlpt.shared.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Chi tiết bài học cho học viên. GET /api/lessons/{id}.
 */
@RestController
@RequestMapping("/api/lessons")
@RequiredArgsConstructor
@PreAuthorize("hasRole('STUDENT')")
public class StudentLessonController {

    private final StudentLessonService studentLessonService;

    @GetMapping("/{lessonId}")
    public ResponseEntity<ApiResponse<LessonDetailResponse>> getLessonDetail(
            @PathVariable Long lessonId, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        LessonDetailResponse response = studentLessonService.getLessonDetail(
                lessonId, userDetails.getStudentUser().getId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
