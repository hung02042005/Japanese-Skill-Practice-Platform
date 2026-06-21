/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.staffcontent.learning;

import com.jlpt.feature.learning.Kanji;
import com.jlpt.feature.learning.Vocabulary;
import com.jlpt.feature.student.StudentUser.JlptLevel;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * UC-27 — Repository for Staff vocabulary operations.
 * Vocabulary reuses {@link Kanji.ContentStatus} as its status enum.
 */
@Repository
public interface StaffVocabularyRepository extends JpaRepository<Vocabulary, Long> {

    /** Fetch a vocabulary entry that is not soft-deleted (use with {@code ContentStatus.DELETED}). */
    Optional<Vocabulary> findByIdAndStatusNot(Long id, Kanji.ContentStatus status);

    /**
     * UC-27 — Filtered paginated query for staff listing their own vocabulary.
     * All params except {@code staffId} and {@code deletedStatus} are optional.
     */
    @Query(
            value =
                    """
            SELECT v FROM Vocabulary v
            WHERE v.createdBy.id = :staffId
              AND (:jlptLevel IS NULL OR v.jlptLevel = :jlptLevel)
              AND (:topicId IS NULL OR v.topicRef.id = :topicId)
              AND ((:status IS NULL AND v.status <> :deletedStatus) OR v.status = :status)
              AND (:q IS NULL OR LOWER(v.word) LIKE LOWER(CONCAT('%', :q, '%'))
                   OR LOWER(v.meaning) LIKE LOWER(CONCAT('%', :q, '%')))
            """,
            countQuery =
                    """
            SELECT COUNT(v) FROM Vocabulary v
            WHERE v.createdBy.id = :staffId
              AND (:jlptLevel IS NULL OR v.jlptLevel = :jlptLevel)
              AND (:topicId IS NULL OR v.topicRef.id = :topicId)
              AND ((:status IS NULL AND v.status <> :deletedStatus) OR v.status = :status)
              AND (:q IS NULL OR LOWER(v.word) LIKE LOWER(CONCAT('%', :q, '%'))
                   OR LOWER(v.meaning) LIKE LOWER(CONCAT('%', :q, '%')))
            """)
    Page<Vocabulary> findByCreatedByWithFilters(
            @Param("staffId") Long staffId,
            @Param("jlptLevel") JlptLevel jlptLevel,
            @Param("topicId") Long topicId,
            @Param("status") Kanji.ContentStatus status,
            @Param("deletedStatus") Kanji.ContentStatus deletedStatus,
            @Param("q") String q,
            Pageable pageable);
}
