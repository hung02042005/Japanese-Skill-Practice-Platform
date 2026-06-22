package com.jlpt.feature.student.reading.controller;

import com.jlpt.feature.learning.Lesson;
import com.jlpt.feature.student.StudentUser;
import com.jlpt.feature.student.reading.dto.ReadingDetailResponse;
import com.jlpt.feature.student.reading.dto.ReadingLessonSummaryResponse;
import com.jlpt.feature.student.reading.dto.ReadingSubmitRequest;
import com.jlpt.feature.student.reading.dto.ReadingSubmitResponse;
import com.jlpt.feature.student.reading.service.StudentReadingService;
import com.jlpt.shared.common.ApiResponse;
import com.jlpt.shared.security.UserDetailsImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/lessons")
@RequiredArgsConstructor
@PreAuthorize("hasRole('STUDENT')")
public class StudentReadingController {

    private final StudentReadingService readingService;

    @GetMapping(params = "type=reading")
    public ApiResponse<Page<ReadingLessonSummaryResponse>> getReadingLessons(
            @RequestParam(name = "level") StudentUser.JlptLevel level,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        Page<ReadingLessonSummaryResponse> response = readingService.getLessonList(
                Lesson.LessonType.READING, level, userDetails.getStudentUser().getId(), page, size);
        return ApiResponse.success(response);
    }

    @GetMapping("/{lessonId}/reading")
    public ApiResponse<ReadingDetailResponse> getReadingDetail(
            @PathVariable Long lessonId,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        ReadingDetailResponse response = readingService.getReadingDetail(lessonId, userDetails.getStudentUser().getId());
        return ApiResponse.success(response);
    }

    @PostMapping("/{lessonId}/submit")
    public ApiResponse<ReadingSubmitResponse> submitReading(
            @PathVariable Long lessonId,
            @Valid @RequestBody ReadingSubmitRequest request,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        ReadingSubmitResponse response = readingService.submitReading(lessonId, userDetails.getStudentUser().getId(), request);
        return ApiResponse.success(response);
    }
}
