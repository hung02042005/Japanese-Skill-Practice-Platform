/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.contentreview.handler;

import com.jlpt.feature.contentreview.ContentSnapshot;
import com.jlpt.feature.contentreview.ContentType;
import com.jlpt.feature.contentreview.repository.ReviewKanjiRepository;
import com.jlpt.feature.learning.Kanji;
import com.jlpt.feature.learning.Kanji.ContentStatus;
import com.jlpt.feature.staff.StaffUser;
import com.jlpt.feature.student.StudentUser.JlptLevel;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** UC-33 — Handler kiểm duyệt cho {@code kanji}. */
@Component
@RequiredArgsConstructor
public class KanjiContentHandler implements ReviewableContentHandler {

    private final ReviewKanjiRepository repository;

    @Override
    public ContentType type() {
        return ContentType.KANJI;
    }

    @Override
    public String tableName() {
        return "kanji";
    }

    @Override
    public List<ContentSnapshot> findPending(JlptLevel level) {
        List<Kanji> list = (level == null)
                ? repository.findPending(ContentStatus.PENDING_REVIEW)
                : repository.findPending(ContentStatus.PENDING_REVIEW, level);
        return list.stream().map(k -> toSnapshot(k, false)).toList();
    }

    @Override
    public Optional<ContentSnapshot> findActiveById(Long contentId) {
        return repository.findActiveById(contentId, ContentStatus.DELETED).map(k -> toSnapshot(k, true));
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

    private ContentSnapshot toSnapshot(Kanji k, boolean withDetail) {
        Map<String, Object> detail = null;
        if (withDetail) {
            detail = HandlerSupport.newDetail();
            HandlerSupport.put(detail, "characterValue", k.getCharacterValue());
            HandlerSupport.put(detail, "meaning", k.getMeaning());
            HandlerSupport.put(detail, "onyomi", k.getOnyomi());
            HandlerSupport.put(detail, "kunyomi", k.getKunyomi());
            HandlerSupport.put(detail, "strokeCount", k.getStrokeCount());
            HandlerSupport.put(detail, "exampleWord", k.getExampleWord());
            HandlerSupport.put(detail, "exampleReading", k.getExampleReading());
            HandlerSupport.put(detail, "exampleMeaning", k.getExampleMeaning());
        }
        return ContentSnapshot.builder()
                .contentId(k.getId())
                .contentType(ContentType.KANJI)
                .titleOrText(k.getCharacterValue())
                .jlptLevel(k.getJlptLevel() != null ? k.getJlptLevel().name() : null)
                .status(k.getStatus() != null ? k.getStatus().getValue() : null)
                .createdById(HandlerSupport.creatorId(k.getCreatedBy()))
                .createdByName(HandlerSupport.creatorName(k.getCreatedBy()))
                .submittedAt(k.getUpdatedAt())
                .detail(detail)
                .build();
    }
}
