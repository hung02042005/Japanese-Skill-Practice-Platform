/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.learning;

import com.jlpt.feature.staff.StaffUser;
import com.jlpt.feature.student.StudentUser.JlptLevel;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface LessonRepository extends JpaRepository<Lesson, Long> {

    /**
     * Fetches a lesson that is not soft-deleted (FR-27-18, lesson update).
     * Use with {@code Lesson.LessonStatus.DELETED} to exclude deleted rows.
     */
    Optional<Lesson> findByIdAndStatusNot(Long id, Lesson.LessonStatus status);

    /** Lists the lessons owned by a staff member, newest first, excluding soft-deleted rows. */
    List<Lesson> findByCreatedByAndStatusNotOrderByUpdatedAtDesc(StaffUser createdBy, Lesson.LessonStatus status);

    /**
     * Đếm số bài học đã publish của một cấp độ JLPT (SPEC-course-list — totalLessons mỗi khoá).
     */
    long countByJlptLevelAndStatus(JlptLevel jlptLevel, Lesson.LessonStatus status);

    /** Bài học đã publish của một cấp độ, sắp theo thứ tự hiển thị (Dashboard / next-lesson). */
    List<Lesson> findByJlptLevelAndStatusOrderByDisplayOrderAscIdAsc(
            JlptLevel jlptLevel, Lesson.LessonStatus status);

    /** Một bài học đã publish theo id (trang chi tiết bài học student-facing). */
    Optional<Lesson> findByIdAndStatus(Long id, Lesson.LessonStatus status);

    /**
     * UC-27 — Filtered paginated query for staff listing their own lessons.
     * All params except {@code staffId} and {@code deletedStatus} are optional.
     */
    @Query(
            value =
                    """
            SELECT l FROM Lesson l
            WHERE l.createdBy.id = :staffId
              AND (:jlptLevel IS NULL OR l.jlptLevel = :jlptLevel)
              AND (:lessonType IS NULL OR l.lessonType = :lessonType)
              AND ((:status IS NULL AND l.status <> :deletedStatus) OR l.status = :status)
              AND (:q IS NULL OR LOWER(l.title) LIKE LOWER(CONCAT('%', :q, '%')))
            """,
            countQuery =
                    """
            SELECT COUNT(l) FROM Lesson l
            WHERE l.createdBy.id = :staffId
              AND (:jlptLevel IS NULL OR l.jlptLevel = :jlptLevel)
              AND (:lessonType IS NULL OR l.lessonType = :lessonType)
              AND ((:status IS NULL AND l.status <> :deletedStatus) OR l.status = :status)
              AND (:q IS NULL OR LOWER(l.title) LIKE LOWER(CONCAT('%', :q, '%')))
            """)
    Page<Lesson> findByCreatedByWithFilters(
            @Param("staffId") Long staffId,
            @Param("jlptLevel") JlptLevel jlptLevel,
            @Param("lessonType") Lesson.LessonType lessonType,
            @Param("status") Lesson.LessonStatus status,
            @Param("deletedStatus") Lesson.LessonStatus deletedStatus,
            @Param("q") String q,
            Pageable pageable);

    /** Dictionary search over published lessons by title (ported from legacy flat repository). */
    @Query(
            """
            SELECT l FROM Lesson l
            WHERE l.status = :status
              AND (:jlptLevel IS NULL OR l.jlptLevel = :jlptLevel)
              AND l.title LIKE CONCAT('%', :q, '%')
            ORDER BY l.title ASC
            """)
    List<Lesson> searchPublished(
            @Param("q") String q,
            @Param("jlptLevel") JlptLevel jlptLevel,
            @Param("status") Lesson.LessonStatus status,
            Pageable pageable);
}
