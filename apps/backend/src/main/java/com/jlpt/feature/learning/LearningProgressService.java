/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.learning;

import com.jlpt.feature.learning.dto.request.MarkLearningProgressRequest;
import com.jlpt.feature.learning.dto.response.LearningProgressResponse;
import com.jlpt.feature.learning.exception.LearningException;
import com.jlpt.feature.learning.grammar.GrammarRepository;
import com.jlpt.feature.learning.kana.KanaRepository;
import com.jlpt.feature.learning.kanji.KanjiRepository;
import com.jlpt.feature.learning.vocabulary.VocabularyRepository;
import com.jlpt.feature.student.StudentContentProgress;
import com.jlpt.feature.student.StudentContentProgressRepository;
import com.jlpt.feature.student.StudentUserRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * UC-06/UC-07/UC-08/UC-09 — Đánh dấu/cập nhật tiến độ học.
 * Hỗ trợ {@code contentType} thuộc {@code grammar, kanji, kana, vocabulary}; loại khác bị từ chối VALIDATION_FAILED.
 */
@Service
@RequiredArgsConstructor
public class LearningProgressService {

    private static final Set<String> ALLOWED_STATUSES = Set.of("learning", "completed", "reviewing");
    private static final Set<String> SUPPORTED_CONTENT_TYPES = Set.of("grammar", "kanji", "kana", "vocabulary");

    private final GrammarRepository grammarRepository;
    private final KanjiRepository kanjiRepository;
    private final KanaRepository kanaRepository;
    private final VocabularyRepository vocabularyRepository;
    private final StudentContentProgressRepository progressRepository;
    private final StudentUserRepository studentUserRepository;

    @Transactional
    public LearningProgressResponse markProgress(MarkLearningProgressRequest request, Long studentId) {
        validate(request);

        if (!contentExists(request.getContentType(), request.getContentId())) {
            throw LearningException.contentNotFound();
        }

        StudentContentProgress.ContentType contentType =
                StudentContentProgress.ContentType.valueOf(request.getContentType().toUpperCase());
        StudentContentProgress.ProgressStatus newStatus = StudentContentProgress.ProgressStatus.valueOf(
                request.getStatus().toUpperCase());
        BigDecimal newPercent = BigDecimal.valueOf(request.getProgressPercent());

        StudentContentProgress progress = progressRepository
                .findByStudent_IdAndContentTypeAndContentId(studentId, contentType, request.getContentId())
                .orElse(null);

        if (progress != null && isRegression(progress, newStatus, newPercent)) {
            throw LearningException.progressRegression();
        }

        if (progress == null) {
            progress = StudentContentProgress.builder()
                    .student(studentUserRepository.getReferenceById(studentId))
                    .contentType(contentType)
                    .contentId(request.getContentId())
                    .build();
        }

        progress.setStatus(newStatus);
        progress.setProgressPercent(newPercent);
        progress.setLastStudiedAt(LocalDateTime.now());
        if (newStatus == StudentContentProgress.ProgressStatus.COMPLETED) {
            progress.setCompletedAt(LocalDateTime.now());
        }

        StudentContentProgress saved = progressRepository.save(progress);
        return toResponse(saved);
    }

    private void validate(MarkLearningProgressRequest request) {
        if (request.getContentType() == null || !SUPPORTED_CONTENT_TYPES.contains(request.getContentType())) {
            throw LearningException.validationFailed("contentType");
        }
        if (request.getContentId() == null) {
            throw LearningException.validationFailed("contentId");
        }
        if (request.getStatus() == null
                || !ALLOWED_STATUSES.contains(request.getStatus().toLowerCase())) {
            throw LearningException.validationFailed("status");
        }
        if (request.getProgressPercent() == null
                || request.getProgressPercent() < 0
                || request.getProgressPercent() > 100) {
            throw LearningException.validationFailed("progressPercent");
        }
    }

    private boolean contentExists(String contentType, Long contentId) {
        return switch (contentType) {
            case "grammar" -> grammarRepository.findByIdAndStatus(contentId, Kanji.ContentStatus.PUBLISHED).isPresent();
            case "kanji" -> kanjiRepository.findByIdAndStatus(contentId, Kanji.ContentStatus.PUBLISHED).isPresent();
            // BR-08-07: kana không có status/VIP — tồn tại là đủ điều kiện
            case "kana" -> kanaRepository.existsById(contentId.intValue());
            // BR-09-01: chỉ từ vựng published mới được đánh dấu tiến độ
            case "vocabulary" -> vocabularyRepository.findByIdAndStatus(contentId, Kanji.ContentStatus.PUBLISHED).isPresent();
            default -> false;
        };
    }

    /** BR-06-03 — progress_percent/status chỉ tăng, không giảm thủ công. */
    private boolean isRegression(
            StudentContentProgress existing, StudentContentProgress.ProgressStatus newStatus, BigDecimal newPercent) {
        if (newPercent.compareTo(existing.getProgressPercent()) < 0) {
            return true;
        }
        return existing.getStatus() == StudentContentProgress.ProgressStatus.COMPLETED
                && newStatus != StudentContentProgress.ProgressStatus.COMPLETED;
    }

    private LearningProgressResponse toResponse(StudentContentProgress progress) {
        return LearningProgressResponse.builder()
                .progressId(progress.getId())
                .contentType(progress.getContentType().getValue())
                .contentId(progress.getContentId())
                .status(progress.getStatus().getValue())
                .progressPercent(progress.getProgressPercent().intValue())
                .completedAt(progress.getCompletedAt())
                .build();
    }
}
