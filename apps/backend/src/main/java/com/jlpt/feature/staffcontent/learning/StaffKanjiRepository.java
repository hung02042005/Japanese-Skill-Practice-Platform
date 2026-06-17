/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.staffcontent.learning;

import com.jlpt.feature.learning.Kanji;
import com.jlpt.feature.student.StudentUser.JlptLevel;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * UC-27 — Repository for Staff kanji read/write operations.
 */
@Repository
public interface StaffKanjiRepository extends JpaRepository<Kanji, Long> {

    /** FR-27-21: enforce uniqueness of {@code character_value}. */
    boolean existsByCharacterValue(String characterValue);

    /** Fetch a kanji entry that is not soft-deleted (use with {@code ContentStatus.DELETED}). */
    Optional<Kanji> findByIdAndStatusNot(Long id, Kanji.ContentStatus status);

    /**
     * UC-27 — Filtered paginated query for staff listing their own kanji.
     * All params except {@code staffId} and {@code deletedStatus} are optional.
     */
    @Query(
            value =
                    """
            SELECT k FROM Kanji k
            WHERE k.createdBy.id = :staffId
              AND (:jlptLevel IS NULL OR k.jlptLevel = :jlptLevel)
              AND ((:status IS NULL AND k.status <> :deletedStatus) OR k.status = :status)
              AND (:q IS NULL OR LOWER(k.characterValue) LIKE LOWER(CONCAT('%', :q, '%'))
                   OR LOWER(k.meaning) LIKE LOWER(CONCAT('%', :q, '%')))
            """,
            countQuery =
                    """
            SELECT COUNT(k) FROM Kanji k
            WHERE k.createdBy.id = :staffId
              AND (:jlptLevel IS NULL OR k.jlptLevel = :jlptLevel)
              AND ((:status IS NULL AND k.status <> :deletedStatus) OR k.status = :status)
              AND (:q IS NULL OR LOWER(k.characterValue) LIKE LOWER(CONCAT('%', :q, '%'))
                   OR LOWER(k.meaning) LIKE LOWER(CONCAT('%', :q, '%')))
            """)
    Page<Kanji> findByCreatedByWithFilters(
            @Param("staffId") Long staffId,
            @Param("jlptLevel") JlptLevel jlptLevel,
            @Param("status") Kanji.ContentStatus status,
            @Param("deletedStatus") Kanji.ContentStatus deletedStatus,
            @Param("q") String q,
            Pageable pageable);
}
