/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.learning.flashcard;

import com.jlpt.feature.learning.Flashcard;
import com.jlpt.feature.learning.Kanji;
import com.jlpt.feature.learning.exception.LearningException;
import com.jlpt.feature.learning.flashcard.dto.request.AddFlashcardRequest;
import com.jlpt.feature.learning.flashcard.dto.response.FlashcardResponse;
import com.jlpt.feature.learning.kanji.KanjiRepository;
import com.jlpt.feature.learning.vocabulary.VocabularyRepository;
import com.jlpt.feature.student.StudentUserRepository;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * UC-07/UC-09 — "Add to Flashcard" (FR-LEARN-13, FR-LEARN-33, BR-07-05, BR-09-05).
 * Hỗ trợ {@code contentType} thuộc {@code kanji, vocabulary}; loại khác bị từ chối VALIDATION_FAILED.
 */
@Service
@RequiredArgsConstructor
public class FlashcardService {

    private static final Set<String> SUPPORTED_CONTENT_TYPES = Set.of("kanji", "vocabulary");
    private static final String DEFAULT_DECK_NAME = "Mặc định";

    private final FlashcardRepository flashcardRepository;
    private final KanjiRepository kanjiRepository;
    private final VocabularyRepository vocabularyRepository;
    private final StudentUserRepository studentUserRepository;

    @Transactional
    public FlashcardResponse addToFlashcard(AddFlashcardRequest request, Long studentId) {
        validate(request);

        Flashcard.ContentType contentType = resolveContentType(request.getContentType());

        if (!contentExists(request.getContentType(), request.getContentId())) {
            throw LearningException.contentNotFound();
        }

        if (flashcardRepository.existsByStudent_IdAndContentTypeAndContentId(
                studentId, contentType, request.getContentId())) {
            throw LearningException.flashcardExists();
        }

        Flashcard flashcard = Flashcard.builder()
                .student(studentUserRepository.getReferenceById(studentId))
                .contentType(contentType)
                .contentId(request.getContentId())
                .deckName(StringUtils.hasText(request.getDeckName()) ? request.getDeckName() : DEFAULT_DECK_NAME)
                .build();

        Flashcard saved = flashcardRepository.save(flashcard);
        return toResponse(saved);
    }

    private void validate(AddFlashcardRequest request) {
        if (request.getContentType() == null
                || !SUPPORTED_CONTENT_TYPES.contains(request.getContentType())) {
            throw LearningException.validationFailed("contentType");
        }
        if (request.getContentId() == null) {
            throw LearningException.validationFailed("contentId");
        }
    }

    private Flashcard.ContentType resolveContentType(String contentType) {
        return switch (contentType) {
            case "kanji" -> Flashcard.ContentType.KANJI;
            case "vocabulary" -> Flashcard.ContentType.VOCABULARY;
            default -> throw LearningException.validationFailed("contentType");
        };
    }

    private boolean contentExists(String contentType, Long contentId) {
        return switch (contentType) {
            case "kanji" -> kanjiRepository
                    .findByIdAndStatus(contentId, Kanji.ContentStatus.PUBLISHED)
                    .isPresent();
            case "vocabulary" -> vocabularyRepository
                    .findByIdAndStatus(contentId, Kanji.ContentStatus.PUBLISHED)
                    .isPresent();
            default -> false;
        };
    }

    private FlashcardResponse toResponse(Flashcard flashcard) {
        return FlashcardResponse.builder()
                .flashcardId(flashcard.getId())
                .contentType(flashcard.getContentType().getValue())
                .contentId(flashcard.getContentId())
                .deckName(flashcard.getDeckName())
                .createdAt(flashcard.getCreatedAt())
                .build();
    }
}
