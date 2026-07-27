/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.publishedcontent.repository;

import com.jlpt.feature.learning.Kanji.ContentStatus;
import com.jlpt.feature.learning.Vocabulary;
import com.jlpt.feature.student.StudentUser.JlptLevel;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/** UC-34 — Quản lý trạng thái cho {@code vocabulary} (FR-34-03/10). */
@Repository
public interface ManagedVocabularyRepository extends JpaRepository<Vocabulary, Long> {

    @Query("SELECT v FROM Vocabulary v "
            + "WHERE v.status = :published AND (:level IS NULL OR v.jlptLevel = :level) "
            + "ORDER BY v.publishedAt DESC")
    List<Vocabulary> findPublished(
            @Param("published") ContentStatus publishedStatus, @Param("level") JlptLevel jlptLevel);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Vocabulary v SET v.status = :to, v.updatedAt = :now WHERE v.id = :id AND v.status = :from")
    int transition(
            @Param("id") Long contentId,
            @Param("from") ContentStatus expectedStatus,
            @Param("to") ContentStatus targetStatus,
            @Param("now") LocalDateTime changeTimestamp);
}
