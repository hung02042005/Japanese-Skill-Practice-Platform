/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.student.lesson;

import com.jlpt.feature.learning.Lesson;
import com.jlpt.feature.learning.LessonRepository;
import com.jlpt.feature.student.StudentContentProgress;
import com.jlpt.feature.student.StudentContentProgress.ContentType;
import com.jlpt.feature.student.StudentContentProgressRepository;
import com.jlpt.feature.student.lesson.dto.LessonDetailResponse;
import com.jlpt.shared.exception.ResourceNotFoundException;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Chi tiết bài học published cho học viên (student-facing GET /api/lessons/{id}).
 * Tách khỏi {@code StudentReadingController} vốn chỉ phục vụ bài Đọc hiểu.
 */
@Service
@RequiredArgsConstructor
public class StudentLessonService {

    private static final int DEFAULT_LESSON_MINUTES = 10;

    private final LessonRepository lessonRepository;
    private final StudentContentProgressRepository progressRepository;

    @Transactional(readOnly = true)
    public LessonDetailResponse getLessonDetail(Long lessonId, Long studentId) {
        Lesson lesson = lessonRepository
                .findByIdAndStatus(lessonId, Lesson.LessonStatus.PUBLISHED)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bài học"));

        StudentContentProgress progress = progressRepository
                .findByStudentIdAndContentTypeAndContentId(studentId, ContentType.LESSON, lessonId)
                .orElse(null);
        String progressStatus = progress != null ? progress.getStatus().getValue() : "learning";
        int progressPercent = progress != null && progress.getProgressPercent() != null
                ? progress.getProgressPercent().intValue()
                : 0;

        Long[] prevNext = resolvePrevNext(lesson);

        return LessonDetailResponse.builder()
                .id(lesson.getId())
                .title(lesson.getTitle())
                .jlptLevel(lesson.getJlptLevel() != null ? lesson.getJlptLevel().name() : null)
                .lessonType(lesson.getLessonType() != null ? lesson.getLessonType().name() : null)
                .estimatedMinutes(DEFAULT_LESSON_MINUTES)
                // Chưa có model subscription/VIP → không khoá (đồng nhất với VocabHomeService).
                .locked(false)
                .progressStatus(progressStatus)
                .progressPercent(progressPercent)
                .contentHtml(lesson.getContentText())
                .audioUrl(lesson.getAudioUrl())
                .imageUrl(lesson.getAttachmentUrl())
                .prevLessonId(prevNext[0])
                .nextLessonId(prevNext[1])
                // Lesson chưa liên kết Vocabulary/Grammar → mảng rỗng (FE đã guard).
                .vocabulary(Collections.emptyList())
                .grammarPoints(Collections.emptyList())
                .build();
    }

    /** prev/next theo thứ tự hiển thị trong cùng cấp độ. */
    private Long[] resolvePrevNext(Lesson lesson) {
        if (lesson.getJlptLevel() == null) {
            return new Long[] {null, null};
        }
        List<Lesson> siblings = lessonRepository.findByJlptLevelAndStatusOrderByDisplayOrderAscIdAsc(
                lesson.getJlptLevel(), Lesson.LessonStatus.PUBLISHED);
        Long prev = null;
        Long next = null;
        for (int i = 0; i < siblings.size(); i++) {
            if (siblings.get(i).getId().equals(lesson.getId())) {
                if (i > 0) {
                    prev = siblings.get(i - 1).getId();
                }
                if (i < siblings.size() - 1) {
                    next = siblings.get(i + 1).getId();
                }
                break;
            }
        }
        return new Long[] {prev, next};
    }
}
