/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.student.kanji;

import com.jlpt.feature.flashcard.Flashcard;
import com.jlpt.feature.flashcard.repository.FlashcardRepository;
import com.jlpt.feature.learning.Kanji;
import com.jlpt.feature.learning.Kanji.ContentStatus;
import com.jlpt.feature.student.StudentContentProgress;
import com.jlpt.feature.student.StudentContentProgress.ContentType;
import com.jlpt.feature.student.StudentContentProgressRepository;
import com.jlpt.feature.student.StudentUser.JlptLevel;
import com.jlpt.feature.student.StudentUserRepository;
import com.jlpt.feature.student.kanji.dto.KanjiDetailResponse;
import com.jlpt.feature.student.kanji.dto.KanjiItemResponse;
import com.jlpt.feature.student.kanji.dto.KanjiListResponse;
import com.jlpt.feature.student.kanji.dto.KanjiProgressSummaryResponse;
import com.jlpt.shared.common.JlptLevels;
import com.jlpt.shared.exception.ResourceNotFoundException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class StudentKanjiServiceImpl implements StudentKanjiService {

    private final StudentKanjiRepository kanjiRepository;
    private final StudentContentProgressRepository progressRepository;
    private final FlashcardRepository flashcardRepository;
    private final StudentUserRepository studentUserRepository;

    @Override
    @Transactional(readOnly = true)
    public KanjiListResponse getKanjiList(String level, Long studentId, int page, int size) {
        JlptLevel jlptLevel;
        try {
            jlptLevel = JlptLevel.valueOf(level.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid JLPT level: " + level);
        }

        Page<Kanji> kanjiPage =
                kanjiRepository.findByLevelAndStatus(jlptLevel, ContentStatus.PUBLISHED, PageRequest.of(page, size));

        List<Long> kanjiIds = kanjiPage.getContent().stream().map(Kanji::getId).collect(Collectors.toList());

        List<StudentContentProgress> progresses = new java.util.ArrayList<>();
        if (!kanjiIds.isEmpty()) {
            progresses = progressRepository.findByStudentIdAndContentTypeAndContentIdIn(
                    studentId, ContentType.KANJI, kanjiIds);
        }

        Map<Long, Boolean> completionMap = progresses.stream()
                .collect(Collectors.toMap(
                        StudentContentProgress::getContentId,
                        p -> p.getStatus() == StudentContentProgress.ProgressStatus.COMPLETED));

        // isInFlashcard cho danh sách: tránh N+1, truy 1 lần tập contentId rồi tra trong Set.
        java.util.Set<Long> inFlashcardIds = new java.util.HashSet<>();
        if (!kanjiIds.isEmpty()) {
            flashcardRepository
                    .findByStudentAndContentIds(studentId, Flashcard.ContentType.KANJI, kanjiIds)
                    .forEach(f -> inFlashcardIds.add(f.getContentId()));
        }

        List<KanjiItemResponse> items = kanjiPage.getContent().stream()
                .map(k -> KanjiItemResponse.builder()
                        .kanjiId(k.getId())
                        .characterValue(k.getCharacterValue())
                        .meaning(k.getMeaning())
                        .onyomi(k.getOnyomi())
                        .kunyomi(k.getKunyomi())
                        .strokeCount(k.getStrokeCount())
                        .jlptLevel(k.getJlptLevel() != null ? k.getJlptLevel().name() : null)
                        .isCompleted(completionMap.getOrDefault(k.getId(), false))
                        .isInFlashcard(inFlashcardIds.contains(k.getId()))
                        .build())
                .collect(Collectors.toList());

        // Count total completed items for this level, not just current page
        long completedCount = progressRepository.countCompletedKanjiByLevel(
                studentId, jlptLevel, ContentType.KANJI, StudentContentProgress.ProgressStatus.COMPLETED);

        return KanjiListResponse.builder()
                .content(items)
                .totalPages(kanjiPage.getTotalPages())
                .totalElements(kanjiPage.getTotalElements())
                .page(kanjiPage.getNumber())
                .size(kanjiPage.getSize())
                .completedCount(completedCount)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public KanjiProgressSummaryResponse getProgressSummary(String level, Long studentId) {
        JlptLevel jlptLevel = JlptLevels.parseRequired(level);
        long completed = progressRepository.countCompletedKanjiByLevel(
                studentId, jlptLevel, ContentType.KANJI, StudentContentProgress.ProgressStatus.COMPLETED);
        long total = kanjiRepository.countByLevelAndStatus(jlptLevel, ContentStatus.PUBLISHED);
        return KanjiProgressSummaryResponse.builder()
                .completed(completed)
                .total(total)
                .build();
    }

    @Override
    @Transactional
    public KanjiDetailResponse getKanjiDetail(Long kanjiId, Long studentId) {
        Kanji kanji = kanjiRepository
                .findByIdAndStatus(kanjiId, ContentStatus.PUBLISHED)
                .orElseThrow(() -> new ResourceNotFoundException("Kanji", kanjiId));

        // Update last activity date
        studentUserRepository.findById(studentId).ifPresent(user -> {
            user.setLastActivityDate(LocalDate.now());
            studentUserRepository.save(user);
        });

        StudentContentProgress progress = progressRepository
                .findByStudentIdAndContentTypeAndContentId(studentId, ContentType.KANJI, kanjiId)
                .orElse(null);

        String progressStatusStr = progress != null ? progress.getStatus().getValue() : "learning";
        boolean isCompleted =
                progress != null && progress.getStatus() == StudentContentProgress.ProgressStatus.COMPLETED;

        boolean isInFlashcard = flashcardRepository
                .findByStudentAndContent(studentId, Flashcard.ContentType.KANJI, kanjiId)
                .isPresent();

        Long prevKanjiId = kanjiRepository
                .findFirstByJlptLevelAndStatusAndIdLessThanOrderByIdDesc(
                        kanji.getJlptLevel(), ContentStatus.PUBLISHED, kanjiId)
                .map(Kanji::getId)
                .orElse(null);
        Long nextKanjiId = kanjiRepository
                .findFirstByJlptLevelAndStatusAndIdGreaterThanOrderByIdAsc(
                        kanji.getJlptLevel(), ContentStatus.PUBLISHED, kanjiId)
                .map(Kanji::getId)
                .orElse(null);

        return KanjiDetailResponse.builder()
                .kanjiId(kanji.getId())
                .characterValue(kanji.getCharacterValue())
                .strokeCount(kanji.getStrokeCount())
                .strokeOrderUrl(kanji.getStrokeOrderUrl())
                .onyomi(kanji.getOnyomi())
                .kunyomi(kanji.getKunyomi())
                .meaning(kanji.getMeaning())
                .jlptLevel(kanji.getJlptLevel() != null ? kanji.getJlptLevel().name() : null)
                .exampleWord(kanji.getExampleWord())
                .exampleReading(kanji.getExampleReading())
                .exampleMeaning(kanji.getExampleMeaning())
                .isCompleted(isCompleted)
                .isInFlashcard(isInFlashcard)
                .progressStatus(progressStatusStr)
                .prevKanjiId(prevKanjiId)
                .nextKanjiId(nextKanjiId)
                .build();
    }
}
