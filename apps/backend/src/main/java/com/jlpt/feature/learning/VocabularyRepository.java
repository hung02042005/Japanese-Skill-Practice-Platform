/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.learning;

import com.jlpt.feature.student.StudentUser;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface VocabularyRepository extends JpaRepository<Vocabulary, Long> {

    @Query(
            """
            SELECT v FROM Vocabulary v
            WHERE v.status = :status
              AND (:jlptLevel IS NULL OR v.jlptLevel = :jlptLevel)
              AND (v.word LIKE CONCAT('%', :q, '%')
                OR v.furigana LIKE CONCAT('%', :q, '%')
                OR v.meaning LIKE CONCAT('%', :q, '%'))
            ORDER BY v.word ASC
            """)
    List<Vocabulary> searchPublished(
            @Param("q") String q,
            @Param("jlptLevel") StudentUser.JlptLevel jlptLevel,
            @Param("status") Kanji.ContentStatus status,
            Pageable pageable);

    @Query(
            """
            SELECT v FROM Vocabulary v
            LEFT JOIN v.topicRef t
            WHERE v.status = :status
              AND (:jlptLevel IS NULL OR v.jlptLevel = :jlptLevel)
              AND (:topic IS NULL
                OR v.topic = :topic
                OR t.slug = :topic
                OR t.titleVi = :topic)
              AND (:q IS NULL OR v.word LIKE CONCAT('%', :q, '%')
                OR v.furigana LIKE CONCAT('%', :q, '%')
                OR v.meaning LIKE CONCAT('%', :q, '%'))
            ORDER BY v.jlptLevel ASC, v.topic ASC, v.word ASC
            """)
    Page<Vocabulary> findPublished(
            @Param("status") Kanji.ContentStatus status,
            @Param("jlptLevel") StudentUser.JlptLevel jlptLevel,
            @Param("topic") String topic,
            @Param("q") String q,
            Pageable pageable);

    @Query(
            """
            SELECT DISTINCT v.topic FROM Vocabulary v
            WHERE v.status = :status
              AND v.jlptLevel = :jlptLevel
              AND v.topic IS NOT NULL
              AND v.topic <> ''
            ORDER BY v.topic ASC
            """)
    List<String> findDistinctTopics(
            @Param("status") Kanji.ContentStatus status, @Param("jlptLevel") StudentUser.JlptLevel jlptLevel);

    @Query(
            """
            SELECT COUNT(v) FROM Vocabulary v
            WHERE v.status = :status
              AND (:jlptLevel IS NULL OR v.jlptLevel = :jlptLevel)
            """)
    long countPublished(
            @Param("status") Kanji.ContentStatus status, @Param("jlptLevel") StudentUser.JlptLevel jlptLevel);

    @Query(
            """
            SELECT COUNT(v) FROM Vocabulary v
            WHERE v.status = :status
              AND v.topicRef.id = :topicId
            """)
    long countPublishedByTopic(@Param("status") Kanji.ContentStatus status, @Param("topicId") Long topicId);

    // Toàn bộ từ vựng published của một (cấp độ, chủ đề) — phiên học theo giáo trình (§3.7, FR-FC-62).
    @Query(
            """
            SELECT v FROM Vocabulary v
            LEFT JOIN v.topicRef t
            WHERE v.status = :status
              AND v.jlptLevel = :jlptLevel
              AND (v.topic = :topic
                OR t.slug = :topic
                OR t.titleVi = :topic)
            ORDER BY v.word ASC
            """)
    List<Vocabulary> findPublishedByLevelAndTopic(
            @Param("status") Kanji.ContentStatus status,
            @Param("jlptLevel") StudentUser.JlptLevel jlptLevel,
            @Param("topic") String topic);

    // Pool ứng viên cho đáp án nhiễu (distractor) theo cấp độ — batch một lần, tránh N+1 (FR-FC-54).
    @Query(
            """
            SELECT v FROM Vocabulary v
            WHERE v.status = :status
              AND v.jlptLevel = :jlptLevel
            ORDER BY v.id ASC
            """)
    List<Vocabulary> findPublishedByLevel(
            @Param("status") Kanji.ContentStatus status,
            @Param("jlptLevel") StudentUser.JlptLevel jlptLevel,
            Pageable pageable);
}
