/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.staffcontent.learning.controller;

import com.jlpt.feature.learning.dto.VocabTopicResponse;
import com.jlpt.feature.staffcontent.learning.dto.CreateVocabTopicRequest;
import com.jlpt.feature.staffcontent.learning.service.VocabularyTopicService;
import com.jlpt.shared.common.ApiResponse;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Quản lý chủ đề từ vựng (catalog) cho Staff — soạn từ vựng cần gắn topicId.
 * {@code GET ?level=N5} liệt kê chủ đề của cấp độ; {@code POST} tạo chủ đề mới (publish ngay).
 */
@RestController
@RequestMapping("/api/staff/vocabulary-topics")
@RequiredArgsConstructor
@PreAuthorize("hasRole('STAFF')")
public class StaffVocabularyTopicController {

    private final VocabularyTopicService topicService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<VocabTopicResponse>>> list(@RequestParam String level) {
        return ResponseEntity.ok(ApiResponse.success(topicService.listByLevel(level)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<VocabTopicResponse>> create(
            @Valid @RequestBody CreateVocabTopicRequest request, Authentication authentication) {
        VocabTopicResponse data = topicService.create(request, authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created("Tạo chủ đề thành công", data));
    }
}
