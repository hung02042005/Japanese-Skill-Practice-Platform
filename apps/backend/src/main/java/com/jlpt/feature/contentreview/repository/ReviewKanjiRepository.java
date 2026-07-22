/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.contentreview.repository;

import com.jlpt.feature.learning.Kanji;
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

/** UC-33 — Repository kiểm duyệt cho {@code kanji}. */
@Repository
public interface ReviewKanjiRepository extends JpaRepository<Kanji, Long> {

    @Query("SELECT k FROM Kanji k LEFT JOIN FETCH k.createdBy "
            + "WHERE k.status = :status "
            + "ORDER BY k.updatedAt ASC")
    List<Kanji> findPending(@Param("status") ContentStatus status);

    @Query("SELECT k FROM Kanji k LEFT JOIN FETCH k.createdBy "
            + "WHERE k.status = :status AND k.jlptLevel = :level "
            + "ORDER BY k.updatedAt ASC")
    List<Kanji> findPending(@Param("status") ContentStatus status, @Param("level") JlptLevel level);

    @Query("SELECT k FROM Kanji k LEFT JOIN FETCH k.createdBy WHERE k.id = :id AND k.status <> :deleted")
    Optional<Kanji> findActiveById(
            @Param("id") Long contentId, @Param("deleted") ContentStatus deletedStatus);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Kanji k SET k.status = :to, k.approvedBy = :mgr, k.publishedAt = :now, k.updatedAt = :now "
            + "WHERE k.id = :id AND k.status = :from")
    int approve(
            @Param("id") Long contentId,
            @Param("mgr") StaffUser manager,
            @Param("now") LocalDateTime reviewTimestamp,
            @Param("from") ContentStatus expectedStatus,
            @Param("to") ContentStatus targetStatus);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Kanji k SET k.status = :to, k.updatedAt = :now WHERE k.id = :id AND k.status = :from")
    int transition(
            @Param("id") Long contentId,
            @Param("now") LocalDateTime reviewTimestamp,
            @Param("from") ContentStatus expectedStatus,
            @Param("to") ContentStatus targetStatus);
}
