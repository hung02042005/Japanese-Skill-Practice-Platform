/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.contentreview.handler;

import com.jlpt.feature.contentreview.ContentSnapshot;
import com.jlpt.feature.contentreview.ContentType;
import com.jlpt.feature.contentreview.repository.ReviewVocabularyRepository;
import com.jlpt.feature.learning.Kanji.ContentStatus;
import com.jlpt.feature.learning.Vocabulary;
import com.jlpt.feature.staff.StaffUser;
import com.jlpt.feature.student.StudentUser.JlptLevel;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** UC-33 — Handler kiểm duyệt cho {@code vocabulary}. */
@Component
@RequiredArgsConstructor
public class VocabularyContentHandler implements ReviewableContentHandler {

    private final ReviewVocabularyRepository repository;

    @Override
    public ContentType type() {
        return ContentType.VOCABULARY;
    }

    @Override
    public String tableName() {
        return "vocabulary";
    }

    @Override
    public List<ContentSnapshot> findPending(JlptLevel level) {
        List<Vocabulary> list = (level == null)
                ? repository.findPending(ContentStatus.PENDING_REVIEW)
                : repository.findPending(ContentStatus.PENDING_REVIEW, level);
        return list.stream().map(v -> toSnapshot(v, false)).toList();
    }

    @Override
    public Optional<ContentSnapshot> findActiveById(Long contentId) {
        return repository.findActiveById(contentId, ContentStatus.DELETED).map(v -> toSnapshot(v, true));
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

    private ContentSnapshot toSnapshot(Vocabulary v, boolean withDetail) {
        Map<String, Object> detail = null;
        if (withDetail) {
            detail = HandlerSupport.newDetail();
            HandlerSupport.put(detail, "word", v.getWord());
            HandlerSupport.put(detail, "furigana", v.getFurigana());
            HandlerSupport.put(detail, "meaning", v.getMeaning());
            HandlerSupport.put(detail, "wordType", v.getWordType());
            HandlerSupport.put(detail, "topic", v.getTopic());
            HandlerSupport.put(detail, "exampleSentenceJp", v.getExampleSentenceJp());
            HandlerSupport.put(detail, "exampleSentenceVi", v.getExampleSentenceVi());
        }
        return ContentSnapshot.builder()
                .contentId(v.getId())
                .contentType(ContentType.VOCABULARY)
                .titleOrText(v.getWord())
                .jlptLevel(v.getJlptLevel() != null ? v.getJlptLevel().name() : null)
                .status(v.getStatus() != null ? v.getStatus().getValue() : null)
                .createdById(HandlerSupport.creatorId(v.getCreatedBy()))
                .createdByName(HandlerSupport.creatorName(v.getCreatedBy()))
                .submittedAt(v.getUpdatedAt())
                .detail(detail)
                .build();
    }
}
