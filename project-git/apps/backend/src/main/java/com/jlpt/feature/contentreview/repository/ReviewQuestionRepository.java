/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.contentreview.repository;

import com.jlpt.feature.assessment.Question;
import com.jlpt.feature.assessment.Question.ContentStatus;
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

/** UC-33 — Repository kiểm duyệt cho {@code questions}. */
@Repository
public interface ReviewQuestionRepository extends JpaRepository<Question, Long> {

    @Query("SELECT q FROM Question q LEFT JOIN FETCH q.createdBy "
            + "WHERE q.status = :status "
            + "ORDER BY q.updatedAt ASC")
    List<Question> findPending(@Param("status") ContentStatus status);

    @Query("SELECT q FROM Question q LEFT JOIN FETCH q.createdBy "
            + "WHERE q.status = :status AND q.jlptLevel = :level "
            + "ORDER BY q.updatedAt ASC")
    List<Question> findPending(@Param("status") ContentStatus status, @Param("level") JlptLevel level);

    @Query("SELECT q FROM Question q LEFT JOIN FETCH q.createdBy WHERE q.id = :id AND q.status <> :deleted")
    Optional<Question> findActiveById(@Param("id") Long id, @Param("deleted") ContentStatus deleted);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Question q SET q.status = :to, q.approvedBy = :mgr, q.publishedAt = :now, q.updatedAt = :now "
            + "WHERE q.id = :id AND q.status = :from")
    int approve(
            @Param("id") Long id,
            @Param("mgr") StaffUser mgr,
            @Param("now") LocalDateTime now,
            @Param("from") ContentStatus from,
            @Param("to") ContentStatus to);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Question q SET q.status = :to, q.updatedAt = :now WHERE q.id = :id AND q.status = :from")
    int transition(
            @Param("id") Long id,
            @Param("now") LocalDateTime now,
            @Param("from") ContentStatus from,
            @Param("to") ContentStatus to);
}
