/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.learning;

import com.jlpt.feature.learning.dto.VocabTopicResponse;
import com.jlpt.feature.student.StudentUser;
import com.jlpt.shared.common.JlptLevels;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Truy vấn nội dung từ vựng cho Student (read-only) — phục vụ màn "Chủ đề khoá học".
 *
 * <p>Trả {@link VocabTopicResponse} (topicId, slug, titleJa, titleVi) sắp theo {@code display_order}.
 * FE dùng {@code topicId} làm khoá mở phiên flashcard và lọc danh sách từ.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StudentVocabularyService {

    private final VocabularyTopicRepository topicRepository;

    public List<VocabTopicResponse> getTopics(String level) {
        StudentUser.JlptLevel jlptLevel = JlptLevels.parseRequired(level);
        return topicRepository.findPublishedByLevel(jlptLevel, Kanji.ContentStatus.PUBLISHED).stream()
                .map(VocabTopicResponse::from)
                .toList();
    }
}
