/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.publishedcontent;

import com.jlpt.feature.publishedcontent.dto.DeletedContentResponse;
import com.jlpt.shared.common.ApiResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST Controller quản lý thùng rác (nội dung bị soft-deleted) dành cho Manager.
 * Hỗ trợ liệt kê và khôi phục các mục học liệu (Bài học, Câu hỏi, Từ vựng, Ngữ pháp, Kanji, Bài kiểm tra).
 */
@RestController
@RequestMapping("/api/manager/deleted-contents")
@RequiredArgsConstructor
@PreAuthorize("hasRole('STAFF')")
@Validated
public class ManagerDeletedContentController {

    private final ManagerDeletedContentService deletedContentService;

    /** GET /api/manager/deleted-contents?type={type} — Liệt kê các mục bị soft-deleted theo loại hoặc tất cả. */
    @GetMapping
    public ResponseEntity<ApiResponse<List<DeletedContentResponse>>> listDeleted(
            @RequestParam(value = "type", required = false) String type,
            Authentication authentication) {
        List<DeletedContentResponse> data = deletedContentService.listDeleted(authentication.getName(), type);
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách nội dung bị xóa thành công", data));
    }

    /** POST /api/manager/deleted-contents/{type}/{id}/restore — Khôi phục mục bị soft-deleted về published. */
    @PostMapping("/{type}/{id}/restore")
    public ResponseEntity<ApiResponse<Void>> restore(
            @PathVariable("type") String type,
            @PathVariable("id") Long id,
            Authentication authentication) {
        deletedContentService.restore(authentication.getName(), type, id);
        return ResponseEntity.ok(ApiResponse.success("Khôi phục nội dung thành công", null));
    }
}
