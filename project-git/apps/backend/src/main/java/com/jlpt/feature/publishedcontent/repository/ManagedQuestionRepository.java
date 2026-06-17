/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.publishedcontent.repository;

import com.jlpt.feature.assessment.Question;
import com.jlpt.feature.assessment.Question.ContentStatus;
import com.jlpt.feature.student.StudentUser.JlptLevel;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/** UC-34 — Quản lý trạng thái cho {@code questions} (FR-34-03/10). */
@Repository
public interface ManagedQuestionRepository extends JpaRepository<Question, Long> {

    @Query("SELECT q FROM Question q "
            + "WHERE q.status = :published AND (:level IS NULL OR q.jlptLevel = :level) "
            + "ORDER BY q.publishedAt DESC")
    List<Question> findPublished(@Param("published") ContentStatus published, @Param("level") JlptLevel level);

    /** Guarded soft-delete/restore: chỉ đổi cột {@code status} khi đang ở {@code from} (FR-34-10/17/22). */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Question q SET q.status = :to, q.updatedAt = :now WHERE q.id = :id AND q.status = :from")
    int transition(
            @Param("id") Long id,
            @Param("from") ContentStatus from,
            @Param("to") ContentStatus to,
            @Param("now") LocalDateTime now);
}
