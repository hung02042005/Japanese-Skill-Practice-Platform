/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.staffcontent.question;

import com.jlpt.shared.common.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
 * UC-24 — Staff endpoints for managing the question bank.
 * All endpoints require JWT with role STAFF (enforced via SecurityConfig + @PreAuthorize).
 */
@RestController
@RequestMapping("/api/staff/questions")
@RequiredArgsConstructor
@PreAuthorize("hasRole('STAFF')")
@Validated
public class StaffQuestionController {

    private final StaffQuestionService staffQuestionService;

    /**
     * POST /api/staff/questions — Create a new question (FR-24-01..09).
     */
    @PostMapping
    public ResponseEntity<ApiResponse<QuestionResponse>> createQuestion(
            @Valid @RequestBody CreateQuestionRequest request, Authentication authentication) {
        QuestionResponse data = staffQuestionService.createQuestion(request, authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created("Tạo câu hỏi thành công", data));
    }

    /**
     * GET /api/staff/questions — List / search / filter questions (FR-24-10..13).
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> listQuestions(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String skill,
            @RequestParam(required = false) String jlptLevel,
            @RequestParam(required = false) String questionType,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") @Min(value = 0, message = "page phải >= 0") int page,
            @RequestParam(defaultValue = "20")
                    @Min(value = 1, message = "size phải >= 1")
                    @Max(value = 100, message = "size tối đa 100")
                    int size,
            Authentication authentication) {

        // size cap (FR-24-10: max 100)
        int effectiveSize = Math.min(size, 100);

        Pageable pageable = PageRequest.of(page, effectiveSize, Sort.by(Sort.Direction.DESC, "updatedAt"));
        Page<QuestionResponse> resultPage =
                staffQuestionService.listQuestions(q, skill, jlptLevel, questionType, status, pageable);

        Map<String, Object> data = new HashMap<>();
        data.put("content", resultPage.getContent());
        data.put("totalElements", resultPage.getTotalElements());
        data.put("totalPages", resultPage.getTotalPages());

        return ResponseEntity.ok(ApiResponse.success("OK", data));
    }

    /**
     * GET /api/staff/questions/{questionId} — Get question detail (FR-24-14/15).
     */
    @GetMapping("/{questionId}")
    public ResponseEntity<ApiResponse<QuestionResponse>> getQuestion(@PathVariable Long questionId) {
        QuestionResponse data = staffQuestionService.getQuestion(questionId);
        return ResponseEntity.ok(ApiResponse.success("OK", data));
    }

    /**
     * PUT /api/staff/questions/{questionId} — Update question (FR-24-16..19).
     */
    @PutMapping("/{questionId}")
    public ResponseEntity<ApiResponse<QuestionResponse>> updateQuestion(
            @PathVariable Long questionId,
            @Valid @RequestBody UpdateQuestionRequest request,
            Authentication authentication) {
        QuestionResponse data = staffQuestionService.updateQuestion(questionId, request, authentication.getName());
        return ResponseEntity.ok(ApiResponse.success("Cập nhật câu hỏi thành công", data));
    }

    /**
     * POST /api/staff/questions/{questionId}/submit-review — Submit for review (FR-24-20..25).
     */
    @PostMapping("/{questionId}/submit-review")
    public ResponseEntity<ApiResponse<StaffQuestionSubmitReviewResponse>> submitForReview(
            @PathVariable Long questionId, Authentication authentication) {
        StaffQuestionSubmitReviewResponse data =
                staffQuestionService.submitForReview(questionId, authentication.getName());
        return ResponseEntity.ok(ApiResponse.success("Đã gửi câu hỏi để phê duyệt", data));
    }
}
