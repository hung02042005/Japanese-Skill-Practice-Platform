/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.staffcontent.quiz;

import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface QuizAssessmentRepository extends JpaRepository<QuizAssessmentEntity, Long> {

    /**
     * FR-26-10..12: paginated quizzes (assessment_type = 'quiz'), newest first, with optional
     * AND-combined filters. Deleted quizzes are excluded unless {@code status = 'deleted'} is supplied.
     */
    @Query("SELECT a FROM QuizAssessmentEntity a WHERE a.assessmentType = 'quiz' "
            + "AND (:level IS NULL OR a.jlptLevel = :level) "
            + "AND ((:status IS NULL AND a.status <> 'deleted') OR a.status = :status) "
            + "AND (:lessonId IS NULL OR a.lessonId = :lessonId) "
            + "ORDER BY a.updatedAt DESC")
    Page<QuizAssessmentEntity> findQuizzesWithFilters(
            @Param("level") String level,
            @Param("status") String status,
            @Param("lessonId") Long lessonId,
            Pageable pageable);

    /** Fetch a single non-deleted quiz by id. */
    Optional<QuizAssessmentEntity> findByIdAndAssessmentTypeAndStatusNot(Long id, String assessmentType, String status);
}
