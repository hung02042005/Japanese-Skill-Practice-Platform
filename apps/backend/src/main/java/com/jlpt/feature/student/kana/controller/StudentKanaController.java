package com.jlpt.feature.student.kana.controller;

import com.jlpt.feature.student.kana.dto.response.KanaListResponse;
import com.jlpt.feature.student.kana.service.KanaService;
import com.jlpt.shared.common.ApiResponse;
import com.jlpt.shared.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/kana")
@RequiredArgsConstructor
@PreAuthorize("hasRole('STUDENT')")
public class StudentKanaController {

    private final KanaService kanaService;

    @GetMapping
    public ResponseEntity<ApiResponse<KanaListResponse>> getKanaChart(
            @RequestParam(name = "script", defaultValue = "hiragana") String script,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
            
        KanaListResponse response = kanaService.getKanaChart(script, userDetails.getStudentUser().getId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/{kanaId}/complete")
    public ResponseEntity<ApiResponse<Void>> markKanaComplete(
            @PathVariable Integer kanaId,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
            
        kanaService.markKanaComplete(kanaId, userDetails.getStudentUser().getId());
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
