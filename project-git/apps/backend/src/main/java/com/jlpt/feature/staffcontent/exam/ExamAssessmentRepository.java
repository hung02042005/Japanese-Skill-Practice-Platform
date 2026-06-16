/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.staffcontent.exam;

import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ExamAssessmentRepository extends JpaRepository<ExamAssessmentEntity, Long> {

    /**
     * FR-28-10..12: paginated exams (assessment_type = 'exam'), newest first, with optional
     * AND-combined filters. Deleted exams are excluded unless {@code status = 'deleted'} is supplied.
     */
    @Query("SELECT a FROM ExamAssessmentEntity a WHERE a.assessmentType = 'exam' "
            + "AND (:level IS NULL OR a.jlptLevel = :level) "
            + "AND ((:status IS NULL AND a.status <> 'deleted') OR a.status = :status) "
            + "ORDER BY a.updatedAt DESC")
    Page<ExamAssessmentEntity> findExamsWithFilters(
            @Param("level") String level, @Param("status") String status, Pageable pageable);

    /** Fetch a single non-deleted exam by id. */
    Optional<ExamAssessmentEntity> findByIdAndAssessmentTypeAndStatusNot(Long id, String assessmentType, String status);
}
