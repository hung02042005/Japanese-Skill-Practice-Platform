package com.jlpt.feature.student.flashcard;

import com.jlpt.feature.learning.Flashcard;
import com.jlpt.feature.learning.Flashcard.ContentType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StudentFlashcardRepository extends JpaRepository<Flashcard, Long> {
    Optional<Flashcard> findByStudentIdAndContentTypeAndContentId(Long studentId, ContentType contentType, Long contentId);
}
