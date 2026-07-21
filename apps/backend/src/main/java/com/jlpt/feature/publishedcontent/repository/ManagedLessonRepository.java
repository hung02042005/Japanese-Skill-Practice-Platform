/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.publishedcontent.repository;

import com.jlpt.feature.learning.Lesson;
import com.jlpt.feature.learning.Lesson.LessonStatus;
import com.jlpt.feature.student.StudentUser.JlptLevel;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/** UC-34 — Quản lý trạng thái cho {@code lessons} (cũng phục vụ contentType=course) (FR-34-03/10). */
@Repository
public interface ManagedLessonRepository extends JpaRepository<Lesson, Long> {

    @Query("SELECT l FROM Lesson l "
            + "WHERE l.status = :published AND (:level IS NULL OR l.jlptLevel = :level) "
            + "ORDER BY l.publishedAt DESC")
    List<Lesson> findPublished(
            @Param("published") LessonStatus publishedStatus, @Param("level") JlptLevel jlptLevel);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Lesson l SET l.status = :to, l.updatedAt = :now WHERE l.id = :id AND l.status = :from")
    int transition(
            @Param("id") Long id,
            @Param("from") LessonStatus from,
            @Param("to") LessonStatus to,
            @Param("now") LocalDateTime now);

    List<Lesson> findByStatusOrderByUpdatedAtDesc(LessonStatus status);
}
