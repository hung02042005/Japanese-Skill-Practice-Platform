/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.staffcontent.learning;

import com.jlpt.feature.staffcontent.learning.dto.CreateKanjiRequest;
import com.jlpt.feature.staffcontent.learning.dto.CreateLessonRequest;
import com.jlpt.feature.staffcontent.learning.dto.CreateVocabularyRequest;
import com.jlpt.feature.staffcontent.learning.dto.KanjiDetailResponse;
import com.jlpt.feature.staffcontent.learning.dto.LessonDetailResponse;
import com.jlpt.feature.staffcontent.learning.dto.UpdateKanjiRequest;
import com.jlpt.feature.staffcontent.learning.dto.UpdateLessonRequest;
import com.jlpt.feature.staffcontent.learning.dto.UpdateVocabularyRequest;
import com.jlpt.feature.staffcontent.learning.dto.VocabularyDetailResponse;
import com.jlpt.shared.common.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * UC-27 — Staff endpoints for managing learning content (lesson / vocabulary / kanji).
 * Note: "course" maps to the {@code lessons} table; there is no separate /api/staff/courses (UC-27 §9).
 */
@RestController
@RequestMapping("/api/staff")
@RequiredArgsConstructor
@PreAuthorize("hasRole('STAFF')")
@Validated
public class StaffLearningContentController {

    private final LearningContentService learningContentService;

    // ── Lesson CRUD ────────────────────────────────────────────────

    @PostMapping("/lessons")
    public ResponseEntity<ApiResponse<LessonDetailResponse>> createLesson(
            @Valid @RequestBody CreateLessonRequest request, Authentication authentication) {
        LessonDetailResponse data = learningContentService.createLesson(request, authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.<LessonDetailResponse>builder()
                        .status(201)
                        .message("Tạo học liệu thành công")
                        .data(data)
                        .build());
    }

    @PutMapping("/lessons/{lessonId}")
    public ResponseEntity<ApiResponse<LessonDetailResponse>> updateLesson(
            @PathVariable Long lessonId,
            @Valid @RequestBody UpdateLessonRequest request,
            Authentication authentication) {
        LessonDetailResponse data = learningContentService.updateLesson(lessonId, request, authentication.getName());
        return ResponseEntity.ok(ApiResponse.success("Cập nhật học liệu thành công", data));
    }

    @GetMapping("/lessons")
    public ResponseEntity<ApiResponse<Map<String, Object>>> listLessons(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String jlptLevel,
            @RequestParam(required = false) String lessonType,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") @Min(value = 0, message = "page phải >= 0") int page,
            @RequestParam(defaultValue = "20")
                    @Min(value = 1, message = "size phải >= 1")
                    @Max(value = 100, message = "size tối đa 100")
                    int size,
            Authentication authentication) {
        Page<LessonDetailResponse> resultPage = learningContentService.listLessons(
                q, jlptLevel, lessonType, status, page, size, authentication.getName());
        Map<String, Object> data = new HashMap<>();
        data.put("content", resultPage.getContent());
        data.put("totalElements", resultPage.getTotalElements());
        data.put("totalPages", resultPage.getTotalPages());
        return ResponseEntity.ok(ApiResponse.success("OK", data));
    }

    @GetMapping("/lessons/{lessonId}")
    public ResponseEntity<ApiResponse<LessonDetailResponse>> getLesson(
            @PathVariable Long lessonId, Authentication authentication) {
        LessonDetailResponse data = learningContentService.getLesson(lessonId, authentication.getName());
        return ResponseEntity.ok(ApiResponse.success("OK", data));
    }

    // ── Vocabulary CRUD ───────────────────────────────────────────

    @PostMapping("/vocabulary")
    public ResponseEntity<ApiResponse<VocabularyDetailResponse>> createVocabulary(
            @Valid @RequestBody CreateVocabularyRequest request, Authentication authentication) {
        VocabularyDetailResponse data = learningContentService.createVocabulary(request, authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.<VocabularyDetailResponse>builder()
                        .status(201)
                        .message("Tạo từ vựng thành công")
                        .data(data)
                        .build());
    }

    @PutMapping("/vocabulary/{vocabularyId}")
    public ResponseEntity<ApiResponse<VocabularyDetailResponse>> updateVocabulary(
            @PathVariable Long vocabularyId,
            @Valid @RequestBody UpdateVocabularyRequest request,
            Authentication authentication) {
        VocabularyDetailResponse data =
                learningContentService.updateVocabulary(vocabularyId, request, authentication.getName());
        return ResponseEntity.ok(ApiResponse.success("Cập nhật từ vựng thành công", data));
    }

    @GetMapping("/vocabulary")
    public ResponseEntity<ApiResponse<Map<String, Object>>> listVocabulary(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String jlptLevel,
            @RequestParam(required = false) Long topicId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") @Min(value = 0, message = "page phải >= 0") int page,
            @RequestParam(defaultValue = "20")
                    @Min(value = 1, message = "size phải >= 1")
                    @Max(value = 100, message = "size tối đa 100")
                    int size,
            Authentication authentication) {
        Page<VocabularyDetailResponse> resultPage = learningContentService.listVocabulary(
                q, jlptLevel, topicId, status, page, size, authentication.getName());
        Map<String, Object> data = new HashMap<>();
        data.put("content", resultPage.getContent());
        data.put("totalElements", resultPage.getTotalElements());
        data.put("totalPages", resultPage.getTotalPages());
        return ResponseEntity.ok(ApiResponse.success("OK", data));
    }

    @GetMapping("/vocabulary/{vocabularyId}")
    public ResponseEntity<ApiResponse<VocabularyDetailResponse>> getVocabulary(
            @PathVariable Long vocabularyId, Authentication authentication) {
        VocabularyDetailResponse data = learningContentService.getVocabulary(vocabularyId, authentication.getName());
        return ResponseEntity.ok(ApiResponse.success("OK", data));
    }

    // ── Kanji CRUD ────────────────────────────────────────────────

    @PostMapping("/kanji")
    public ResponseEntity<ApiResponse<KanjiDetailResponse>> createKanji(
            @Valid @RequestBody CreateKanjiRequest request, Authentication authentication) {
        KanjiDetailResponse data = learningContentService.createKanji(request, authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.<KanjiDetailResponse>builder()
                        .status(201)
                        .message("Tạo Kanji thành công")
                        .data(data)
                        .build());
    }

    @PutMapping("/kanji/{kanjiId}")
    public ResponseEntity<ApiResponse<KanjiDetailResponse>> updateKanji(
            @PathVariable Long kanjiId, @Valid @RequestBody UpdateKanjiRequest request, Authentication authentication) {
        KanjiDetailResponse data = learningContentService.updateKanji(kanjiId, request, authentication.getName());
        return ResponseEntity.ok(ApiResponse.success("Cập nhật Kanji thành công", data));
    }

    @GetMapping("/kanji")
    public ResponseEntity<ApiResponse<Map<String, Object>>> listKanji(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String jlptLevel,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") @Min(value = 0, message = "page phải >= 0") int page,
            @RequestParam(defaultValue = "20")
                    @Min(value = 1, message = "size phải >= 1")
                    @Max(value = 100, message = "size tối đa 100")
                    int size,
            Authentication authentication) {
        Page<KanjiDetailResponse> resultPage =
                learningContentService.listKanji(q, jlptLevel, status, page, size, authentication.getName());
        Map<String, Object> data = new HashMap<>();
        data.put("content", resultPage.getContent());
        data.put("totalElements", resultPage.getTotalElements());
        data.put("totalPages", resultPage.getTotalPages());
        return ResponseEntity.ok(ApiResponse.success("OK", data));
    }

    @GetMapping("/kanji/{kanjiId}")
    public ResponseEntity<ApiResponse<KanjiDetailResponse>> getKanji(
            @PathVariable Long kanjiId, Authentication authentication) {
        KanjiDetailResponse data = learningContentService.getKanji(kanjiId, authentication.getName());
        return ResponseEntity.ok(ApiResponse.success("OK", data));
    }
}
