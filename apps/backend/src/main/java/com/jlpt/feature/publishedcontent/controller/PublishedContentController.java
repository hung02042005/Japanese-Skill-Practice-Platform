/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.publishedcontent.controller;

import com.jlpt.feature.publishedcontent.dto.ChangeStatusRequest;
import com.jlpt.feature.publishedcontent.dto.PublishedContentDetailResponse;
import com.jlpt.feature.publishedcontent.dto.PublishedContentListResponse;
import com.jlpt.feature.publishedcontent.dto.RestoreContentRequest;
import com.jlpt.feature.publishedcontent.dto.StatusChangeResultResponse;
import com.jlpt.feature.publishedcontent.service.PublishedContentService;
import com.jlpt.shared.common.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
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
 * UC-34 — Quản lý trạng thái nội dung đã xuất bản cho StaffManager.
 *
 * <p>{@code @PreAuthorize("hasRole('STAFF')")} chặn student/admin ở tầng web; giới hạn riêng vai trò
 * {@code staff_manager} được {@link PublishedContentService} thực thi ở Service Layer (FR-34-01/02),
 * do JWT hiện chỉ cấp authority ROLE_STAFF cho mọi nhân viên.
 *
 * <p>Lưu ý: endpoint chi tiết dùng {@code GET /api/manager/published-contents/{contentId}} (không phải
 * {@code /api/manager/contents/{contentId}}) để tránh đụng mapping với UC-33 (ManagerReviewController).
 */
@RestController
@RequestMapping("/api/manager")
@RequiredArgsConstructor
@PreAuthorize("hasRole('STAFF')")
@Validated
public class PublishedContentController {

    private final PublishedContentService publishedContentService;

    /** GET /api/manager/published-contents — danh sách nội dung đã xuất bản (FR-34-03..06). */
    @GetMapping("/published-contents")
    public ResponseEntity<ApiResponse<PublishedContentListResponse>> getPublishedContents(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String jlptLevel,
            @RequestParam(defaultValue = "0") @Min(value = 0, message = "page phải >= 0") int page,
            @RequestParam(defaultValue = "20")
                    @Min(value = 1, message = "size phải >= 1")
                    @Max(value = 100, message = "size tối đa 100")
                    int size,
            Authentication authentication) {
        PublishedContentListResponse data =
                publishedContentService.getPublishedContents(authentication.getName(), type, jlptLevel, page, size);
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách nội dung đã xuất bản thành công", data));
    }

    /** GET /api/manager/published-contents/{contentId}?contentType=... — chi tiết + references (FR-34-07..09). */
    @GetMapping("/published-contents/{contentId}")
    public ResponseEntity<ApiResponse<PublishedContentDetailResponse>> getContentDetail(
            @PathVariable Long contentId, @RequestParam String contentType, Authentication authentication) {
        PublishedContentDetailResponse data =
                publishedContentService.getContentDetail(authentication.getName(), contentId, contentType);
        return ResponseEntity.ok(ApiResponse.success("Lấy chi tiết nội dung thành công", data));
    }

    /** PUT /api/manager/published-contents/{contentId}/status — Unpublish / Archive / Delete (FR-34-10..17). */
    @PutMapping("/published-contents/{contentId}/status")
    public ResponseEntity<ApiResponse<StatusChangeResultResponse>> changeStatus(
            @PathVariable Long contentId,
            @Valid @RequestBody ChangeStatusRequest request,
            Authentication authentication) {
        StatusChangeResultResponse data =
                publishedContentService.changeStatus(authentication.getName(), contentId, request);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật trạng thái xuất bản thành công", data));
    }

    /** POST /api/manager/published-contents/{contentId}/restore — khôi phục nội dung archived (FR-34-18..20). */
    @PostMapping("/published-contents/{contentId}/restore")
    public ResponseEntity<ApiResponse<StatusChangeResultResponse>> restore(
            @PathVariable Long contentId,
            @Valid @RequestBody RestoreContentRequest request,
            Authentication authentication) {
        StatusChangeResultResponse data = publishedContentService.restore(authentication.getName(), contentId, request);
        return ResponseEntity.ok(ApiResponse.success("Khôi phục nội dung thành công", data));
    }
}
