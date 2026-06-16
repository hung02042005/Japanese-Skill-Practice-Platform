/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.learning.vocabulary;

import com.jlpt.feature.learning.Kanji;
import com.jlpt.feature.learning.Vocabulary;
import com.jlpt.feature.student.StudentUser;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

/** UC-09 — Student-facing read access to published vocabulary. */
@Repository
public interface VocabularyRepository extends JpaRepository<Vocabulary, Long> {

    Page<Vocabulary> findByJlptLevelAndStatus(
            StudentUser.JlptLevel level, Kanji.ContentStatus status, Pageable pageable);

    Page<Vocabulary> findByJlptLevelAndStatusAndTopic(
            StudentUser.JlptLevel level, Kanji.ContentStatus status, String topic, Pageable pageable);

    Optional<Vocabulary> findByIdAndStatus(Long id, Kanji.ContentStatus status);

    @Query("SELECT v.id FROM Vocabulary v WHERE v.jlptLevel = :level AND v.status = :status")
    List<Long> findIdsByJlptLevelAndStatus(StudentUser.JlptLevel level, Kanji.ContentStatus status);

    @Query(
            "SELECT DISTINCT v.topic FROM Vocabulary v"
                    + " WHERE v.jlptLevel = :level AND v.status = :status AND v.topic IS NOT NULL"
                    + " ORDER BY v.topic")
    List<String> findDistinctTopicsByJlptLevelAndStatus(
            StudentUser.JlptLevel level, Kanji.ContentStatus status);
}
