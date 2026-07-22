/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.contentreview.handler;

import com.jlpt.feature.contentreview.model.ContentSnapshot;
import com.jlpt.feature.contentreview.model.ContentType;
import com.jlpt.feature.contentreview.repository.ReviewLessonRepository;
import com.jlpt.feature.learning.Lesson;
import com.jlpt.feature.speaking.entity.SpeakingQuestion;
import com.jlpt.feature.speaking.repository.SpeakingQuestionRepository;
import com.jlpt.feature.staff.StaffUser;
import com.jlpt.feature.student.StudentUser.JlptLevel;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Review adapter dedicated to speaking lessons so type=speaking never returns other lesson types. */
@Component
@RequiredArgsConstructor
public class SpeakingContentHandler implements ReviewableContentHandler {

    private final ReviewLessonRepository lessonRepository;
    private final SpeakingQuestionRepository questionRepository;

    @Override
    public ContentType type() {
        return ContentType.SPEAKING;
    }

    @Override
    public String tableName() {
        return "lessons";
    }

    @Override
    public List<ContentSnapshot> findPending(JlptLevel level) {
        List<Lesson> lessons = level == null
                ? lessonRepository.findPendingByType(
                        Lesson.LessonStatus.PENDING_REVIEW, Lesson.LessonType.SPEAKING)
                : lessonRepository.findPendingByTypeAndLevel(
                        Lesson.LessonStatus.PENDING_REVIEW, Lesson.LessonType.SPEAKING, level);
        return lessons.stream().map(lesson -> toSnapshot(lesson, false)).toList();
    }

    @Override
    public Optional<ContentSnapshot> findActiveById(Long contentId) {
        return lessonRepository
                .findActiveByIdAndType(contentId, Lesson.LessonStatus.DELETED, Lesson.LessonType.SPEAKING)
                .map(lesson -> toSnapshot(lesson, true));
    }

    @Override
    public int approve(Long contentId, StaffUser manager, LocalDateTime now) {
        return lessonRepository.approve(
                contentId, manager, now, Lesson.LessonStatus.PENDING_REVIEW, Lesson.LessonStatus.PUBLISHED);
    }

    @Override
    public int transitionFromPending(Long contentId, String targetStatus, LocalDateTime now) {
        Lesson.LessonStatus target = HandlerSupport.toEnum(Lesson.LessonStatus.class, targetStatus);
        return lessonRepository.transition(
                contentId, now, Lesson.LessonStatus.PENDING_REVIEW, target);
    }

    private ContentSnapshot toSnapshot(Lesson lesson, boolean withDetail) {
        Map<String, Object> detail = null;
        if (withDetail) {
            detail = HandlerSupport.newDetail();
            HandlerSupport.put(detail, "lessonType", Lesson.LessonType.SPEAKING.getValue());
            HandlerSupport.put(detail, "contentText", lesson.getContentText());
            HandlerSupport.put(detail, "displayOrder", lesson.getDisplayOrder());
            HandlerSupport.put(detail, "questions", questionsFor(lesson));
        }
        return ContentSnapshot.builder()
                .contentId(lesson.getId())
                .contentType(ContentType.SPEAKING)
                .titleOrText(lesson.getTitle())
                .jlptLevel(lesson.getJlptLevel() == null ? null : lesson.getJlptLevel().name())
                .status(lesson.getStatus() == null ? null : lesson.getStatus().getValue())
                .createdById(HandlerSupport.creatorId(lesson.getCreatedBy()))
                .createdByName(HandlerSupport.creatorName(lesson.getCreatedBy()))
                .submittedAt(lesson.getUpdatedAt())
                .detail(detail)
                .build();
    }

    private List<Map<String, Object>> questionsFor(Lesson lesson) {
        List<SpeakingQuestion> questions =
                questionRepository.findByLesson_IdOrderByDisplayOrderAsc(lesson.getId());
        if (questions.isEmpty() && lesson.getContentText() != null && !lesson.getContentText().isBlank()) {
            return List.of(Map.of(
                    "promptText", lesson.getContentText(),
                    "displayOrder", 0,
                    "sampleAudioUrl", lesson.getAudioUrl() == null ? "" : lesson.getAudioUrl()));
        }
        return questions.stream().map(this::questionDetail).toList();
    }

    private Map<String, Object> questionDetail(SpeakingQuestion question) {
        Map<String, Object> detail = HandlerSupport.newDetail();
        HandlerSupport.put(detail, "speakingQuestionId", question.getId());
        HandlerSupport.put(detail, "promptText", question.getPromptText());
        HandlerSupport.put(detail, "instruction", question.getInstruction());
        HandlerSupport.put(detail, "sampleAudioUrl", question.getSampleAudioUrl());
        HandlerSupport.put(detail, "displayOrder", question.getDisplayOrder());
        return detail;
    }
}
