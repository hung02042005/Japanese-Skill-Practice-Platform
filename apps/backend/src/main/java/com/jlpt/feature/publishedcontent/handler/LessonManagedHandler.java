/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.publishedcontent.handler;

import com.jlpt.feature.contentreview.model.ContentType;
import com.jlpt.feature.learning.Kanji;
import com.jlpt.feature.learning.Lesson;
import com.jlpt.feature.learning.Lesson.LessonStatus;
import com.jlpt.feature.publishedcontent.dto.ReferenceItemResponse;
import com.jlpt.feature.publishedcontent.model.ManagedContentSnapshot;
import com.jlpt.feature.publishedcontent.model.TargetStatus;
import com.jlpt.feature.publishedcontent.repository.ManagedAssessmentRepository;
import com.jlpt.feature.publishedcontent.repository.ManagedLessonRepository;
import com.jlpt.feature.student.StudentUser.JlptLevel;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * UC-34 — Handler quản lý trạng thái cho {@code lessons} (phục vụ cả
 * contentType=course).
 */
@Component
@RequiredArgsConstructor
public class LessonManagedHandler implements ManagedContentHandler {

    private final ManagedLessonRepository repository;
    private final ManagedAssessmentRepository assessmentRepository;

    @Override
    public ContentType type() {
        return ContentType.LESSON;
    }

    @Override
    public String tableName() {
        return "lessons";
    }

    @Override
    public List<ManagedContentSnapshot> findPublished(JlptLevel level) {
        return repository.findPublished(LessonStatus.PUBLISHED, level).stream()
                .map(this::toSnapshot)
                .toList();
    }

    @Override
    public Optional<ManagedContentSnapshot> findById(Long contentId) {
        return repository.findById(contentId).map(this::toSnapshot);
    }

    @Override
    public List<ReferenceItemResponse> findBlockingReferences(Long contentId) {
        // FR-34-15 — lesson đang được đề thi published tham chiếu qua
        // assessments.lesson_id.
        return assessmentRepository.findPublishedAssessmentsByLesson(contentId, Kanji.ContentStatus.PUBLISHED);
    }

    @Override
    public int changeStatus(Long contentId, TargetStatus targetStatus, LocalDateTime changeTimestamp) {
        LessonStatus targetLessonStatus =
                switch (targetStatus) {
                    case UNPUBLISHED -> LessonStatus.DRAFT;
                    case ARCHIVED -> LessonStatus.ARCHIVED;
                    case DELETED -> LessonStatus.DELETED;
                };
        return repository.transition(contentId, LessonStatus.PUBLISHED, targetLessonStatus, changeTimestamp);
    }

    @Override
    public int restore(Long contentId, LocalDateTime now) {
        return repository.transition(contentId, LessonStatus.ARCHIVED, LessonStatus.PUBLISHED, now);
    }

    private ManagedContentSnapshot toSnapshot(Lesson lesson) {
        return ManagedContentSnapshot.builder()
                .contentId(lesson.getId())
                .contentType(ContentType.LESSON)
                .titleOrText(lesson.getTitle())
                .jlptLevel(lesson.getJlptLevel() != null ? lesson.getJlptLevel().name() : null)
                .status(lesson.getStatus() != null ? lesson.getStatus().getValue() : null)
                .publishedAt(lesson.getPublishedAt())
                .build();
    }
}
