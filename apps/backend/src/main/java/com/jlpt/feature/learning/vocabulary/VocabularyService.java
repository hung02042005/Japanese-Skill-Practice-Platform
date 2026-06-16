/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.learning.vocabulary;

import com.jlpt.feature.learning.Kanji;
import com.jlpt.feature.learning.Vocabulary;
import com.jlpt.feature.learning.exception.LearningException;
import com.jlpt.feature.learning.vocabulary.dto.response.VocabularyDetailResponse;
import com.jlpt.feature.learning.vocabulary.dto.response.VocabularyProgressSummaryResponse;
import com.jlpt.feature.learning.vocabulary.dto.response.VocabularySummaryResponse;
import com.jlpt.feature.student.StudentContentProgress;
import com.jlpt.feature.student.StudentContentProgressRepository;
import com.jlpt.feature.student.StudentUser;
import com.jlpt.feature.student.StudentUserRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * UC-09 — Học Từ Vựng: danh sách/chi tiết theo level + topic (chỉ published) + cờ tiến độ.
 * BR-09-01: chỉ trả published; BR-09-02: lọc đồng thời level + topic;
 * BR-09-06: mọi lượt xem cập nhật last_activity_date.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class VocabularyService {

    private final VocabularyRepository vocabularyRepository;
    private final StudentContentProgressRepository progressRepository;
    private final StudentUserRepository studentUserRepository;

    @Transactional(readOnly = true)
    public Page<VocabularySummaryResponse> listVocabulary(
            String level, String topic, Long studentId, Pageable pageable) {
        StudentUser.JlptLevel jlptLevel = parseLevel(level);

        Page<Vocabulary> page = StringUtils.hasText(topic)
                ? vocabularyRepository.findByJlptLevelAndStatusAndTopic(
                        jlptLevel, Kanji.ContentStatus.PUBLISHED, topic, pageable)
                : vocabularyRepository.findByJlptLevelAndStatus(
                        jlptLevel, Kanji.ContentStatus.PUBLISHED, pageable);

        List<Long> vocabIds = page.getContent().stream().map(Vocabulary::getId).toList();
        Map<Long, StudentContentProgress> progressMap = vocabIds.isEmpty()
                ? Map.of()
                : progressRepository
                        .findByStudent_IdAndContentTypeAndContentIdIn(
                                studentId, StudentContentProgress.ContentType.VOCABULARY, vocabIds)
                        .stream()
                        .collect(Collectors.toMap(StudentContentProgress::getContentId, Function.identity()));

        return page.map(v -> toSummary(v, progressMap.get(v.getId())));
    }

    @Transactional(readOnly = true)
    public VocabularyProgressSummaryResponse getProgressSummary(String level, Long studentId) {
        StudentUser.JlptLevel jlptLevel = parseLevel(level);

        List<Long> vocabIds =
                vocabularyRepository.findIdsByJlptLevelAndStatus(jlptLevel, Kanji.ContentStatus.PUBLISHED);
        long completed = vocabIds.isEmpty()
                ? 0
                : progressRepository.countByStudent_IdAndContentTypeAndContentIdInAndStatus(
                        studentId,
                        StudentContentProgress.ContentType.VOCABULARY,
                        vocabIds,
                        StudentContentProgress.ProgressStatus.COMPLETED);

        return VocabularyProgressSummaryResponse.builder()
                .jlptLevel(jlptLevel.name())
                .completed(completed)
                .total(vocabIds.size())
                .build();
    }

    @Transactional(readOnly = true)
    public List<String> getAvailableTopics(String level) {
        StudentUser.JlptLevel jlptLevel = parseLevel(level);
        return vocabularyRepository.findDistinctTopicsByJlptLevelAndStatus(
                jlptLevel, Kanji.ContentStatus.PUBLISHED);
    }

    @Transactional
    public VocabularyDetailResponse getVocabularyDetail(Long vocabularyId, Long studentId) {
        Vocabulary vocabulary = vocabularyRepository
                .findByIdAndStatus(vocabularyId, Kanji.ContentStatus.PUBLISHED)
                .orElseThrow(() -> {
                    log.warn(
                            "[VocabularyService] Truy cập nội dung không tồn tại"
                                    + " {{studentId={}, vocabularyId={}}}",
                            studentId,
                            vocabularyId);
                    return LearningException.contentNotFound();
                });

        StudentContentProgress progress = progressRepository
                .findByStudent_IdAndContentTypeAndContentId(
                        studentId, StudentContentProgress.ContentType.VOCABULARY, vocabularyId)
                .orElse(null);

        log.info(
                "[VocabularyService] Truy cập nội dung {{studentId={}, contentType=vocabulary, contentId={}}}",
                studentId,
                vocabularyId);
        touchLastActivity(studentId);

        return toDetail(vocabulary, progress);
    }

    private void touchLastActivity(Long studentId) {
        studentUserRepository.findById(studentId).ifPresent(student -> {
            student.setLastActivityDate(LocalDate.now());
            studentUserRepository.save(student);
        });
    }

    private StudentUser.JlptLevel parseLevel(String level) {
        try {
            return StudentUser.JlptLevel.valueOf(level);
        } catch (IllegalArgumentException | NullPointerException ex) {
            throw LearningException.levelMismatch();
        }
    }

    private VocabularySummaryResponse toSummary(Vocabulary v, StudentContentProgress progress) {
        return VocabularySummaryResponse.builder()
                .vocabularyId(v.getId())
                .word(v.getWord())
                .furigana(v.getFurigana())
                .meaning(v.getMeaning())
                .wordType(v.getWordType())
                .jlptLevel(v.getJlptLevel().name())
                .topic(v.getTopic())
                .isCompleted(
                        progress != null && progress.getStatus() == StudentContentProgress.ProgressStatus.COMPLETED)
                .build();
    }

    private VocabularyDetailResponse toDetail(Vocabulary v, StudentContentProgress progress) {
        return VocabularyDetailResponse.builder()
                .vocabularyId(v.getId())
                .word(v.getWord())
                .furigana(v.getFurigana())
                .meaning(v.getMeaning())
                .wordType(v.getWordType())
                .jlptLevel(v.getJlptLevel().name())
                .topic(v.getTopic())
                .audioUrl(v.getAudioUrl())
                .exampleSentenceJp(v.getExampleSentenceJp())
                .exampleSentenceVi(v.getExampleSentenceVi())
                .progressStatus(progress != null ? progress.getStatus().getValue() : null)
                .build();
    }
}
