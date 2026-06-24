/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.corelearning.repository;

import com.jlpt.feature.corelearning.entity.StudentSubmission;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Owned by Người 3 (feat-core-learning). Người 5 injects READ-ONLY + grade update — no delete calls.
 */
@Repository
public interface StudentSubmissionRepository extends JpaRepository<StudentSubmission, Long> {

    @Query("""
            SELECT s FROM StudentSubmission s
            WHERE s.student.id = :studentId
              AND (:type IS NULL OR s.submissionType = :type)
              AND (:status IS NULL OR s.status = :status)
            ORDER BY s.submittedAt DESC
            """)
    Page<StudentSubmission> findByStudentIdAndFilters(
            @Param("studentId") Long studentId,
            @Param("type") StudentSubmission.SubmissionType type,
            @Param("status") StudentSubmission.SubmissionStatus status,
            Pageable pageable);

    @Query("""
            SELECT s FROM StudentSubmission s
            WHERE (:type IS NULL OR s.submissionType = :type)
              AND (:status IS NULL OR s.status = :status)
            ORDER BY s.submittedAt DESC
            """)
    Page<StudentSubmission> findAllByTypeAndFilters(
            @Param("type") StudentSubmission.SubmissionType type,
            @Param("status") StudentSubmission.SubmissionStatus status,
            Pageable pageable);

    /** Count AI-graded speaking submissions not yet manually graded — for Staff dashboard. */
    @Query("""
            SELECT COUNT(s) FROM StudentSubmission s
            WHERE s.submissionType = com.jlpt.feature.corelearning.entity.StudentSubmission.SubmissionType.SPEAKING
              AND s.status = com.jlpt.feature.corelearning.entity.StudentSubmission.SubmissionStatus.AI_GRADED
              AND s.gradedBy IS NULL
            """)
    long countPendingManualGrade();

    /** Average final_score for speaking submissions of a student — for skill radar. */
    @Query("""
            SELECT AVG(s.aiOverallScore) FROM StudentSubmission s
            WHERE s.student.id = :studentId
              AND s.submissionType = com.jlpt.feature.corelearning.entity.StudentSubmission.SubmissionType.SPEAKING
              AND s.status IN (
                  com.jlpt.feature.corelearning.entity.StudentSubmission.SubmissionStatus.AI_GRADED,
                  com.jlpt.feature.corelearning.entity.StudentSubmission.SubmissionStatus.GRADED)
            """)
    Double avgSpeakingScoreByStudentId(@Param("studentId") Long studentId);
}
