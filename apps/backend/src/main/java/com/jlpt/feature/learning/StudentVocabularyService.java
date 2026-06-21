/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.learning;

import com.jlpt.feature.student.StudentUser;
import com.jlpt.shared.exception.BadRequestException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Truy vấn nội dung từ vựng cho Student (read-only) — phục vụ màn "Chủ đề khoá học".
 *
 * <p>Topic trả về là {@code title_vi} của {@link VocabularyTopic} (cùng giá trị với
 * {@code vocabulary.topic}), sắp theo {@code display_order}. Chuỗi này khớp với bộ lọc
 * danh sách từ vựng và tham số {@code topic} của phiên flashcard nên FE dùng trực tiếp.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StudentVocabularyService {

    private final VocabularyTopicRepository topicRepository;

    public List<String> getTopics(String level) {
        StudentUser.JlptLevel jlptLevel = parseLevel(level);
        return topicRepository.findPublishedByLevel(jlptLevel, Kanji.ContentStatus.PUBLISHED).stream()
                .map(VocabularyTopic::getTitleVi)
                .toList();
    }

    private StudentUser.JlptLevel parseLevel(String level) {
        if (level == null || level.isBlank()) {
            throw new BadRequestException("Cấp độ JLPT không được để trống");
        }
        try {
            return StudentUser.JlptLevel.valueOf(level.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Cấp độ JLPT không hợp lệ: " + level);
        }
    }
}
