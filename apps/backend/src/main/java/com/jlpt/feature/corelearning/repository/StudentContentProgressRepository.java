/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.corelearning.repository;

import com.jlpt.feature.corelearning.entity.StudentContentProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Owned by Người 3 (feat-core-learning). Người 5 injects READ-ONLY — no save/delete calls.
 */
@Repository
public interface StudentContentProgressRepository extends JpaRepository<StudentContentProgress, Long> {

    @Query("""
            SELECT COUNT(p) FROM StudentContentProgress p
            WHERE p.student.id = :studentId
              AND p.contentType = :contentType
              AND p.status = com.jlpt.feature.corelearning.entity.StudentContentProgress.ProgressStatus.COMPLETED
            """)
    long countCompletedByStudentIdAndContentType(
            @Param("studentId") Long studentId,
            @Param("contentType") StudentContentProgress.ContentType contentType);

    @Query("""
            SELECT COUNT(p) FROM StudentContentProgress p
            WHERE p.student.id = :studentId
              AND p.contentType = :contentType
            """)
    long countByStudentIdAndContentType(
            @Param("studentId") Long studentId,
            @Param("contentType") StudentContentProgress.ContentType contentType);
}
