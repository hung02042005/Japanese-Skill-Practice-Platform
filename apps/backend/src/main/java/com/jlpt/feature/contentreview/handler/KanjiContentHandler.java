/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.contentreview.handler;

import com.jlpt.feature.contentreview.model.ContentSnapshot;
import com.jlpt.feature.contentreview.model.ContentType;
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
        List<Kanji> kanjiEntries = (level == null)
                ? repository.findPending(ContentStatus.PENDING_REVIEW)
                : repository.findPending(ContentStatus.PENDING_REVIEW, level);
        return kanjiEntries.stream().map(kanji -> toSnapshot(kanji, false)).toList();
    }

    @Override
    public Optional<ContentSnapshot> findActiveById(Long contentId) {
        return repository.findActiveById(contentId, ContentStatus.DELETED).map(kanji -> toSnapshot(kanji, true));
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

    private ContentSnapshot toSnapshot(Kanji kanji, boolean withDetail) {
        Map<String, Object> detail = null;
        if (withDetail) {
            detail = HandlerSupport.newDetail();
            HandlerSupport.put(detail, "characterValue", kanji.getCharacterValue());
            HandlerSupport.put(detail, "meaning", kanji.getMeaning());
            HandlerSupport.put(detail, "onyomi", kanji.getOnyomi());
            HandlerSupport.put(detail, "kunyomi", kanji.getKunyomi());
            HandlerSupport.put(detail, "strokeCount", kanji.getStrokeCount());
            HandlerSupport.put(detail, "exampleWord", kanji.getExampleWord());
            HandlerSupport.put(detail, "exampleReading", kanji.getExampleReading());
            HandlerSupport.put(detail, "exampleMeaning", kanji.getExampleMeaning());
        }
        return ContentSnapshot.builder()
                .contentId(kanji.getId())
                .contentType(ContentType.KANJI)
                .titleOrText(kanji.getCharacterValue())
                .jlptLevel(kanji.getJlptLevel() != null ? kanji.getJlptLevel().name() : null)
                .status(kanji.getStatus() != null ? kanji.getStatus().getValue() : null)
                .createdById(HandlerSupport.creatorId(kanji.getCreatedBy()))
                .createdByName(HandlerSupport.creatorName(kanji.getCreatedBy()))
                .submittedAt(kanji.getUpdatedAt())
                .detail(detail)
                .build();
    }
}
