/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.student;

import com.jlpt.feature.learning.Kanji;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface StudentContentProgressRepository extends JpaRepository<StudentContentProgress, Long> {

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
}
