/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.staffcontent.grammar;

import com.jlpt.feature.learning.GrammarPoint;
import com.jlpt.feature.learning.Kanji;
import com.jlpt.feature.student.StudentUser;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * UC-25 — Repository for Staff grammar operations.
 */
@Repository
public interface StaffGrammarRepository extends JpaRepository<GrammarPoint, Long> {

    /**
     * Filtered search for a specific staff's grammars (FR-09).
     * Excludes soft-deleted records by default unless explicitly querying for deleted (handled in service).
     */
    @Query(
            """
            SELECT g FROM GrammarPoint g
            WHERE g.createdBy.id = :staffId
              AND (:jlptLevel IS NULL OR g.jlptLevel = :jlptLevel)
              AND ((:status IS NULL AND g.status <> :deletedStatus) OR g.status = :status)
            """)
    Page<GrammarPoint> findByCreatedByWithFilters(
            @Param("staffId") Long staffId,
            @Param("jlptLevel") StudentUser.JlptLevel jlptLevel,
            @Param("status") Kanji.ContentStatus status,
            @Param("deletedStatus") Kanji.ContentStatus deletedStatus,
            Pageable pageable);

    /**
     * Fetch an active grammar by ID with its lesson.
     */
    @Query("SELECT g FROM GrammarPoint g LEFT JOIN FETCH g.lesson WHERE g.id = :id AND g.status <> :deletedStatus")
    Optional<GrammarPoint> findActiveByIdWithLesson(
            @Param("id") Long id, @Param("deletedStatus") Kanji.ContentStatus deletedStatus);

    /**
     * Fetch an active grammar by ID.
     */
    Optional<GrammarPoint> findByIdAndStatusNot(Long id, Kanji.ContentStatus status);
}
