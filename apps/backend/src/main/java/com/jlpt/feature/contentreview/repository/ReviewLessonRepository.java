/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.contentreview.repository;

import com.jlpt.feature.learning.Lesson;
import com.jlpt.feature.learning.Lesson.LessonStatus;
import com.jlpt.feature.staff.StaffUser;
import com.jlpt.feature.student.StudentUser.JlptLevel;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/** UC-33 — Repository kiểm duyệt cho {@code lessons} (cũng phục vụ contentType=course). */
@Repository
public interface ReviewLessonRepository extends JpaRepository<Lesson, Long> {

    @Query("SELECT l FROM Lesson l LEFT JOIN FETCH l.createdBy "
            + "WHERE l.status = :status "
            + "ORDER BY l.updatedAt ASC")
    List<Lesson> findPending(@Param("status") LessonStatus status);

    @Query("SELECT l FROM Lesson l LEFT JOIN FETCH l.createdBy "
            + "WHERE l.status = :status AND l.lessonType <> :excludedType "
            + "ORDER BY l.updatedAt ASC")
    List<Lesson> findPendingExcludingType(
            @Param("status") LessonStatus status, @Param("excludedType") Lesson.LessonType excludedLessonType);

    @Query("SELECT l FROM Lesson l LEFT JOIN FETCH l.createdBy "
            + "WHERE l.status = :status AND l.jlptLevel = :level "
            + "ORDER BY l.updatedAt ASC")
    List<Lesson> findPending(@Param("status") LessonStatus status, @Param("level") JlptLevel level);

    @Query("SELECT l FROM Lesson l LEFT JOIN FETCH l.createdBy "
            + "WHERE l.status = :status AND l.jlptLevel = :level AND l.lessonType <> :excludedType "
            + "ORDER BY l.updatedAt ASC")
    List<Lesson> findPendingExcludingType(
            @Param("status") LessonStatus status,
            @Param("level") JlptLevel level,
            @Param("excludedType") Lesson.LessonType excludedLessonType);

    @Query("SELECT l FROM Lesson l LEFT JOIN FETCH l.createdBy "
            + "WHERE l.status = :status AND l.lessonType = :type ORDER BY l.updatedAt ASC")
    List<Lesson> findPendingByType(@Param("status") LessonStatus status, @Param("type") Lesson.LessonType lessonType);

    @Query("SELECT l FROM Lesson l LEFT JOIN FETCH l.createdBy "
            + "WHERE l.status = :status AND l.lessonType = :type AND l.jlptLevel = :level "
            + "ORDER BY l.updatedAt ASC")
    List<Lesson> findPendingByTypeAndLevel(
            @Param("status") LessonStatus status,
            @Param("type") Lesson.LessonType lessonType,
            @Param("level") JlptLevel level);

    @Query("SELECT l FROM Lesson l LEFT JOIN FETCH l.createdBy WHERE l.id = :id AND l.status <> :deleted")
    Optional<Lesson> findActiveById(@Param("id") Long contentId, @Param("deleted") LessonStatus deletedStatus);

    @Query("SELECT l FROM Lesson l LEFT JOIN FETCH l.createdBy "
            + "WHERE l.id = :id AND l.status <> :deleted AND l.lessonType = :type")
    Optional<Lesson> findActiveByIdAndType(
            @Param("id") Long contentId,
            @Param("deleted") LessonStatus deletedStatus,
            @Param("type") Lesson.LessonType lessonType);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Lesson l SET l.status = :to, l.approvedBy = :mgr, l.publishedAt = :now, l.updatedAt = :now "
            + "WHERE l.id = :id AND l.status = :from")
    int approve(
            @Param("id") Long contentId,
            @Param("mgr") StaffUser manager,
            @Param("now") LocalDateTime reviewTimestamp,
            @Param("from") LessonStatus expectedStatus,
            @Param("to") LessonStatus targetStatus);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Lesson l SET l.status = :to, l.updatedAt = :now WHERE l.id = :id AND l.status = :from")
    int transition(
            @Param("id") Long contentId,
            @Param("now") LocalDateTime reviewTimestamp,
            @Param("from") LessonStatus expectedStatus,
            @Param("to") LessonStatus targetStatus);
}
