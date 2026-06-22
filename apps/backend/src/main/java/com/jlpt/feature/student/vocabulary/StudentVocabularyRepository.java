package com.jlpt.feature.student.vocabulary;

import com.jlpt.feature.learning.Kanji;
import com.jlpt.feature.learning.Vocabulary;
import com.jlpt.feature.student.StudentUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StudentVocabularyRepository extends JpaRepository<Vocabulary, Long> {

    @Query("SELECT v FROM Vocabulary v WHERE " +
           "(:level IS NULL OR v.jlptLevel = :level) AND " +
           "(:topic IS NULL OR v.topic = :topic) AND " +
           "v.status = :status")
    Page<Vocabulary> findByFilters(
            @Param("level") StudentUser.JlptLevel level,
            @Param("topic") String topic,
            @Param("status") Kanji.ContentStatus status,
            Pageable pageable);

    Optional<Vocabulary> findByIdAndStatus(Long id, Kanji.ContentStatus status);

    Optional<Vocabulary> findFirstByJlptLevelAndTopicAndStatusAndIdLessThanOrderByIdDesc(
            StudentUser.JlptLevel level, String topic, Kanji.ContentStatus status, Long id);

    Optional<Vocabulary> findFirstByJlptLevelAndTopicAndStatusAndIdGreaterThanOrderByIdAsc(
            StudentUser.JlptLevel level, String topic, Kanji.ContentStatus status, Long id);

    @Query("SELECT COUNT(v) FROM Vocabulary v JOIN StudentContentProgress p ON p.contentId = v.id " +
           "WHERE p.student.id = :studentId AND p.contentType = 'VOCABULARY' " +
           "AND p.status = 'COMPLETED' AND v.jlptLevel = :level")
    long countCompletedVocabularyByLevel(@Param("studentId") Long studentId, @Param("level") StudentUser.JlptLevel level);
}
