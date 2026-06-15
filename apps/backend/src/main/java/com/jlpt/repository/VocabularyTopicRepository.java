/* (c) JLPT E-Learning Platform */
package com.jlpt.repository;

import com.jlpt.entity.Kanji;
import com.jlpt.entity.StudentUser;
import com.jlpt.entity.VocabularyTopic;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VocabularyTopicRepository extends JpaRepository<VocabularyTopic, Long> {

    List<VocabularyTopic> findByJlptLevelAndStatusOrderByDisplayOrderAsc(
            StudentUser.JlptLevel jlptLevel, Kanji.ContentStatus status);

    Optional<VocabularyTopic> findFirstByJlptLevelAndSlugAndStatus(
            StudentUser.JlptLevel jlptLevel, String slug, Kanji.ContentStatus status);

    long countByJlptLevelAndStatus(StudentUser.JlptLevel jlptLevel, Kanji.ContentStatus status);
}
