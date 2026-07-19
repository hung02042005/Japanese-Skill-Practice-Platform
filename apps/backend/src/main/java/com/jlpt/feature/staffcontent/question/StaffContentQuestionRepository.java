/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.staffcontent.question;

import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * UC-24 — Repository for Staff question-bank operations on the {@code questions} table.
 */
@Repository
public interface StaffContentQuestionRepository extends JpaRepository<StaffContentQuestionEntity, Long> {

    /**
     * Fetch a question that is not soft-deleted.
     */
    Optional<StaffContentQuestionEntity> findByIdAndStatusNot(Long id, String status);

    /**
     * Filtered search with AND semantics (FR-24-11/12).
     * When a param is null it is skipped. Excludes 'deleted' by default unless explicitly requested.
     */
    @Query(
            value =
                    """
            SELECT q FROM StaffContentQuestionEntity q
            WHERE (:q IS NULL OR LOWER(q.questionText) LIKE LOWER(CONCAT('%', :q, '%')))
              AND (:skill IS NULL OR q.skill = :skill)
              AND (:jlptLevel IS NULL OR q.jlptLevel = :jlptLevel)
              AND (:questionType IS NULL OR q.questionType = :questionType)
              AND ((:status IS NULL AND q.status <> 'deleted') OR q.status = :status)
            """)
    Page<StaffContentQuestionEntity> findFiltered(
            @Param("q") String q,
            @Param("skill") String skill,
            @Param("jlptLevel") String jlptLevel,
            @Param("questionType") String questionType,
            @Param("status") String status,
            Pageable pageable);
}
