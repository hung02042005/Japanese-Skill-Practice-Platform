/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.student.kanji;

import com.jlpt.feature.student.kanji.dto.KanjiDetailResponse;
import com.jlpt.feature.student.kanji.dto.KanjiListResponse;
import com.jlpt.feature.student.kanji.dto.KanjiWritingAttemptRequest;
import com.jlpt.feature.student.kanji.dto.KanjiWritingAttemptResponse;
import com.jlpt.feature.student.kanji.dto.KanjiWritingEvaluateRequest;
import com.jlpt.feature.student.kanji.dto.KanjiWritingEvaluateResponse;
import com.jlpt.shared.common.ApiResponse;
import com.jlpt.shared.security.UserDetailsImpl;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/kanji")
@RequiredArgsConstructor
@PreAuthorize("hasRole('STUDENT')")
@Validated
public class StudentKanjiController {

    private final StudentKanjiService studentKanjiService;
    private final KanjiWritingService kanjiWritingService;

    @GetMapping
    public ResponseEntity<ApiResponse<KanjiListResponse>> getKanjiList(
            @RequestParam String level,
            @RequestParam(defaultValue = "0") @Min(value = 0, message = "page phải >= 0") int page,
            @RequestParam(defaultValue = "20")
                    @Min(value = 1, message = "size phải >= 1")
                    @Max(value = 100, message = "size tối đa 100")
                    int size,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        int validPage = Math.max(0, page);
        int validSize = Math.max(1, Math.min(50, size));

        KanjiListResponse response = studentKanjiService.getKanjiList(
                level, userDetails.getStudentUser().getId(), validPage, validSize);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{kanjiId}")
    public ResponseEntity<ApiResponse<KanjiDetailResponse>> getKanjiDetail(
            @PathVariable Long kanjiId, @AuthenticationPrincipal UserDetailsImpl userDetails) {

        KanjiDetailResponse response = studentKanjiService.getKanjiDetail(
                kanjiId, userDetails.getStudentUser().getId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Chạy DTW so sánh nét người dùng vs nét chuẩn.
     * Stateless — không lưu DB, chỉ trả kết quả để frontend hiển thị.
     */
    @PostMapping("/writing/evaluate-stroke")
    public ResponseEntity<ApiResponse<KanjiWritingEvaluateResponse>> evaluateStroke(
            @Valid @RequestBody KanjiWritingEvaluateRequest request) {

        KanjiWritingEvaluateResponse response = kanjiWritingService.evaluateStroke(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Lưu toàn bộ kết quả một phiên luyện viết vào DB.
     */
    @PostMapping("/writing/attempt")
    public ResponseEntity<ApiResponse<KanjiWritingAttemptResponse>> saveWritingAttempt(
            @Valid @RequestBody KanjiWritingAttemptRequest request,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        KanjiWritingAttemptResponse response = kanjiWritingService.saveAttempt(
                request, userDetails.getStudentUser().getId());
        return ResponseEntity.created(null).body(ApiResponse.created(response));
    }
}
