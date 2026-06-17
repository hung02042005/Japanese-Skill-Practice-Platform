/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.contentreview.repository;

import com.jlpt.feature.learning.Kanji.ContentStatus;
import com.jlpt.feature.learning.Vocabulary;
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

/** UC-33 — Repository kiểm duyệt cho {@code vocabulary}. */
@Repository
public interface ReviewVocabularyRepository extends JpaRepository<Vocabulary, Long> {

    @Query("SELECT v FROM Vocabulary v LEFT JOIN FETCH v.createdBy "
            + "WHERE v.status = :status "
            + "ORDER BY v.updatedAt ASC")
    List<Vocabulary> findPending(@Param("status") ContentStatus status);

    @Query("SELECT v FROM Vocabulary v LEFT JOIN FETCH v.createdBy "
            + "WHERE v.status = :status AND v.jlptLevel = :level "
            + "ORDER BY v.updatedAt ASC")
    List<Vocabulary> findPending(@Param("status") ContentStatus status, @Param("level") JlptLevel level);

    @Query("SELECT v FROM Vocabulary v LEFT JOIN FETCH v.createdBy WHERE v.id = :id AND v.status <> :deleted")
    Optional<Vocabulary> findActiveById(@Param("id") Long id, @Param("deleted") ContentStatus deleted);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Vocabulary v SET v.status = :to, v.approvedBy = :mgr, v.publishedAt = :now, v.updatedAt = :now "
            + "WHERE v.id = :id AND v.status = :from")
    int approve(
            @Param("id") Long id,
            @Param("mgr") StaffUser mgr,
            @Param("now") LocalDateTime now,
            @Param("from") ContentStatus from,
            @Param("to") ContentStatus to);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Vocabulary v SET v.status = :to, v.updatedAt = :now WHERE v.id = :id AND v.status = :from")
    int transition(
            @Param("id") Long id,
            @Param("now") LocalDateTime now,
            @Param("from") ContentStatus from,
            @Param("to") ContentStatus to);
}
