/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.publishedcontent.handler;

import com.jlpt.feature.contentreview.ContentType;
import com.jlpt.feature.learning.Kanji.ContentStatus;
import com.jlpt.feature.learning.Vocabulary;
import com.jlpt.feature.publishedcontent.ManagedContentSnapshot;
import com.jlpt.feature.publishedcontent.TargetStatus;
import com.jlpt.feature.publishedcontent.dto.ReferenceItemResponse;
import com.jlpt.feature.publishedcontent.repository.ManagedVocabularyRepository;
import com.jlpt.feature.student.StudentUser.JlptLevel;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** UC-34 — Handler quản lý trạng thái cho {@code vocabulary}. */
@Component
@RequiredArgsConstructor
public class VocabularyManagedHandler implements ManagedContentHandler {

    private final ManagedVocabularyRepository repository;

    @Override
    public ContentType type() {
        return ContentType.VOCABULARY;
    }

    @Override
    public String tableName() {
        return "vocabulary";
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
    public int changeStatus(Long contentId, TargetStatus target, LocalDateTime now) {
        ContentStatus to =
                switch (target) {
                    case UNPUBLISHED -> ContentStatus.DRAFT;
                    case ARCHIVED -> ContentStatus.ARCHIVED;
                    case DELETED -> ContentStatus.DELETED;
                };
        return repository.transition(contentId, ContentStatus.PUBLISHED, to, now);
    }

    @Override
    public int restore(Long contentId, LocalDateTime now) {
        return repository.transition(contentId, ContentStatus.ARCHIVED, ContentStatus.PUBLISHED, now);
    }

    private ManagedContentSnapshot toSnapshot(Vocabulary v) {
        return ManagedContentSnapshot.builder()
                .contentId(v.getId())
                .contentType(ContentType.VOCABULARY)
                .titleOrText(v.getWord())
                .jlptLevel(v.getJlptLevel() != null ? v.getJlptLevel().name() : null)
                .status(v.getStatus() != null ? v.getStatus().getValue() : null)
                .publishedAt(v.getPublishedAt())
                .build();
    }
}
