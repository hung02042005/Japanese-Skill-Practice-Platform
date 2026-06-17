/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.contentreview.handler;

import com.jlpt.feature.contentreview.ContentSnapshot;
import com.jlpt.feature.contentreview.ContentType;
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
        List<Lesson> list = (level == null)
                ? repository.findPending(LessonStatus.PENDING_REVIEW)
                : repository.findPending(LessonStatus.PENDING_REVIEW, level);
        return list.stream().map(l -> toSnapshot(l, false)).toList();
    }

    @Override
    public Optional<ContentSnapshot> findActiveById(Long contentId) {
        return repository.findActiveById(contentId, LessonStatus.DELETED).map(l -> toSnapshot(l, true));
    }

    @Override
    public int approve(Long contentId, StaffUser manager, LocalDateTime now) {
        return repository.approve(contentId, manager, now, LessonStatus.PENDING_REVIEW, LessonStatus.PUBLISHED);
    }

    @Override
    public int transitionFromPending(Long contentId, String targetStatus, LocalDateTime now) {
        LessonStatus to = HandlerSupport.toEnum(LessonStatus.class, targetStatus);
        return repository.transition(contentId, now, LessonStatus.PENDING_REVIEW, to);
    }

    private ContentSnapshot toSnapshot(Lesson l, boolean withDetail) {
        Map<String, Object> detail = null;
        if (withDetail) {
            detail = HandlerSupport.newDetail();
            HandlerSupport.put(
                    detail,
                    "lessonType",
                    l.getLessonType() != null ? l.getLessonType().getValue() : null);
            HandlerSupport.put(detail, "contentText", l.getContentText());
            HandlerSupport.put(detail, "explanation", l.getExplanation());
            HandlerSupport.put(detail, "displayOrder", l.getDisplayOrder());
            HandlerSupport.put(detail, "videoUrl", l.getVideoUrl());
            HandlerSupport.put(detail, "audioUrl", l.getAudioUrl());
        }
        return ContentSnapshot.builder()
                .contentId(l.getId())
                .contentType(ContentType.LESSON)
                .titleOrText(l.getTitle())
                .jlptLevel(l.getJlptLevel() != null ? l.getJlptLevel().name() : null)
                .status(l.getStatus() != null ? l.getStatus().getValue() : null)
                .createdById(HandlerSupport.creatorId(l.getCreatedBy()))
                .createdByName(HandlerSupport.creatorName(l.getCreatedBy()))
                .submittedAt(l.getUpdatedAt())
                .detail(detail)
                .build();
    }
}
