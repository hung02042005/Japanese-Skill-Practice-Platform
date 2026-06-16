/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.student;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StudentContentProgressRepository extends JpaRepository<StudentContentProgress, Long> {

    Optional<StudentContentProgress> findByStudent_IdAndContentTypeAndContentId(
            Long studentId, StudentContentProgress.ContentType contentType, Long contentId);

    List<StudentContentProgress> findByStudent_IdAndContentTypeAndContentIdIn(
            Long studentId, StudentContentProgress.ContentType contentType, List<Long> contentIds);

    long countByStudent_IdAndContentTypeAndContentIdInAndStatus(
            Long studentId,
            StudentContentProgress.ContentType contentType,
            List<Long> contentIds,
            StudentContentProgress.ProgressStatus status);
}
