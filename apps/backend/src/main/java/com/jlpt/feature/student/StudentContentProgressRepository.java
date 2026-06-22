/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.student;

import com.jlpt.feature.learning.Kanji;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface StudentContentProgressRepository extends JpaRepository<StudentContentProgress, Long> {

    // ── Hợp nhất từ feature.student.progress (kana/kanji/grammar feature của Duy) ──
    Optional<StudentContentProgress> findByStudentIdAndContentTypeAndContentId(
            Long studentId, StudentContentProgress.ContentType contentType, Long contentId);

    List<StudentContentProgress> findByStudentIdAndContentTypeAndContentIdIn(
            Long studentId, StudentContentProgress.ContentType contentType, Collection<Long> contentIds);

    @org.springframework.transaction.annotation.Transactional
    void deleteByStudentIdAndContentType(Long studentId, StudentContentProgress.ContentType contentType);

    @Query("SELECT COUNT(p) FROM StudentContentProgress p, Kanji k "
            + "WHERE p.contentId = k.id AND p.student.id = :studentId AND p.contentType = :contentType "
            + "AND p.status = :status AND k.jlptLevel = :level")
    long countCompletedKanjiByLevel(
            @Param("studentId") Long studentId,
            @Param("level") com.jlpt.feature.student.StudentUser.JlptLevel level,
            @Param("contentType") StudentContentProgress.ContentType contentType,
            @Param("status") StudentContentProgress.ProgressStatus status);

    @Query("SELECT COUNT(p) FROM StudentContentProgress p, Vocabulary v "
            + "WHERE p.contentId = v.id AND p.student.id = :studentId AND p.contentType = :contentType "
            + "AND p.status = :status AND v.jlptLevel = :level")
    long countCompletedVocabularyByLevel(
            @Param("studentId") Long studentId,
            @Param("level") com.jlpt.feature.student.StudentUser.JlptLevel level,
            @Param("contentType") StudentContentProgress.ContentType contentType,
            @Param("status") StudentContentProgress.ProgressStatus status);

    /**
     * Trả về tập hợp contentId mà student đã hoàn thành theo contentType.
     * Dùng DB query thay vì load toàn bộ bảng vào memory.
     */
    @Query(
            """
            SELECT p.contentId FROM StudentContentProgress p
            WHERE p.student.id = :studentId
              AND p.contentType = :contentType
              AND p.status = :status
            """)
    Set<Long> findCompletedContentIds(
            @Param("studentId") Long studentId,
            @Param("contentType") StudentContentProgress.ContentType contentType,
            @Param("status") StudentContentProgress.ProgressStatus status);

    @Query(
            """
            SELECT COUNT(p) FROM StudentContentProgress p
            WHERE p.student.id = :studentId
              AND p.contentType = :contentType
              AND p.status = :status
            """)
    long countCompleted(
            @Param("studentId") Long studentId,
            @Param("contentType") StudentContentProgress.ContentType contentType,
            @Param("status") StudentContentProgress.ProgressStatus status);

    @Query(
            """
            SELECT COUNT(p) FROM StudentContentProgress p
            WHERE p.student.id = :studentId
              AND p.contentType = :contentType
              AND p.status = :progressStatus
              AND p.contentId IN (
                SELECT v.id FROM Vocabulary v
                WHERE v.status = :contentStatus
                  AND v.topicRef.id = :topicId
              )
            """)
    long countCompletedVocabularyInTopic(
            @Param("studentId") Long studentId,
            @Param("contentType") StudentContentProgress.ContentType contentType,
            @Param("progressStatus") StudentContentProgress.ProgressStatus progressStatus,
            @Param("topicId") Long topicId,
            @Param("contentStatus") Kanji.ContentStatus contentStatus);

    /**
     * Đếm số bài học (LESSON) student đã hoàn thành trong một cấp độ JLPT
     * (SPEC-course-list — completedLessons mỗi khoá). Chỉ tính các bài đã publish.
     */
    @Query(
            """
            SELECT COUNT(p) FROM StudentContentProgress p
            WHERE p.student.id = :studentId
              AND p.contentType = :contentType
              AND p.status = :progressStatus
              AND p.contentId IN (
                SELECT l.id FROM Lesson l
                WHERE l.jlptLevel = :level
                  AND l.status = :lessonStatus
              )
            """)
    long countCompletedLessonsInLevel(
            @Param("studentId") Long studentId,
            @Param("contentType") StudentContentProgress.ContentType contentType,
            @Param("progressStatus") StudentContentProgress.ProgressStatus progressStatus,
            @Param("level") com.jlpt.feature.student.StudentUser.JlptLevel level,
            @Param("lessonStatus") com.jlpt.feature.learning.Lesson.LessonStatus lessonStatus);

    /**
     * Đếm số từ vựng đã hoàn thành (wordCount cho Dashboard).
     * Native query để tránh enum literal issue trong JPQL.
     */
    @Query(
            value =
                    """
            SELECT COUNT(*) FROM student_content_progress
            WHERE student_id = :studentId
              AND content_type = 'vocabulary'
              AND status = 'completed'
            """,
            nativeQuery = true)
    long countCompletedVocab(@Param("studentId") Long studentId);

    /**
     * Đếm số ngày khác nhau student đã học trong tháng hiện tại (dựa vào lastStudiedAt).
     * Dùng native query để tránh vấn đề JPQL với YEAR/MONTH trên SQL Server.
     */
    @Query(
            value =
                    """
            SELECT COUNT(DISTINCT CAST(last_studied_at AS DATE))
            FROM student_content_progress
            WHERE student_id = :studentId
              AND YEAR(last_studied_at) = :year
              AND MONTH(last_studied_at) = :month
            """,
            nativeQuery = true)
    long countDistinctStudyDaysInMonth(
            @Param("studentId") Long studentId, @Param("year") int year, @Param("month") int month);

    Optional<StudentContentProgress> findByStudent_IdAndContentTypeAndContentId(
            Long studentId, StudentContentProgress.ContentType contentType, Long contentId);

    List<StudentContentProgress> findByStudent_IdAndContentTypeAndContentIdIn(
            Long studentId, StudentContentProgress.ContentType contentType, List<Long> contentIds);

    long countByStudent_IdAndContentTypeAndContentIdInAndStatus(
            Long studentId,
            StudentContentProgress.ContentType contentType,
            List<Long> contentIds,
            StudentContentProgress.ProgressStatus status);
}
