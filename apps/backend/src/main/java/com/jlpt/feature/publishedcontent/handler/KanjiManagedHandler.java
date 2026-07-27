/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.publishedcontent.handler;

import com.jlpt.feature.contentreview.model.ContentType;
import com.jlpt.feature.learning.Kanji;
import com.jlpt.feature.learning.Kanji.ContentStatus;
import com.jlpt.feature.publishedcontent.dto.ReferenceItemResponse;
import com.jlpt.feature.publishedcontent.model.ManagedContentSnapshot;
import com.jlpt.feature.publishedcontent.model.TargetStatus;
import com.jlpt.feature.publishedcontent.repository.ManagedKanjiRepository;
import com.jlpt.feature.student.StudentUser.JlptLevel;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** UC-34 — Handler quản lý trạng thái cho {@code kanji}. */
@Component
@RequiredArgsConstructor
public class KanjiManagedHandler implements ManagedContentHandler {

    private final ManagedKanjiRepository repository;

    @Override
    public ContentType type() {
        return ContentType.KANJI;
    }

    @Override
    public String tableName() {
        return "kanji";
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

    private ManagedContentSnapshot toSnapshot(Kanji kanji) {
        return ManagedContentSnapshot.builder()
                .contentId(kanji.getId())
                .contentType(ContentType.KANJI)
                .titleOrText(kanji.getCharacterValue())
                .jlptLevel(kanji.getJlptLevel() != null ? kanji.getJlptLevel().name() : null)
                .status(kanji.getStatus() != null ? kanji.getStatus().getValue() : null)
                .publishedAt(kanji.getPublishedAt())
                .build();
    }
}
