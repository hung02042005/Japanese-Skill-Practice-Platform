/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.contentreview.handler;

import com.jlpt.feature.contentreview.model.ContentSnapshot;
import com.jlpt.feature.contentreview.model.ContentType;
import com.jlpt.feature.contentreview.repository.ReviewGrammarRepository;
import com.jlpt.feature.learning.GrammarPoint;
import com.jlpt.feature.learning.Kanji.ContentStatus;
import com.jlpt.feature.staff.StaffUser;
import com.jlpt.feature.student.StudentUser.JlptLevel;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** UC-33 — Handler kiểm duyệt cho {@code grammar_points}. */
@Component
@RequiredArgsConstructor
public class GrammarContentHandler implements ReviewableContentHandler {

    private final ReviewGrammarRepository repository;

    @Override
    public ContentType type() {
        return ContentType.GRAMMAR;
    }

    @Override
    public String tableName() {
        return "grammar_points";
    }

    @Override
    public List<ContentSnapshot> findPending(JlptLevel level) {
        List<GrammarPoint> grammarPoints = (level == null)
                ? repository.findPending(ContentStatus.PENDING_REVIEW)
                : repository.findPending(ContentStatus.PENDING_REVIEW, level);
        return grammarPoints.stream()
                .map(grammarPoint -> toSnapshot(grammarPoint, false))
                .toList();
    }

    @Override
    public Optional<ContentSnapshot> findActiveById(Long contentId) {
        return repository
                .findActiveById(contentId, ContentStatus.DELETED)
                .map(grammarPoint -> toSnapshot(grammarPoint, true));
    }

    @Override
    public int approve(Long contentId, StaffUser manager, LocalDateTime now) {
        return repository.approve(contentId, manager, now, ContentStatus.PENDING_REVIEW, ContentStatus.PUBLISHED);
    }

    @Override
    public int transitionFromPending(Long contentId, String targetStatus, LocalDateTime now) {
        ContentStatus targetContentStatus = HandlerSupport.toEnum(ContentStatus.class, targetStatus);
        return repository.transition(contentId, now, ContentStatus.PENDING_REVIEW, targetContentStatus);
    }

    private ContentSnapshot toSnapshot(GrammarPoint grammarPoint, boolean withDetail) {
        Map<String, Object> detail = null;
        if (withDetail) {
            detail = HandlerSupport.newDetail();
            HandlerSupport.put(detail, "structure", grammarPoint.getStructure());
            HandlerSupport.put(detail, "formula", grammarPoint.getFormula());
            HandlerSupport.put(detail, "meaning", grammarPoint.getMeaning());
            HandlerSupport.put(detail, "usageExplanation", grammarPoint.getUsageExplanation());
            HandlerSupport.put(detail, "exampleSentenceJp", grammarPoint.getExampleSentenceJp());
            HandlerSupport.put(detail, "exampleSentenceVi", grammarPoint.getExampleSentenceVi());
        }
        String titleOrText = grammarPoint.getTitle() != null ? grammarPoint.getTitle() : grammarPoint.getStructure();
        return ContentSnapshot.builder()
                .contentId(grammarPoint.getId())
                .contentType(ContentType.GRAMMAR)
                .titleOrText(titleOrText)
                .jlptLevel(
                        grammarPoint.getJlptLevel() != null
                                ? grammarPoint.getJlptLevel().name()
                                : null)
                .status(
                        grammarPoint.getStatus() != null
                                ? grammarPoint.getStatus().getValue()
                                : null)
                .createdById(HandlerSupport.creatorId(grammarPoint.getCreatedBy()))
                .createdByName(HandlerSupport.creatorName(grammarPoint.getCreatedBy()))
                .submittedAt(grammarPoint.getUpdatedAt())
                .detail(detail)
                .build();
    }
}
