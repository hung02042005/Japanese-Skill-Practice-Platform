/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.publishedcontent.repository;

import com.jlpt.feature.learning.GrammarPoint;
import com.jlpt.feature.learning.Kanji.ContentStatus;
import com.jlpt.feature.student.StudentUser.JlptLevel;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/** UC-34 — Quản lý trạng thái cho {@code grammar_points} (FR-34-03/10). */
@Repository
public interface ManagedGrammarRepository extends JpaRepository<GrammarPoint, Long> {

    @Query("SELECT g FROM GrammarPoint g "
            + "WHERE g.status = :published AND (:level IS NULL OR g.jlptLevel = :level) "
            + "ORDER BY g.publishedAt DESC")
    List<GrammarPoint> findPublished(@Param("published") ContentStatus published, @Param("level") JlptLevel level);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE GrammarPoint g SET g.status = :to, g.updatedAt = :now WHERE g.id = :id AND g.status = :from")
    int transition(
            @Param("id") Long id,
            @Param("from") ContentStatus from,
            @Param("to") ContentStatus to,
            @Param("now") LocalDateTime now);
}
