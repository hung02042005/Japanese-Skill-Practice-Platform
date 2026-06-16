/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.contentreview.handler;

import com.jlpt.feature.assessment.Question;
import com.jlpt.feature.assessment.Question.ContentStatus;
import com.jlpt.feature.contentreview.ContentSnapshot;
import com.jlpt.feature.contentreview.ContentType;
import com.jlpt.feature.contentreview.repository.ReviewQuestionRepository;
import com.jlpt.feature.staff.StaffUser;
import com.jlpt.feature.student.StudentUser.JlptLevel;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** UC-33 — Handler kiểm duyệt cho {@code questions}. */
@Component
@RequiredArgsConstructor
public class QuestionContentHandler implements ReviewableContentHandler {

    private final ReviewQuestionRepository repository;

    @Override
    public ContentType type() {
        return ContentType.QUESTION;
    }

    @Override
    public String tableName() {
        return "questions";
    }

    @Override
    public List<ContentSnapshot> findPending(JlptLevel level) {
        List<Question> list = (level == null)
                ? repository.findPending(ContentStatus.PENDING_REVIEW)
                : repository.findPending(ContentStatus.PENDING_REVIEW, level);
        return list.stream().map(q -> toSnapshot(q, false)).toList();
    }

    @Override
    public Optional<ContentSnapshot> findActiveById(Long contentId) {
        return repository.findActiveById(contentId, ContentStatus.DELETED).map(q -> toSnapshot(q, true));
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

    private ContentSnapshot toSnapshot(Question q, boolean withDetail) {
        Map<String, Object> detail = null;
        if (withDetail) {
            detail = HandlerSupport.newDetail();
            HandlerSupport.put(
                    detail,
                    "questionType",
                    q.getQuestionType() != null ? q.getQuestionType().getValue() : null);
            HandlerSupport.put(
                    detail, "skill", q.getSkill() != null ? q.getSkill().getValue() : null);
            HandlerSupport.put(detail, "optionA", q.getOptionA());
            HandlerSupport.put(detail, "optionB", q.getOptionB());
            HandlerSupport.put(detail, "optionC", q.getOptionC());
            HandlerSupport.put(detail, "optionD", q.getOptionD());
            HandlerSupport.put(detail, "correctOption", q.getCorrectOption());
            HandlerSupport.put(detail, "correctAnswerText", q.getCorrectAnswerText());
            HandlerSupport.put(detail, "explanation", q.getExplanation());
        }
        return ContentSnapshot.builder()
                .contentId(q.getId())
                .contentType(ContentType.QUESTION)
                .titleOrText(q.getQuestionText())
                .jlptLevel(q.getJlptLevel() != null ? q.getJlptLevel().name() : null)
                .status(q.getStatus() != null ? q.getStatus().getValue() : null)
                .createdById(HandlerSupport.creatorId(q.getCreatedBy()))
                .createdByName(HandlerSupport.creatorName(q.getCreatedBy()))
                .submittedAt(q.getUpdatedAt())
                .detail(detail)
                .build();
    }
}
