/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.speaking.controller;

import com.jlpt.feature.speaking.dto.SpeakingLessonCreateRequest;
import com.jlpt.feature.speaking.dto.SpeakingLessonDetailResponse;
import com.jlpt.feature.speaking.dto.SpeakingLessonMutationResponse;
import com.jlpt.feature.speaking.service.SpeakingAuthoringService;
import com.jlpt.shared.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/staff/speaking-lessons")
@RequiredArgsConstructor
@PreAuthorize("hasRole('STAFF')")
public class StaffSpeakingContentController {

    private final SpeakingAuthoringService authoringService;

    @PostMapping
    public ResponseEntity<ApiResponse<SpeakingLessonMutationResponse>> create(
            @Valid @RequestBody SpeakingLessonCreateRequest request, Authentication authentication) {
        SpeakingLessonMutationResponse data = authoringService.create(request, authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created("Đã tạo bài nói (nháp)", data));
    }

    @PutMapping("/{lessonId}")
    public ResponseEntity<ApiResponse<SpeakingLessonDetailResponse>> update(
            @PathVariable Long lessonId,
            @Valid @RequestBody SpeakingLessonCreateRequest request,
            Authentication authentication) {
        SpeakingLessonDetailResponse data = authoringService.update(lessonId, request, authentication.getName());
        return ResponseEntity.ok(ApiResponse.success("Đã cập nhật bài nói", data));
    }

    @GetMapping("/{lessonId}")
    public ResponseEntity<ApiResponse<SpeakingLessonDetailResponse>> getOwnDetail(
            @PathVariable Long lessonId, Authentication authentication) {
        SpeakingLessonDetailResponse data = authoringService.getOwnDetail(lessonId, authentication.getName());
        return ResponseEntity.ok(ApiResponse.success("OK", data));
    }
}
