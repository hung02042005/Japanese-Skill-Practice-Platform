/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.learning.flashcard;

import com.jlpt.feature.learning.Flashcard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** UC-07 — Student-owned flashcards (BR-07-05). */
@Repository
public interface FlashcardRepository extends JpaRepository<Flashcard, Long> {

    boolean existsByStudent_IdAndContentTypeAndContentId(
            Long studentId, Flashcard.ContentType contentType, Long contentId);
}
