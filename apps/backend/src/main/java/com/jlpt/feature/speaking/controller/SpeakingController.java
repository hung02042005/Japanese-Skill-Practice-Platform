/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.speaking.controller;

import com.jlpt.feature.speaking.dto.SpeakingExerciseResponse;
import com.jlpt.feature.speaking.dto.SpeakingResultResponse;
import com.jlpt.feature.speaking.dto.SpeakingSubmitResponse;
import com.jlpt.feature.speaking.service.SpeakingService;
import com.jlpt.shared.common.ApiResponse;
import com.jlpt.shared.security.UserDetailsImpl;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/** API luyện nói + chấm điểm AI async cho Student (UC-13). */
@RestController
@RequestMapping("/api/speaking")
@RequiredArgsConstructor
@PreAuthorize("hasRole('STUDENT')")
public class SpeakingController {

    private final SpeakingService speakingService;

    /** GET /api/speaking/exercises?level=N5 — danh sách bài luyện nói theo cấp độ. */
    @GetMapping("/exercises")
    public ResponseEntity<ApiResponse<List<SpeakingExerciseResponse>>> listExercises(
            @RequestParam(defaultValue = "N5") String level, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        List<SpeakingExerciseResponse> data =
                speakingService.getExercises(level, userDetails.getStudentUser().getId());
        return ResponseEntity.ok(ApiResponse.success(data));
    }

    /** POST /api/speaking/submit — nộp audio, trả ngay jobId (async, 202). */
    @PostMapping(value = "/submit", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<SpeakingSubmitResponse>> submit(
            @RequestParam("exerciseId") Long exerciseId,
            @RequestParam("audio") MultipartFile audio,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        SpeakingSubmitResponse data = speakingService.submit(exerciseId, audio, userDetails.getStudentUser());
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(ApiResponse.success("Đã nhận bài, đang xử lý", data));
    }

    /** GET /api/speaking/{jobId} — poll kết quả AI. */
    @GetMapping("/{jobId}")
    public ResponseEntity<ApiResponse<SpeakingResultResponse>> getResult(
            @PathVariable Long jobId, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        SpeakingResultResponse data =
                speakingService.getResult(jobId, userDetails.getStudentUser().getId());
        return ResponseEntity.ok(ApiResponse.success(data));
    }
}
