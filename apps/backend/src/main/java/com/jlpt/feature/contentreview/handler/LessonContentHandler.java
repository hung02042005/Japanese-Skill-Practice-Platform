/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.contentreview.handler;

import com.jlpt.feature.contentreview.model.ContentSnapshot;
import com.jlpt.feature.contentreview.model.ContentType;
import com.jlpt.feature.contentreview.repository.ReviewLessonRepository;
import com.jlpt.feature.learning.Lesson;
import com.jlpt.feature.learning.Lesson.LessonStatus;
import com.jlpt.feature.staff.StaffUser;
import com.jlpt.feature.student.StudentUser.JlptLevel;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** UC-33 — Handler kiểm duyệt cho {@code lessons} (đồng thời phục vụ contentType=course). */
@Component
@RequiredArgsConstructor
public class LessonContentHandler implements ReviewableContentHandler {

    private final ReviewLessonRepository repository;

    @Override
    public ContentType type() {
        return ContentType.LESSON;
    }

    @Override
    public String tableName() {
        return "lessons";
    }

    @Override
    public List<ContentSnapshot> findPending(JlptLevel level) {
        List<Lesson> lessons = (level == null)
                ? repository.findPendingExcludingType(LessonStatus.PENDING_REVIEW, Lesson.LessonType.SPEAKING)
                : repository.findPendingExcludingType(LessonStatus.PENDING_REVIEW, level, Lesson.LessonType.SPEAKING);
        return lessons.stream().map(lesson -> toSnapshot(lesson, false)).toList();
    }

    @Override
    public Optional<ContentSnapshot> findActiveById(Long contentId) {
        return repository.findActiveById(contentId, LessonStatus.DELETED).map(lesson -> toSnapshot(lesson, true));
    }

    @Override
    public int approve(Long contentId, StaffUser manager, LocalDateTime now) {
        return repository.approve(contentId, manager, now, LessonStatus.PENDING_REVIEW, LessonStatus.PUBLISHED);
    }

    @Override
    public int transitionFromPending(Long contentId, String targetStatus, LocalDateTime now) {
        LessonStatus targetLessonStatus = HandlerSupport.toEnum(LessonStatus.class, targetStatus);
        return repository.transition(contentId, now, LessonStatus.PENDING_REVIEW, targetLessonStatus);
    }

    private ContentSnapshot toSnapshot(Lesson lesson, boolean withDetail) {
        Map<String, Object> detail = null;
        if (withDetail) {
            detail = HandlerSupport.newDetail();
            HandlerSupport.put(
                    detail,
                    "lessonType",
                    lesson.getLessonType() != null ? lesson.getLessonType().getValue() : null);
            HandlerSupport.put(detail, "contentText", lesson.getContentText());
            HandlerSupport.put(detail, "explanation", lesson.getExplanation());
            HandlerSupport.put(detail, "displayOrder", lesson.getDisplayOrder());
            HandlerSupport.put(detail, "videoUrl", lesson.getVideoUrl());
            HandlerSupport.put(detail, "audioUrl", lesson.getAudioUrl());
        }
        return ContentSnapshot.builder()
                .contentId(lesson.getId())
                .contentType(ContentType.LESSON)
                .titleOrText(lesson.getTitle())
                .jlptLevel(lesson.getJlptLevel() != null ? lesson.getJlptLevel().name() : null)
                .status(lesson.getStatus() != null ? lesson.getStatus().getValue() : null)
                .createdById(HandlerSupport.creatorId(lesson.getCreatedBy()))
                .createdByName(HandlerSupport.creatorName(lesson.getCreatedBy()))
                .submittedAt(lesson.getUpdatedAt())
                .detail(detail)
                .build();
    }
}
