/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.publishedcontent.handler;

import com.jlpt.feature.contentreview.model.ContentType;
import com.jlpt.feature.learning.GrammarPoint;
import com.jlpt.feature.learning.Kanji.ContentStatus;
import com.jlpt.feature.publishedcontent.model.ManagedContentSnapshot;
import com.jlpt.feature.publishedcontent.model.TargetStatus;
import com.jlpt.feature.publishedcontent.dto.ReferenceItemResponse;
import com.jlpt.feature.publishedcontent.repository.ManagedGrammarRepository;
import com.jlpt.feature.student.StudentUser.JlptLevel;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** UC-34 — Handler quản lý trạng thái cho {@code grammar_points}. */
@Component
@RequiredArgsConstructor
public class GrammarManagedHandler implements ManagedContentHandler {

    private final ManagedGrammarRepository repository;

    @Override
    public ContentType type() {
        return ContentType.GRAMMAR;
    }

    @Override
    public String tableName() {
        return "grammar_points";
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
        return List.of();
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

    private ManagedContentSnapshot toSnapshot(GrammarPoint grammarPoint) {
        String title = grammarPoint.getTitle() != null ? grammarPoint.getTitle() : grammarPoint.getStructure();
        return ManagedContentSnapshot.builder()
                .contentId(grammarPoint.getId())
                .contentType(ContentType.GRAMMAR)
                .titleOrText(title)
                .jlptLevel(grammarPoint.getJlptLevel() != null ? grammarPoint.getJlptLevel().name() : null)
                .status(grammarPoint.getStatus() != null ? grammarPoint.getStatus().getValue() : null)
                .publishedAt(grammarPoint.getPublishedAt())
                .build();
    }
}
