/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.staffcontent.question;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * UC-24 — Minimal repository for {@code attempt_answers} table.
 * Used to determine if a question is locked (has been answered by students).
 */
@Repository
public interface StaffContentAttemptAnswerRepository extends JpaRepository<StaffContentAttemptAnswerEntity, Long> {

    /**
     * Returns true if at least one answer record references the given question.
     */
    boolean existsByQuestionId(Long questionId);
}
