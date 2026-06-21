/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.flashcard.repository;

import com.jlpt.feature.flashcard.Flashcard;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface FlashcardRepository extends JpaRepository<Flashcard, Long> {

    // Tóm tắt deck nay lấy từ FlashcardDeckRepository.findDeckSummaries (deck first-class, V9).

    @Query(
            """
            SELECT f FROM Flashcard f
            WHERE f.student.id = :studentId
              AND f.deck.id = :deckId
            ORDER BY f.nextReviewDate ASC
            """)
    Page<Flashcard> findAllByDeck(@Param("studentId") Long studentId, @Param("deckId") Long deckId, Pageable pageable);

    @Query(
            """
            SELECT f FROM Flashcard f
            WHERE f.student.id = :studentId
            ORDER BY f.nextReviewDate ASC
            """)
    Page<Flashcard> findAllByStudent(@Param("studentId") Long studentId, Pageable pageable);

    @Query(
            """
            SELECT f FROM Flashcard f
            WHERE f.student.id = :studentId
              AND f.deck.id = :deckId
              AND f.nextReviewDate <= :today
            ORDER BY f.nextReviewDate ASC
            """)
    Page<Flashcard> findDueByDeck(
            @Param("studentId") Long studentId,
            @Param("deckId") Long deckId,
            @Param("today") LocalDate today,
            Pageable pageable);

    @Query(
            """
            SELECT f FROM Flashcard f
            WHERE f.student.id = :studentId
              AND f.nextReviewDate <= :today
            ORDER BY f.nextReviewDate ASC
            """)
    Page<Flashcard> findAllDue(@Param("studentId") Long studentId, @Param("today") LocalDate today, Pageable pageable);

    // Toàn bộ thẻ của một deck (phiên học theo sổ tay, §3.6).
    @Query("SELECT f FROM Flashcard f WHERE f.student.id = :studentId AND f.deck.id = :deckId")
    List<Flashcard> findByStudentAndDeck(@Param("studentId") Long studentId, @Param("deckId") Long deckId);

    // Toàn bộ thẻ của student (tìm kiếm `q` không kèm deckId — resolve live rồi lọc theo mặt trước).
    @Query("SELECT f FROM Flashcard f WHERE f.student.id = :studentId")
    List<Flashcard> findByStudent(@Param("studentId") Long studentId);

    boolean existsByIdAndStudentId(Long flashcardId, Long studentId);

    // Soft delete (ADR-004) toàn bộ thẻ của một deck khi xóa sổ tay.
    @Modifying
    @Query("UPDATE Flashcard f SET f.isDeleted = true WHERE f.deck.id = :deckId")
    void softDeleteByDeckId(@Param("deckId") Long deckId);

    // Thẻ từ vựng bị chấm WRONG trong phiên (gom theo session_id — V17) — §3.5.
    @Query(
            """
            SELECT f FROM Flashcard f
            WHERE f.student.id = :studentId
              AND f.lastSessionId = :sessionId
              AND f.contentType = com.jlpt.feature.flashcard.Flashcard$ContentType.VOCABULARY
              AND f.lastRating = com.jlpt.feature.flashcard.Flashcard$LastRating.WRONG
            """)
    List<Flashcard> findWrongVocabCardsInSession(
            @Param("studentId") Long studentId, @Param("sessionId") String sessionId);

    @Query(
            """
            SELECT f FROM Flashcard f
            WHERE f.student.id = :studentId
              AND f.contentType = :contentType
              AND f.contentId = :contentId
            """)
    Optional<Flashcard> findByStudentAndContent(
            @Param("studentId") Long studentId,
            @Param("contentType") Flashcard.ContentType contentType,
            @Param("contentId") Long contentId);

    // Thẻ của student theo loại + tập contentId (điều hướng level+topic, §3.7 — tránh N+1).
    @Query(
            """
            SELECT f FROM Flashcard f
            WHERE f.student.id = :studentId
              AND f.contentType = :contentType
              AND f.contentId IN :contentIds
            """)
    List<Flashcard> findByStudentAndContentIds(
            @Param("studentId") Long studentId,
            @Param("contentType") Flashcard.ContentType contentType,
            @Param("contentIds") Collection<Long> contentIds);
}
