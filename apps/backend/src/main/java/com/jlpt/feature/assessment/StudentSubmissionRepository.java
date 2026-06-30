/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.assessment;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface StudentSubmissionRepository extends JpaRepository<StudentSubmission, Long> {

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

    // ── Dashboard (admin) — đếm theo trạng thái / mốc chấm ────────────────────
    @Query("SELECT COUNT(s) FROM StudentSubmission s WHERE s.status IN :statuses")
    long countByStatusIn(
            @Param("statuses") java.util.Collection<StudentSubmission.SubmissionStatus> statuses);
}
