/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.publishedcontent.handler;

import com.jlpt.feature.assessment.Question;
import com.jlpt.feature.assessment.Question.ContentStatus;
import com.jlpt.feature.assessment.QuestionAssignment.ParentType;
import com.jlpt.feature.contentreview.model.ContentType;
import com.jlpt.feature.learning.Kanji;
import com.jlpt.feature.publishedcontent.dto.ReferenceItemResponse;
import com.jlpt.feature.publishedcontent.model.ManagedContentSnapshot;
import com.jlpt.feature.publishedcontent.model.TargetStatus;
import com.jlpt.feature.publishedcontent.repository.ManagedQuestionRepository;
import com.jlpt.feature.publishedcontent.repository.QuestionReferenceRepository;
import com.jlpt.feature.student.StudentUser.JlptLevel;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** UC-34 — Handler quản lý trạng thái cho {@code questions}. */
@Component
@RequiredArgsConstructor
public class QuestionManagedHandler implements ManagedContentHandler {

    private final ManagedQuestionRepository repository;
    private final QuestionReferenceRepository referenceRepository;

    @Override
    public ContentType type() {
        return ContentType.QUESTION;
    }

    @Override
    public String tableName() {
        return "questions";
    }

    @Override
    public List<ManagedContentSnapshot> findPublished(JlptLevel level) {
        return repository.findPublished(ContentStatus.PUBLISHED, level).stream()
                .map(this::toSnapshot)
                .toList();
    }

    @Override
    public Optional<ManagedContentSnapshot> findById(Long contentId) {
        return repository.findById(contentId).map(this::toSnapshot);
    }

    @Override
    public List<ReferenceItemResponse> findBlockingReferences(Long contentId) {
        // FR-34-14 — câu hỏi đang nằm trong đề thi published.
        return referenceRepository.findPublishedAssessmentsByQuestion(
                contentId, ParentType.ASSESSMENT, Kanji.ContentStatus.PUBLISHED);
    }

    @Override
    public int changeStatus(Long contentId, TargetStatus targetStatus, LocalDateTime changeTimestamp) {
        ContentStatus targetContentStatus =
                switch (targetStatus) {
                    case UNPUBLISHED -> ContentStatus.DRAFT;
                    case ARCHIVED -> ContentStatus.ARCHIVED;
                    case DELETED -> ContentStatus.DELETED;
                };
        return repository.transition(contentId, ContentStatus.PUBLISHED, targetContentStatus, changeTimestamp);
    }

    @Override
    public int restore(Long contentId, LocalDateTime now) {
        return repository.transition(contentId, ContentStatus.ARCHIVED, ContentStatus.PUBLISHED, now);
    }

    private ManagedContentSnapshot toSnapshot(Question question) {
        return ManagedContentSnapshot.builder()
                .contentId(question.getId())
                .contentType(ContentType.QUESTION)
                .titleOrText(question.getQuestionText())
                .jlptLevel(
                        question.getJlptLevel() != null
                                ? question.getJlptLevel().name()
                                : null)
                .status(question.getStatus() != null ? question.getStatus().getValue() : null)
                .publishedAt(question.getPublishedAt())
                .build();
    }
}
