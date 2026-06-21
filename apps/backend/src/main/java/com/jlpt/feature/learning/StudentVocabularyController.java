/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.learning;

import com.jlpt.feature.learning.dto.VocabTopicResponse;
import com.jlpt.shared.common.ApiResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * API từ vựng cho Student — UC-09 (màn "Chủ đề khoá học").
 *
 * <p>{@code GET /api/vocabulary/topics?level=N5} trả danh sách tên chủ đề (title_vi) của cấp độ,
 * sắp theo thứ tự hiển thị. FE dùng cho lưới chủ đề và làm tham số mở phiên flashcard.
 */
@RestController
@RequestMapping("/api/vocabulary")
@RequiredArgsConstructor
@PreAuthorize("hasRole('STUDENT')")
public class StudentVocabularyController {

    private final StudentVocabularyService vocabularyService;

    @GetMapping("/topics")
    public ResponseEntity<ApiResponse<List<VocabTopicResponse>>> getTopics(@RequestParam String level) {
        return ResponseEntity.ok(ApiResponse.success(vocabularyService.getTopics(level)));
    }
}
