/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.student.progress;

import com.jlpt.feature.student.StudentContentProgress;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * UC-06 — Repository for Student learning progress.
 */
@Repository
public interface StudentContentProgressRepository extends JpaRepository<StudentContentProgress, Long> {

    Optional<StudentContentProgress> findByStudentIdAndContentTypeAndContentId(
            Long studentId, StudentContentProgress.ContentType contentType, Long contentId);

    List<StudentContentProgress> findByStudentIdAndContentTypeAndContentIdIn(
            Long studentId, StudentContentProgress.ContentType contentType, Collection<Long> contentIds);

    @org.springframework.transaction.annotation.Transactional
    void deleteByStudentIdAndContentType(Long studentId, StudentContentProgress.ContentType contentType);

    @org.springframework.data.jpa.repository.Query("SELECT COUNT(p) FROM StudentContentProgress p, Kanji k " +
            "WHERE p.contentId = k.id AND p.student.id = :studentId AND p.contentType = :contentType " +
            "AND p.status = :status AND k.jlptLevel = :level")
    long countCompletedKanjiByLevel(
            @org.springframework.data.repository.query.Param("studentId") Long studentId,
            @org.springframework.data.repository.query.Param("level") com.jlpt.feature.student.StudentUser.JlptLevel level,
            @org.springframework.data.repository.query.Param("contentType") com.jlpt.feature.student.StudentContentProgress.ContentType contentType,
            @org.springframework.data.repository.query.Param("status") com.jlpt.feature.student.StudentContentProgress.ProgressStatus status);
}
