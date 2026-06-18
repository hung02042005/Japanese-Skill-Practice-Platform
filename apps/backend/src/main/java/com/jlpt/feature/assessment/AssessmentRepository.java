/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.assessment;

import com.jlpt.feature.learning.Kanji;
import com.jlpt.feature.student.StudentUser;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AssessmentRepository extends JpaRepository<Assessment, Long> {

    @Query(
            """
            SELECT a FROM Assessment a
            WHERE a.assessmentType = :assessmentType
              AND a.status = :status
              AND (:jlptLevel IS NULL OR a.jlptLevel = :jlptLevel)
              AND (:topic IS NULL OR LOWER(a.topic) = LOWER(:topic))
            ORDER BY a.publishedAt DESC
            """)
    Page<Assessment> findAllByAssessmentTypeAndJlptLevelAndStatus(
            @Param("assessmentType") Assessment.AssessmentType assessmentType,
            @Param("status") Kanji.ContentStatus status,
            @Param("jlptLevel") StudentUser.JlptLevel jlptLevel,
            @Param("topic") String topic,
            Pageable pageable);

    Optional<Assessment> findByIdAndStatus(Long id, Kanji.ContentStatus status);

    boolean existsByTitle(String title);
}
