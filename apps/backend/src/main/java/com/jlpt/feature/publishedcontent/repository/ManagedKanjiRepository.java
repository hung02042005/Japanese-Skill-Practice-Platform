/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.publishedcontent.repository;

import com.jlpt.feature.learning.Kanji;
import com.jlpt.feature.learning.Kanji.ContentStatus;
import com.jlpt.feature.student.StudentUser.JlptLevel;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/** UC-34 — Quản lý trạng thái cho {@code kanji} (FR-34-03/10). */
@Repository
public interface ManagedKanjiRepository extends JpaRepository<Kanji, Long> {

    @Query("SELECT k FROM Kanji k "
            + "WHERE k.status = :published AND (:level IS NULL OR k.jlptLevel = :level) "
            + "ORDER BY k.publishedAt DESC")
    List<Kanji> findPublished(@Param("published") ContentStatus published, @Param("level") JlptLevel level);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Kanji k SET k.status = :to, k.updatedAt = :now WHERE k.id = :id AND k.status = :from")
    int transition(
            @Param("id") Long id,
            @Param("from") ContentStatus from,
            @Param("to") ContentStatus to,
            @Param("now") LocalDateTime now);
}
