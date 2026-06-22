/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.learning;

import com.jlpt.feature.flashcard.Flashcard;
import com.jlpt.feature.flashcard.repository.FlashcardRepository;
import com.jlpt.feature.learning.dto.VocabTopicResponse;
import com.jlpt.feature.learning.dto.VocabularyListItemResponse;
import com.jlpt.feature.learning.dto.VocabularyListResponse;
import com.jlpt.feature.student.StudentContentProgress;
import com.jlpt.feature.student.StudentContentProgress.ContentType;
import com.jlpt.feature.student.StudentContentProgress.ProgressStatus;
import com.jlpt.feature.student.StudentContentProgressRepository;
import com.jlpt.feature.student.StudentUser;
import com.jlpt.shared.common.JlptLevels;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

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
    private final VocabularyRepository vocabularyRepository;
    private final StudentContentProgressRepository progressRepository;
    private final FlashcardRepository flashcardRepository;

    public List<VocabTopicResponse> getTopics(String level) {
        StudentUser.JlptLevel jlptLevel = JlptLevels.parseRequired(level);
        return topicRepository.findPublishedByLevel(jlptLevel, Kanji.ContentStatus.PUBLISHED).stream()
                .map(VocabTopicResponse::from)
                .toList();
    }

    /**
     * UC-09 (FR-LEARN-30/31/34) — danh sách từ vựng published lọc theo level + topicId + từ khoá.
     * Đính kèm {@code isCompleted} (tiến độ) và {@code isInFlashcard} (đã thêm flashcard) cho từng từ.
     */
    public VocabularyListResponse getVocabularyList(
            String level, Long topicId, String search, int page, int size, Long studentId) {
        StudentUser.JlptLevel jlptLevel = StringUtils.hasText(level) ? JlptLevels.parseRequired(level) : null;
        String q = StringUtils.hasText(search) ? search.trim() : null;
        int safeSize = Math.min(Math.max(size, 1), 50);

        Page<Vocabulary> result = vocabularyRepository.findPublished(
                Kanji.ContentStatus.PUBLISHED, jlptLevel, topicId, q,
                PageRequest.of(Math.max(page, 0), safeSize));

        List<Long> ids = result.getContent().stream().map(Vocabulary::getId).toList();

        Set<Long> completedIds = new HashSet<>();
        Set<Long> inFlashcardIds = new HashSet<>();
        if (!ids.isEmpty()) {
            progressRepository
                    .findByStudentIdAndContentTypeAndContentIdIn(studentId, ContentType.VOCABULARY, ids)
                    .forEach(p -> {
                        if (p.getStatus() == ProgressStatus.COMPLETED) {
                            completedIds.add(p.getContentId());
                        }
                    });
            flashcardRepository
                    .findByStudentAndContentIds(studentId, Flashcard.ContentType.VOCABULARY, ids)
                    .forEach(f -> inFlashcardIds.add(f.getContentId()));
        }

        List<VocabularyListItemResponse> content = result.getContent().stream()
                .map(v -> VocabularyListItemResponse.builder()
                        .id(v.getId())
                        .word(v.getWord())
                        .furigana(v.getFurigana())
                        .meaning(v.getMeaning())
                        .wordType(v.getWordType())
                        .jlptLevel(v.getJlptLevel() != null ? v.getJlptLevel().name() : null)
                        .topicId(v.getTopicRef() != null ? v.getTopicRef().getId() : null)
                        .audioUrl(v.getAudioUrl())
                        .exampleSentenceJp(v.getExampleSentenceJp())
                        .exampleSentenceVi(v.getExampleSentenceVi())
                        .isCompleted(completedIds.contains(v.getId()))
                        .isInFlashcard(inFlashcardIds.contains(v.getId()))
                        .build())
                .toList();

        long completedCount = jlptLevel == null
                ? progressRepository.countCompleted(studentId, ContentType.VOCABULARY, ProgressStatus.COMPLETED)
                : progressRepository.countCompletedVocabularyByLevel(
                        studentId, jlptLevel, ContentType.VOCABULARY, ProgressStatus.COMPLETED);

        return VocabularyListResponse.builder()
                .content(content)
                .totalElements(result.getTotalElements())
                .totalPages(result.getTotalPages())
                .page(result.getNumber())
                .size(result.getSize())
                .completedCount(completedCount)
                .build();
    }
}
