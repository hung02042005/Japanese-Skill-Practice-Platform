/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.contentreview.handler;

import com.jlpt.feature.assessment.Question;
import com.jlpt.feature.assessment.Question.ContentStatus;
import com.jlpt.feature.contentreview.model.ContentSnapshot;
import com.jlpt.feature.contentreview.model.ContentType;
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
        List<Question> questions = (level == null)
                ? repository.findPending(ContentStatus.PENDING_REVIEW)
                : repository.findPending(ContentStatus.PENDING_REVIEW, level);
        return questions.stream().map(question -> toSnapshot(question, false)).toList();
    }

    @Override
    public Optional<ContentSnapshot> findActiveById(Long contentId) {
        return repository.findActiveById(contentId, ContentStatus.DELETED).map(question -> toSnapshot(question, true));
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

    private ContentSnapshot toSnapshot(Question question, boolean withDetail) {
        Map<String, Object> detail = null;
        if (withDetail) {
            detail = HandlerSupport.newDetail();
            HandlerSupport.put(
                    detail,
                    "questionType",
                    question.getQuestionType() != null
                            ? question.getQuestionType().getValue()
                            : null);
            HandlerSupport.put(
                    detail,
                    "skill",
                    question.getSkill() != null ? question.getSkill().getValue() : null);
            HandlerSupport.put(detail, "optionA", question.getOptionA());
            HandlerSupport.put(detail, "optionB", question.getOptionB());
            HandlerSupport.put(detail, "optionC", question.getOptionC());
            HandlerSupport.put(detail, "optionD", question.getOptionD());
            HandlerSupport.put(detail, "correctOption", question.getCorrectOption());
            HandlerSupport.put(detail, "correctAnswerText", question.getCorrectAnswerText());
            HandlerSupport.put(detail, "explanation", question.getExplanation());
        }
        return ContentSnapshot.builder()
                .contentId(question.getId())
                .contentType(ContentType.QUESTION)
                .titleOrText(question.getQuestionText())
                .jlptLevel(
                        question.getJlptLevel() != null
                                ? question.getJlptLevel().name()
                                : null)
                .status(question.getStatus() != null ? question.getStatus().getValue() : null)
                .createdById(HandlerSupport.creatorId(question.getCreatedBy()))
                .createdByName(HandlerSupport.creatorName(question.getCreatedBy()))
                .submittedAt(question.getUpdatedAt())
                .detail(detail)
                .build();
    }
}
