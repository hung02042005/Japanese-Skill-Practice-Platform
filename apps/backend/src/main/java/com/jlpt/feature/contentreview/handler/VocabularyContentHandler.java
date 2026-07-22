/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.contentreview.handler;

import com.jlpt.feature.contentreview.model.ContentSnapshot;
import com.jlpt.feature.contentreview.model.ContentType;
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
        List<Vocabulary> vocabularyEntries = (level == null)
                ? repository.findPending(ContentStatus.PENDING_REVIEW)
                : repository.findPending(ContentStatus.PENDING_REVIEW, level);
        return vocabularyEntries.stream().map(vocabulary -> toSnapshot(vocabulary, false)).toList();
    }

    @Override
    public Optional<ContentSnapshot> findActiveById(Long contentId) {
        return repository
                .findActiveById(contentId, ContentStatus.DELETED)
                .map(vocabulary -> toSnapshot(vocabulary, true));
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

    private ContentSnapshot toSnapshot(Vocabulary vocabulary, boolean withDetail) {
        Map<String, Object> detail = null;
        if (withDetail) {
            detail = HandlerSupport.newDetail();
            HandlerSupport.put(detail, "word", vocabulary.getWord());
            HandlerSupport.put(detail, "furigana", vocabulary.getFurigana());
            HandlerSupport.put(detail, "meaning", vocabulary.getMeaning());
            HandlerSupport.put(detail, "wordType", vocabulary.getWordType());
            HandlerSupport.put(
                    detail,
                    "topic",
                    vocabulary.getTopicRef() != null ? vocabulary.getTopicRef().getTitleVi() : null);
            HandlerSupport.put(detail, "exampleSentenceJp", vocabulary.getExampleSentenceJp());
            HandlerSupport.put(detail, "exampleSentenceVi", vocabulary.getExampleSentenceVi());
        }
        return ContentSnapshot.builder()
                .contentId(vocabulary.getId())
                .contentType(ContentType.VOCABULARY)
                .titleOrText(vocabulary.getWord())
                .jlptLevel(vocabulary.getJlptLevel() != null ? vocabulary.getJlptLevel().name() : null)
                .status(vocabulary.getStatus() != null ? vocabulary.getStatus().getValue() : null)
                .createdById(HandlerSupport.creatorId(vocabulary.getCreatedBy()))
                .createdByName(HandlerSupport.creatorName(vocabulary.getCreatedBy()))
                .submittedAt(vocabulary.getUpdatedAt())
                .detail(detail)
                .build();
    }
}
