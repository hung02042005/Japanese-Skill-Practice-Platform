package com.jlpt.feature.student.vocabulary;

import com.jlpt.feature.learning.Kanji;
import com.jlpt.feature.learning.Vocabulary;
import com.jlpt.feature.student.StudentContentProgress;
import com.jlpt.feature.student.StudentContentProgress.ContentType;
import com.jlpt.feature.student.StudentContentProgress.ProgressStatus;
import com.jlpt.feature.student.StudentUser.JlptLevel;
import com.jlpt.feature.student.StudentUserRepository;
import com.jlpt.feature.student.flashcard.StudentFlashcardRepository;
import com.jlpt.feature.student.progress.StudentContentProgressRepository;
import com.jlpt.feature.student.vocabulary.dto.VocabularyDetailResponse;
import com.jlpt.feature.student.vocabulary.dto.VocabularyItemResponse;
import com.jlpt.feature.student.vocabulary.dto.VocabularyListResponse;
import com.jlpt.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class StudentVocabularyServiceImpl implements StudentVocabularyService {

    private final StudentVocabularyRepository vocabularyRepository;
    private final StudentContentProgressRepository progressRepository;
    private final StudentFlashcardRepository flashcardRepository;
    private final StudentUserRepository studentUserRepository;

    @Override
    @Transactional(readOnly = true)
    public VocabularyListResponse getVocabularyList(String level, String topic, Long studentId, int page, int size) {
        JlptLevel jlptLevel = null;
        if (level != null && !level.trim().isEmpty()) {
            try {
                jlptLevel = JlptLevel.valueOf(level.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid JLPT level: " + level);
            }
        }

        String queryTopic = (topic != null && !topic.trim().isEmpty()) ? topic : null;

        Page<Vocabulary> vocabularyPage = vocabularyRepository.findByFilters(
                jlptLevel, queryTopic, Kanji.ContentStatus.PUBLISHED, PageRequest.of(page, size));

        List<Long> vocabularyIds = vocabularyPage.getContent().stream()
                .map(Vocabulary::getId)
                .collect(Collectors.toList());

        List<StudentContentProgress> progresses = new java.util.ArrayList<>();
        if (!vocabularyIds.isEmpty()) {
            progresses = progressRepository.findByStudentIdAndContentTypeAndContentIdIn(
                    studentId, ContentType.VOCABULARY, vocabularyIds);
        }

        Map<Long, Boolean> completionMap = progresses.stream()
                .collect(Collectors.toMap(
                        StudentContentProgress::getContentId,
                        p -> p.getStatus() == ProgressStatus.COMPLETED
                ));

        List<VocabularyItemResponse> items = vocabularyPage.getContent().stream()
                .map(v -> VocabularyItemResponse.builder()
                        .id(v.getId())
                        .word(v.getWord())
                        .furigana(v.getFurigana())
                        .meaning(v.getMeaning())
                        .wordType(v.getWordType())
                        .jlptLevel(v.getJlptLevel() != null ? v.getJlptLevel().name() : null)
                        .topic(v.getTopic())
                        .isCompleted(completionMap.getOrDefault(v.getId(), false))
                        .isInFlashcard(false) // For simplicity without N+1 queries, left false in list API
                        .build())
                .collect(Collectors.toList());

        long completedCount = 0;
        if (jlptLevel != null) {
            completedCount = vocabularyRepository.countCompletedVocabularyByLevel(studentId, jlptLevel);
        }

        return VocabularyListResponse.builder()
                .content(items)
                .totalPages(vocabularyPage.getTotalPages())
                .totalElements(vocabularyPage.getTotalElements())
                .page(vocabularyPage.getNumber())
                .size(vocabularyPage.getSize())
                .completedCount(completedCount)
                .build();
    }

    @Override
    @Transactional
    public VocabularyDetailResponse getVocabularyDetail(Long id, Long studentId) {
        Vocabulary vocabulary = vocabularyRepository.findByIdAndStatus(id, Kanji.ContentStatus.PUBLISHED)
                .orElseThrow(() -> new ResourceNotFoundException("Vocabulary", id));

        // Update last activity date (BR-09-06)
        studentUserRepository.findById(studentId).ifPresent(user -> {
            user.setLastActivityDate(LocalDate.now());
            studentUserRepository.save(user);
        });

        StudentContentProgress progress = progressRepository.findByStudentIdAndContentTypeAndContentId(
                studentId, ContentType.VOCABULARY, id).orElse(null);

        String progressStatusStr = progress != null ? progress.getStatus().getValue() : "learning";
        boolean isCompleted = progress != null && progress.getStatus() == ProgressStatus.COMPLETED;

        boolean isInFlashcard = flashcardRepository.findByStudentIdAndContentTypeAndContentId(
                studentId, com.jlpt.feature.learning.Flashcard.ContentType.VOCABULARY, id).isPresent();

        Long prevId = vocabularyRepository.findFirstByJlptLevelAndTopicAndStatusAndIdLessThanOrderByIdDesc(
                vocabulary.getJlptLevel(), vocabulary.getTopic(), Kanji.ContentStatus.PUBLISHED, id)
                .map(Vocabulary::getId).orElse(null);
        Long nextId = vocabularyRepository.findFirstByJlptLevelAndTopicAndStatusAndIdGreaterThanOrderByIdAsc(
                vocabulary.getJlptLevel(), vocabulary.getTopic(), Kanji.ContentStatus.PUBLISHED, id)
                .map(Vocabulary::getId).orElse(null);

        return VocabularyDetailResponse.builder()
                .id(vocabulary.getId())
                .word(vocabulary.getWord())
                .furigana(vocabulary.getFurigana())
                .meaning(vocabulary.getMeaning())
                .wordType(vocabulary.getWordType())
                .jlptLevel(vocabulary.getJlptLevel() != null ? vocabulary.getJlptLevel().name() : null)
                .topic(vocabulary.getTopic())
                .audioUrl(vocabulary.getAudioUrl())
                .exampleSentenceJp(vocabulary.getExampleSentenceJp())
                .exampleSentenceVi(vocabulary.getExampleSentenceVi())
                .isCompleted(isCompleted)
                .isInFlashcard(isInFlashcard)
                .progressStatus(progressStatusStr)
                .prevVocabularyId(prevId)
                .nextVocabularyId(nextId)
                .build();
    }
}
