/* (c) JLPT E-Learning Platform */
package com.jlpt.bookmark.controller;

import com.jlpt.bookmark.dto.BookmarkRequest;
import com.jlpt.bookmark.dto.BookmarkResponse;
import com.jlpt.bookmark.service.BookmarkService;
import com.jlpt.common.ApiResponse;
import com.jlpt.security.UserDetailsImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/bookmarks")
@RequiredArgsConstructor
@PreAuthorize("hasRole('STUDENT')")
public class StudentBookmarkController {

    private final BookmarkService bookmarkService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<BookmarkResponse>>> listBookmarks(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestParam(required = false) String type,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<BookmarkResponse> bookmarks =
                bookmarkService.listBookmarks(userDetails.getStudentUser().getId(), type, pageable);
        return ResponseEntity.ok(ApiResponse.success(bookmarks));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<BookmarkResponse>> addBookmark(
            @Valid @RequestBody BookmarkRequest request, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        BookmarkResponse response =
                bookmarkService.addBookmark(userDetails.getStudentUser().getId(), request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping
    public ResponseEntity<ApiResponse<Void>> removeBookmark(
            @RequestParam String contentType,
            @RequestParam Long contentId,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        bookmarkService.removeBookmark(userDetails.getStudentUser().getId(), contentType, contentId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
