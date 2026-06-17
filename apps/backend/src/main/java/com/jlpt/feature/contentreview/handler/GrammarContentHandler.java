/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.contentreview.handler;

import com.jlpt.feature.contentreview.ContentSnapshot;
import com.jlpt.feature.contentreview.ContentType;
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
        List<GrammarPoint> list = (level == null)
                ? repository.findPending(ContentStatus.PENDING_REVIEW)
                : repository.findPending(ContentStatus.PENDING_REVIEW, level);
        return list.stream().map(g -> toSnapshot(g, false)).toList();
    }

    @Override
    public Optional<ContentSnapshot> findActiveById(Long contentId) {
        return repository.findActiveById(contentId, ContentStatus.DELETED).map(g -> toSnapshot(g, true));
    }

    @Override
    public int approve(Long contentId, StaffUser manager, LocalDateTime now) {
        return repository.approve(contentId, manager, now, ContentStatus.PENDING_REVIEW, ContentStatus.PUBLISHED);
    }

    @Override
    public int transitionFromPending(Long contentId, String targetStatus, LocalDateTime now) {
        ContentStatus to = HandlerSupport.toEnum(ContentStatus.class, targetStatus);
        return repository.transition(contentId, now, ContentStatus.PENDING_REVIEW, to);
    }

    private ContentSnapshot toSnapshot(GrammarPoint g, boolean withDetail) {
        Map<String, Object> detail = null;
        if (withDetail) {
            detail = HandlerSupport.newDetail();
            HandlerSupport.put(detail, "structure", g.getStructure());
            HandlerSupport.put(detail, "formula", g.getFormula());
            HandlerSupport.put(detail, "meaning", g.getMeaning());
            HandlerSupport.put(detail, "usageExplanation", g.getUsageExplanation());
            HandlerSupport.put(detail, "exampleSentenceJp", g.getExampleSentenceJp());
            HandlerSupport.put(detail, "exampleSentenceVi", g.getExampleSentenceVi());
        }
        String titleOrText = g.getTitle() != null ? g.getTitle() : g.getStructure();
        return ContentSnapshot.builder()
                .contentId(g.getId())
                .contentType(ContentType.GRAMMAR)
                .titleOrText(titleOrText)
                .jlptLevel(g.getJlptLevel() != null ? g.getJlptLevel().name() : null)
                .status(g.getStatus() != null ? g.getStatus().getValue() : null)
                .createdById(HandlerSupport.creatorId(g.getCreatedBy()))
                .createdByName(HandlerSupport.creatorName(g.getCreatedBy()))
                .submittedAt(g.getUpdatedAt())
                .detail(detail)
                .build();
    }
}
