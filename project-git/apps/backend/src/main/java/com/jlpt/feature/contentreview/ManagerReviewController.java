/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.contentreview;

import com.jlpt.feature.contentreview.dto.RequestChangesRequest;
import com.jlpt.feature.contentreview.dto.ReviewActionRequest;
import com.jlpt.feature.contentreview.dto.ReviewQueueResponse;
import com.jlpt.feature.contentreview.dto.ReviewResultResponse;
import com.jlpt.feature.contentreview.dto.ReviewableContentDetailResponse;
import com.jlpt.shared.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * UC-33 — Review Queue cho StaffManager.
 *
 * <p>{@code @PreAuthorize("hasRole('STAFF')")} chặn student/admin ở tầng web; việc giới hạn
 * riêng vai trò {@code staff_manager} được {@link ContentReviewService} thực thi ở Service Layer
 * (FR-33-02/18) — do JWT hiện chỉ cấp authority ROLE_STAFF cho mọi nhân viên.
 */
@RestController
@RequestMapping("/api/manager")
@RequiredArgsConstructor
@PreAuthorize("hasRole('STAFF')")
public class ManagerReviewController {

    private final ContentReviewService contentReviewService;

    /** GET /api/manager/review-queue */
    @GetMapping("/review-queue")
    public ResponseEntity<ApiResponse<ReviewQueueResponse>> getReviewQueue(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String jlptLevel,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication) {
        ReviewQueueResponse data =
                contentReviewService.getReviewQueue(authentication.getName(), type, jlptLevel, page, size);
        return ResponseEntity.ok(ApiResponse.success("Lấy hàng đợi phê duyệt thành công", data));
    }

    /** GET /api/manager/contents/{contentId}?contentType=... */
    @GetMapping("/contents/{contentId}")
    public ResponseEntity<ApiResponse<ReviewableContentDetailResponse>> getContentDetail(
            @PathVariable Long contentId, @RequestParam String contentType, Authentication authentication) {
        ReviewableContentDetailResponse data =
                contentReviewService.getContentDetail(authentication.getName(), contentId, contentType);
        return ResponseEntity.ok(ApiResponse.success("OK", data));
    }

    /** POST /api/manager/reviews — Approve / Reject */
    @PostMapping("/reviews")
    public ResponseEntity<ApiResponse<ReviewResultResponse>> review(
            @Valid @RequestBody ReviewActionRequest request, Authentication authentication) {
        ReviewResultResponse data = contentReviewService.review(authentication.getName(), request);
        String message =
                "published".equals(data.getStatus()) ? "Phê duyệt nội dung thành công" : "Từ chối nội dung thành công";
        return ResponseEntity.ok(ApiResponse.success(message, data));
    }

    /** POST /api/manager/reviews/request-changes */
    @PostMapping("/reviews/request-changes")
    public ResponseEntity<ApiResponse<ReviewResultResponse>> requestChanges(
            @Valid @RequestBody RequestChangesRequest request, Authentication authentication) {
        ReviewResultResponse data = contentReviewService.requestChanges(authentication.getName(), request);
        return ResponseEntity.ok(ApiResponse.success("Yêu cầu chỉnh sửa nội dung thành công", data));
    }
}
