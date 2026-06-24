/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.assessment.repository;

import com.jlpt.feature.assessment.entity.TestAttempt;
import com.jlpt.feature.student.entity.StudentUser;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Owned by Người 3 (feat-assessment). Người 5 injects READ-ONLY — no save/delete calls.
 */
@Repository
public interface TestAttemptRepository extends JpaRepository<TestAttempt, Long> {

    @Query("""
            SELECT t FROM TestAttempt t
            WHERE t.student.id = :studentId
              AND t.attemptType IN :types
            ORDER BY t.startedAt DESC
            """)
    Page<TestAttempt> findByStudentIdAndAttemptTypeIn(
            @Param("studentId") Long studentId,
            @Param("types") List<TestAttempt.AttemptType> types,
            Pageable pageable);

    @Query("""
            SELECT COUNT(t) FROM TestAttempt t
            WHERE t.student.id = :studentId
              AND t.attemptType = com.jlpt.feature.assessment.entity.TestAttempt.AttemptType.EXAM
              AND t.status IN (
                  com.jlpt.feature.assessment.entity.TestAttempt.AttemptStatus.SUBMITTED,
                  com.jlpt.feature.assessment.entity.TestAttempt.AttemptStatus.AUTO_SUBMITTED)
            """)
    long countExamsByStudentId(@Param("studentId") Long studentId);

    @Query("""
            SELECT AVG(t.totalScore) FROM TestAttempt t
            WHERE t.student.id = :studentId
              AND t.attemptType = com.jlpt.feature.assessment.entity.TestAttempt.AttemptType.EXAM
              AND t.status IN (
                  com.jlpt.feature.assessment.entity.TestAttempt.AttemptStatus.SUBMITTED,
                  com.jlpt.feature.assessment.entity.TestAttempt.AttemptStatus.AUTO_SUBMITTED)
            """)
    BigDecimal avgExamScoreByStudentId(@Param("studentId") Long studentId);

    @Query("""
            SELECT MAX(t.totalScore) FROM TestAttempt t
            WHERE t.student.id = :studentId
              AND t.attemptType = com.jlpt.feature.assessment.entity.TestAttempt.AttemptType.EXAM
              AND t.status IN (
                  com.jlpt.feature.assessment.entity.TestAttempt.AttemptStatus.SUBMITTED,
                  com.jlpt.feature.assessment.entity.TestAttempt.AttemptStatus.AUTO_SUBMITTED)
            """)
    BigDecimal maxExamScoreByStudentId(@Param("studentId") Long studentId);

    /** For quiz stats: all attempts against a given parent (assessment). */
    @Query("""
            SELECT t FROM TestAttempt t
            WHERE t.parentId = :parentId
              AND t.parentType = com.jlpt.feature.assessment.entity.TestAttempt.ParentType.ASSESSMENT
              AND t.status IN (
                  com.jlpt.feature.assessment.entity.TestAttempt.AttemptStatus.SUBMITTED,
                  com.jlpt.feature.assessment.entity.TestAttempt.AttemptStatus.AUTO_SUBMITTED)
            """)
    List<TestAttempt> findSubmittedByParentId(@Param("parentId") Long parentId);

    /** Quiz stats: count total submitted attempts for an assessment. */
    @Query("""
            SELECT COUNT(t) FROM TestAttempt t
            WHERE t.parentId = :parentId
              AND t.parentType = com.jlpt.feature.assessment.entity.TestAttempt.ParentType.ASSESSMENT
              AND t.status IN (
                  com.jlpt.feature.assessment.entity.TestAttempt.AttemptStatus.SUBMITTED,
                  com.jlpt.feature.assessment.entity.TestAttempt.AttemptStatus.AUTO_SUBMITTED)
            """)
    long countSubmittedByParentId(@Param("parentId") Long parentId);

    /** Quiz stats: average score for an assessment. */
    @Query("""
            SELECT AVG(t.totalScore) FROM TestAttempt t
            WHERE t.parentId = :parentId
              AND t.parentType = com.jlpt.feature.assessment.entity.TestAttempt.ParentType.ASSESSMENT
              AND t.status IN (
                  com.jlpt.feature.assessment.entity.TestAttempt.AttemptStatus.SUBMITTED,
                  com.jlpt.feature.assessment.entity.TestAttempt.AttemptStatus.AUTO_SUBMITTED)
            """)
    BigDecimal avgScoreByParentId(@Param("parentId") Long parentId);

    /** Quiz stats: count passed attempts. */
    @Query("""
            SELECT COUNT(t) FROM TestAttempt t
            WHERE t.parentId = :parentId
              AND t.parentType = com.jlpt.feature.assessment.entity.TestAttempt.ParentType.ASSESSMENT
              AND t.isPassed = true
              AND t.status IN (
                  com.jlpt.feature.assessment.entity.TestAttempt.AttemptStatus.SUBMITTED,
                  com.jlpt.feature.assessment.entity.TestAttempt.AttemptStatus.AUTO_SUBMITTED)
            """)
    long countPassedByParentId(@Param("parentId") Long parentId);

    /** Admin dashboard: count all attempts started today. */
    @Query(value = "SELECT COUNT(*) FROM test_attempts WHERE CAST(started_at AS DATE) = CAST(GETUTCDATE() AS DATE)", nativeQuery = true)
    long countTodayAttempts();

    /** Admin report: count total exam attempts within a date range. */
    @Query("""
            SELECT COUNT(t) FROM TestAttempt t
            WHERE t.attemptType = com.jlpt.feature.assessment.entity.TestAttempt.AttemptType.EXAM
              AND t.status IN (
                  com.jlpt.feature.assessment.entity.TestAttempt.AttemptStatus.SUBMITTED,
                  com.jlpt.feature.assessment.entity.TestAttempt.AttemptStatus.AUTO_SUBMITTED)
              AND t.submittedAt >= :from AND t.submittedAt <= :to
            """)
    long countExamAttemptsBetween(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    /** Admin report: average exam score within a date range. */
    @Query("""
            SELECT AVG(t.totalScore) FROM TestAttempt t
            WHERE t.attemptType = com.jlpt.feature.assessment.entity.TestAttempt.AttemptType.EXAM
              AND t.status IN (
                  com.jlpt.feature.assessment.entity.TestAttempt.AttemptStatus.SUBMITTED,
                  com.jlpt.feature.assessment.entity.TestAttempt.AttemptStatus.AUTO_SUBMITTED)
              AND t.submittedAt >= :from AND t.submittedAt <= :to
            """)
    BigDecimal avgExamScoreBetween(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    /** Admin report: count distinct students who passed an exam at the given JLPT level. */
    @Query("""
            SELECT COUNT(DISTINCT t.student.id) FROM TestAttempt t
            WHERE t.parentType = com.jlpt.feature.assessment.entity.TestAttempt.ParentType.ASSESSMENT
              AND t.isPassed = true
              AND t.status IN (
                  com.jlpt.feature.assessment.entity.TestAttempt.AttemptStatus.SUBMITTED,
                  com.jlpt.feature.assessment.entity.TestAttempt.AttemptStatus.AUTO_SUBMITTED)
              AND t.parentId IN (
                  SELECT a.id FROM com.jlpt.feature.assessment.entity.Assessment a
                  WHERE a.jlptLevel = :level)
            """)
    long countDistinctPassedStudentsByJlptLevel(@Param("level") StudentUser.JlptLevel level);
}
