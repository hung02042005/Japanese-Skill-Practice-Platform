/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.learning.kana;

import com.jlpt.feature.learning.kana.dto.response.KanaChartResponse;
import com.jlpt.shared.common.ApiResponse;
import com.jlpt.shared.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** UC-08 — GET /api/kana?type=hiragana|katakana — Bảng chữ Kana đầy đủ theo loại. */
@RestController
@RequestMapping("/api/kana")
@RequiredArgsConstructor
@PreAuthorize("hasRole('STUDENT')")
public class KanaController {

    private final KanaService kanaService;

    /**
     * AC-08-01/AC-08-02 — Trả toàn bộ bảng ký tự theo type + thống kê tiến độ.
     * Không phân trang (BR-08-01). Lỗi type không hợp lệ → 400 VALIDATION_FAILED (AC-08-05).
     */
    @GetMapping
    public ResponseEntity<ApiResponse<KanaChartResponse>> getKanaChart(
            @RequestParam String type,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        KanaChartResponse data =
                kanaService.getKanaChart(type, userDetails.getStudentUser().getId());
        return ResponseEntity.ok(ApiResponse.success(data));
    }
}
