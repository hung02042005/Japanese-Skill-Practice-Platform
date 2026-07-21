/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.contentreview.repository;

import com.jlpt.feature.assessment.Assessment;
import com.jlpt.feature.learning.Kanji.ContentStatus;
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

/** UC-33 — Repository kiểm duyệt cho {@code assessments} (quiz/exam). */
@Repository
public interface ReviewAssessmentRepository extends JpaRepository<Assessment, Long> {

    @Query("SELECT a FROM Assessment a LEFT JOIN FETCH a.createdBy "
            + "WHERE a.status = :status "
            + "ORDER BY a.updatedAt ASC")
    List<Assessment> findPending(@Param("status") ContentStatus status);

    @Query("SELECT a FROM Assessment a LEFT JOIN FETCH a.createdBy "
            + "WHERE a.status = :status AND a.jlptLevel = :level "
            + "ORDER BY a.updatedAt ASC")
    List<Assessment> findPending(@Param("status") ContentStatus status, @Param("level") JlptLevel level);

    @Query("SELECT a FROM Assessment a LEFT JOIN FETCH a.createdBy WHERE a.id = :id AND a.status <> :deleted")
    Optional<Assessment> findActiveById(
            @Param("id") Long contentId, @Param("deleted") ContentStatus deletedStatus);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Assessment a SET a.status = :to, a.approvedBy = :mgr, a.publishedAt = :now, a.updatedAt = :now "
            + "WHERE a.id = :id AND a.status = :from")
    int approve(
            @Param("id") Long contentId,
            @Param("mgr") StaffUser manager,
            @Param("now") LocalDateTime reviewTimestamp,
            @Param("from") ContentStatus expectedStatus,
            @Param("to") ContentStatus targetStatus);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Assessment a SET a.status = :to, a.updatedAt = :now WHERE a.id = :id AND a.status = :from")
    int transition(
            @Param("id") Long contentId,
            @Param("now") LocalDateTime reviewTimestamp,
            @Param("from") ContentStatus expectedStatus,
            @Param("to") ContentStatus targetStatus);
}
