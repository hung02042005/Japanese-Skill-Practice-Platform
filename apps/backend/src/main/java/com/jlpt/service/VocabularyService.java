/* (c) JLPT E-Learning Platform */
package com.jlpt.service;

import com.jlpt.dto.response.VocabularyLevelResponse;
import com.jlpt.dto.response.VocabularyPageResponse;
import com.jlpt.dto.response.VocabularyPageResponse.VocabularyItemResponse;
import com.jlpt.dto.response.VocabularyPathResponse;
import com.jlpt.entity.Flashcard;
import com.jlpt.entity.Kanji;
import com.jlpt.entity.StudentContentProgress;
import com.jlpt.entity.StudentContentProgress.ContentType;
import com.jlpt.entity.StudentContentProgress.ProgressStatus;
import com.jlpt.entity.StudentUser;
import com.jlpt.entity.Vocabulary;
import com.jlpt.entity.VocabularyTopic;
import com.jlpt.exception.ResourceNotFoundException;
import com.jlpt.flashcard.repository.FlashcardRepository;
import com.jlpt.repository.StudentContentProgressRepository;
import com.jlpt.repository.StudentUserRepository;
import com.jlpt.repository.VocabularyRepository;
import com.jlpt.repository.VocabularyTopicRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class VocabularyService {

    private final VocabularyRepository vocabularyRepository;
    private final StudentContentProgressRepository progressRepository;
    private final StudentUserRepository studentUserRepository;
    private final FlashcardRepository flashcardRepository;
    private final VocabularyTopicRepository vocabularyTopicRepository;

    /** Lấy danh sách từ vựng đã publish, có phân trang + filter. */
    @Transactional(readOnly = true)
    public VocabularyPageResponse getVocabularyList(
            Long studentId, String levelStr, String topic, String search, Pageable pageable) {

        StudentUser.JlptLevel jlptLevel = parseLevel(levelStr);
        String q = (search != null && !search.isBlank()) ? search.trim() : null;
        String topicFilter = (topic != null && !topic.isBlank()) ? topic.trim() : null;

        Page<Vocabulary> page =
                vocabularyRepository.findPublished(Kanji.ContentStatus.PUBLISHED, jlptLevel, topicFilter, q, pageable);

        // DB query — lấy tập hợp ID đã hoàn thành và đã thêm flashcard
        Set<Long> completedIds =
                progressRepository.findCompletedContentIds(studentId, ContentType.VOCABULARY, ProgressStatus.COMPLETED);

        Set<Long> flashcardContentIds =
                flashcardRepository.findVocabContentIdsByStudent(studentId, Flashcard.ContentType.VOCABULARY);

        long completedCount =
                progressRepository.countCompleted(studentId, ContentType.VOCABULARY, ProgressStatus.COMPLETED);

        List<VocabularyItemResponse> items = page.getContent().stream()
                .map(v -> toItemResponse(v, completedIds, flashcardContentIds))
                .toList();

        return new VocabularyPageResponse(
                items, page.getTotalPages(), page.getTotalElements(), page.getNumber(), page.getSize(), completedCount);
    }

    /**
     * Liệt kê 5 bậc cấp độ JLPT (N5→N1) kèm số chủ đề & số từ đã publish — điều hướng học từ vựng (§3.7).
     * Trả đủ 5 bậc kể cả bậc rỗng để UI hiển thị đầy đủ "thang" cấp độ.
     */
    @Transactional(readOnly = true)
    public List<VocabularyLevelResponse> getLevels() {
        return Arrays.stream(StudentUser.JlptLevel.values())
                .map(level -> new VocabularyLevelResponse(
                        level.name(),
                        (int) vocabularyTopicRepository.countByJlptLevelAndStatus(level, Kanji.ContentStatus.PUBLISHED),
                        vocabularyRepository.countPublished(Kanji.ContentStatus.PUBLISHED, level)))
                .toList();
    }

    /** Lấy danh sách topic của một cấp JLPT. */
    @Transactional(readOnly = true)
    public List<VocabularyPathResponse> getVocabularyPath(Long studentId, String levelStr) {
        StudentUser.JlptLevel jlptLevel = parseLevel(levelStr);
        if (jlptLevel == null) {
            return List.of();
        }

        List<VocabularyTopic> topics = vocabularyTopicRepository.findByJlptLevelAndStatusOrderByDisplayOrderAsc(
                jlptLevel, Kanji.ContentStatus.PUBLISHED);

        boolean previousTopicsCompleted = true;
        List<VocabularyPathResponse> path = new ArrayList<>();
        for (VocabularyTopic topic : topics) {
            long totalWords = vocabularyRepository.countPublishedByTopic(Kanji.ContentStatus.PUBLISHED, topic.getId());
            long completedWords = progressRepository.countCompletedVocabularyInTopic(
                    studentId,
                    ContentType.VOCABULARY,
                    ProgressStatus.COMPLETED,
                    topic.getId(),
                    Kanji.ContentStatus.PUBLISHED);

            String pathStatus;
            if (totalWords > 0 && completedWords >= totalWords) {
                pathStatus = "completed";
            } else if (previousTopicsCompleted) {
                pathStatus = "active";
                previousTopicsCompleted = false;
            } else {
                pathStatus = "locked";
            }

            path.add(new VocabularyPathResponse(
                    topic.getId(),
                    topic.getSlug(),
                    topic.getTitleJa(),
                    topic.getTitleVi(),
                    topic.getDisplayOrder(),
                    topic.getJlptLevel().name(),
                    totalWords,
                    completedWords,
                    pathStatus));
        }
        return path;
    }

    @Transactional(readOnly = true)
    public List<String> getTopics(String levelStr) {
        StudentUser.JlptLevel jlptLevel = parseLevel(levelStr);
        if (jlptLevel == null) {
            return List.of();
        }
        List<String> topicTitles = vocabularyTopicRepository
                .findByJlptLevelAndStatusOrderByDisplayOrderAsc(jlptLevel, Kanji.ContentStatus.PUBLISHED)
                .stream()
                .map(VocabularyTopic::getTitleVi)
                .toList();
        if (!topicTitles.isEmpty()) {
            return topicTitles;
        }
        return vocabularyRepository.findDistinctTopics(Kanji.ContentStatus.PUBLISHED, jlptLevel);
    }

    /** Đánh dấu từ vựng đã hoàn thành (idempotent). */
    @Transactional
    public void markComplete(Long studentId, Long vocabId) {
        vocabularyRepository.findById(vocabId).orElseThrow(() -> new ResourceNotFoundException("Vocabulary", vocabId));

        StudentUser student = studentUserRepository
                .findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student", studentId));

        progressRepository
                .findByStudentAndContent(studentId, ContentType.VOCABULARY, vocabId)
                .ifPresentOrElse(
                        progress -> {
                            if (progress.getStatus() != ProgressStatus.COMPLETED) {
                                progress.setStatus(ProgressStatus.COMPLETED);
                                progress.setProgressPercent(BigDecimal.valueOf(100));
                                progress.setCompletedAt(LocalDateTime.now());
                                progress.setLastStudiedAt(LocalDateTime.now());
                                progressRepository.save(progress);
                                log.info("[VOCAB_COMPLETE] studentId={} vocabId={}", studentId, vocabId);
                            }
                        },
                        () -> {
                            StudentContentProgress newProgress = StudentContentProgress.builder()
                                    .student(student)
                                    .contentType(ContentType.VOCABULARY)
                                    .contentId(vocabId)
                                    .status(ProgressStatus.COMPLETED)
                                    .progressPercent(BigDecimal.valueOf(100))
                                    .completedAt(LocalDateTime.now())
                                    .build();
                            progressRepository.save(newProgress);
                            log.info("[VOCAB_COMPLETE] studentId={} vocabId={} (new)", studentId, vocabId);
                        });
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private VocabularyItemResponse toItemResponse(Vocabulary v, Set<Long> completedIds, Set<Long> flashcardContentIds) {
        return new VocabularyItemResponse(
                v.getId(),
                v.getWord(),
                v.getFurigana(),
                v.getMeaning(),
                v.getWordType(),
                v.getJlptLevel() != null ? v.getJlptLevel().name() : null,
                v.getTopic(),
                v.getAudioUrl(),
                v.getExampleSentenceJp(),
                v.getExampleSentenceVi(),
                completedIds.contains(v.getId()),
                flashcardContentIds.contains(v.getId()));
    }

    private StudentUser.JlptLevel parseLevel(String levelStr) {
        if (levelStr == null || levelStr.isBlank()) return null;
        try {
            return StudentUser.JlptLevel.valueOf(levelStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            log.warn("[VOCAB] Invalid JLPT level param: {}", levelStr);
            return null;
        }
    }
}
