/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.publishedcontent.handler;

import com.jlpt.feature.assessment.Assessment;
import com.jlpt.feature.contentreview.ContentType;
import com.jlpt.feature.learning.Kanji.ContentStatus;
import com.jlpt.feature.publishedcontent.ManagedContentSnapshot;
import com.jlpt.feature.publishedcontent.TargetStatus;
import com.jlpt.feature.publishedcontent.dto.ReferenceItemResponse;
import com.jlpt.feature.publishedcontent.repository.ManagedAssessmentRepository;
import com.jlpt.feature.student.StudentUser.JlptLevel;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** UC-34 — Handler quản lý trạng thái cho {@code assessments} (quiz/exam). */
@Component
@RequiredArgsConstructor
public class AssessmentManagedHandler implements ManagedContentHandler {

    private final ManagedAssessmentRepository repository;

    @Override
    public ContentType type() {
        return ContentType.ASSESSMENT;
    }

    @Override
    public String tableName() {
        return "assessments";
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
        // Không có ràng buộc tham chiếu chặn ẩn đề thi trong phạm vi UC-34 (spec §5.2).
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

    private ManagedContentSnapshot toSnapshot(Assessment a) {
        return ManagedContentSnapshot.builder()
                .contentId(a.getId())
                .contentType(ContentType.ASSESSMENT)
                .titleOrText(a.getTitle())
                .jlptLevel(a.getJlptLevel() != null ? a.getJlptLevel().name() : null)
                .status(a.getStatus() != null ? a.getStatus().getValue() : null)
                .publishedAt(a.getPublishedAt())
                .build();
    }
}
