/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.contentreview.repository;

import com.jlpt.feature.learning.GrammarPoint;
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

/** UC-33 — Repository kiểm duyệt cho {@code grammar_points}. */
@Repository
public interface ReviewGrammarRepository extends JpaRepository<GrammarPoint, Long> {

    @Query("SELECT g FROM GrammarPoint g LEFT JOIN FETCH g.createdBy "
            + "WHERE g.status = :status "
            + "ORDER BY g.updatedAt ASC")
    List<GrammarPoint> findPending(@Param("status") ContentStatus status);

    @Query("SELECT g FROM GrammarPoint g LEFT JOIN FETCH g.createdBy "
            + "WHERE g.status = :status AND g.jlptLevel = :level "
            + "ORDER BY g.updatedAt ASC")
    List<GrammarPoint> findPending(@Param("status") ContentStatus status, @Param("level") JlptLevel level);

    @Query("SELECT g FROM GrammarPoint g LEFT JOIN FETCH g.createdBy WHERE g.id = :id AND g.status <> :deleted")
    Optional<GrammarPoint> findActiveById(
            @Param("id") Long contentId, @Param("deleted") ContentStatus deletedStatus);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE GrammarPoint g SET g.status = :to, g.approvedBy = :mgr, g.publishedAt = :now, g.updatedAt = :now "
            + "WHERE g.id = :id AND g.status = :from")
    int approve(
            @Param("id") Long contentId,
            @Param("mgr") StaffUser manager,
            @Param("now") LocalDateTime reviewTimestamp,
            @Param("from") ContentStatus expectedStatus,
            @Param("to") ContentStatus targetStatus);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE GrammarPoint g SET g.status = :to, g.updatedAt = :now WHERE g.id = :id AND g.status = :from")
    int transition(
            @Param("id") Long contentId,
            @Param("now") LocalDateTime reviewTimestamp,
            @Param("from") ContentStatus expectedStatus,
            @Param("to") ContentStatus targetStatus);
}
