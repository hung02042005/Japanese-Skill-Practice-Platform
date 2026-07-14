/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.publishedcontent.repository;

import com.jlpt.feature.assessment.Assessment;
import com.jlpt.feature.learning.Kanji.ContentStatus;
import com.jlpt.feature.publishedcontent.dto.ReferenceItemResponse;
import com.jlpt.feature.student.StudentUser.JlptLevel;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/** UC-34 — Quản lý trạng thái cho {@code assessments} + kiểm tra tham chiếu lesson (FR-34-03/10/15). */
@Repository
public interface ManagedAssessmentRepository extends JpaRepository<Assessment, Long> {

    @Query("SELECT a FROM Assessment a "
            + "WHERE a.status = :published AND (:level IS NULL OR a.jlptLevel = :level) "
            + "ORDER BY a.publishedAt DESC")
    List<Assessment> findPublished(@Param("published") ContentStatus published, @Param("level") JlptLevel level);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Assessment a SET a.status = :to, a.updatedAt = :now WHERE a.id = :id AND a.status = :from")
    int transition(
            @Param("id") Long id,
            @Param("from") ContentStatus from,
            @Param("to") ContentStatus to,
            @Param("now") LocalDateTime now);

    /**
     * FR-34-15 — Các đề thi {@code published} đang trỏ tới lesson qua {@code assessments.lesson_id}.
     * Nếu trả về ≥1 dòng ⇒ chặn ẩn lesson với 409 RESOURCE_IN_USE.
     */
    @Query("SELECT new com.jlpt.feature.publishedcontent.dto.ReferenceItemResponse('assessment', a.id, a.title) "
            + "FROM Assessment a WHERE a.lesson.id = :lessonId AND a.status = :published")
    List<ReferenceItemResponse> findPublishedAssessmentsByLesson(
            @Param("lessonId") Long lessonId, @Param("published") ContentStatus published);

    List<Assessment> findByStatusOrderByUpdatedAtDesc(ContentStatus status);
}

