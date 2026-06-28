/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.flashcard.repository;

import com.jlpt.feature.flashcard.Flashcard;
import com.jlpt.feature.learning.Kanji;
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

    // ── Sort sổ tay (3B): deck-scoped, lọc dueOnly tùy chọn ───────────────────
    // Cờ dueOnly cho qua mọi thẻ khi false; chỉ thẻ đến hạn khi true (gộp 2 tab trong 1 query).

    // sort=recent: thẻ mới thêm lên đầu (createdAt DESC). Không cần join nguồn.
    @Query(
            """
            SELECT f FROM Flashcard f
            WHERE f.student.id = :studentId
              AND f.deck.id = :deckId
              AND (:dueOnly = false OR f.nextReviewDate <= :today)
            ORDER BY f.createdAt DESC
            """)
    Page<Flashcard> findByDeckOrderByRecent(
            @Param("studentId") Long studentId,
            @Param("deckId") Long deckId,
            @Param("dueOnly") boolean dueOnly,
            @Param("today") LocalDate today,
            Pageable pageable);

    // sort=alpha: theo mặt trước (v.word) A→Z. Sổ "Từ cần ôn lại" chỉ chứa VOCABULARY nên join
    // Vocabulary an toàn (không mất thẻ) và phân trang ở DB đúng — tránh resolve-rồi-sort phá paging.
    @Query(
            value =
                    """
                    SELECT f FROM Flashcard f, Vocabulary v
                    WHERE f.student.id = :studentId
                      AND f.deck.id = :deckId
                      AND f.contentType = com.jlpt.feature.flashcard.Flashcard$ContentType.VOCABULARY
                      AND f.contentId = v.id
                      AND v.status = :status
                      AND (:dueOnly = false OR f.nextReviewDate <= :today)
                    ORDER BY v.word ASC
                    """,
            countQuery =
                    """
                    SELECT COUNT(f) FROM Flashcard f, Vocabulary v
                    WHERE f.student.id = :studentId
                      AND f.deck.id = :deckId
                      AND f.contentType = com.jlpt.feature.flashcard.Flashcard$ContentType.VOCABULARY
                      AND f.contentId = v.id
                      AND v.status = :status
                      AND (:dueOnly = false OR f.nextReviewDate <= :today)
                    """)
    Page<Flashcard> findByDeckOrderByWord(
            @Param("studentId") Long studentId,
            @Param("deckId") Long deckId,
            @Param("status") Kanji.ContentStatus status,
            @Param("dueOnly") boolean dueOnly,
            @Param("today") LocalDate today,
            Pageable pageable);

    // sort=level: theo cấp độ JLPT (N5→N1) rồi tới mặt trước. Cùng lý do join Vocabulary như alpha.
    @Query(
            value =
                    """
                    SELECT f FROM Flashcard f, Vocabulary v
                    WHERE f.student.id = :studentId
                      AND f.deck.id = :deckId
                      AND f.contentType = com.jlpt.feature.flashcard.Flashcard$ContentType.VOCABULARY
                      AND f.contentId = v.id
                      AND v.status = :status
                      AND (:dueOnly = false OR f.nextReviewDate <= :today)
                    ORDER BY v.jlptLevel ASC, v.word ASC
                    """,
            countQuery =
                    """
                    SELECT COUNT(f) FROM Flashcard f, Vocabulary v
                    WHERE f.student.id = :studentId
                      AND f.deck.id = :deckId
                      AND f.contentType = com.jlpt.feature.flashcard.Flashcard$ContentType.VOCABULARY
                      AND f.contentId = v.id
                      AND v.status = :status
                      AND (:dueOnly = false OR f.nextReviewDate <= :today)
                    """)
    Page<Flashcard> findByDeckOrderByLevel(
            @Param("studentId") Long studentId,
            @Param("deckId") Long deckId,
            @Param("status") Kanji.ContentStatus status,
            @Param("dueOnly") boolean dueOnly,
            @Param("today") LocalDate today,
            Pageable pageable);

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

    // Soft delete (ADR-004) nhiều thẻ cùng lúc — gỡ hàng loạt khỏi sổ tay (3B). Quyền sở hữu ép
    // ngay trong WHERE (AND student.id) → không xóa nhầm thẻ của người khác; trả số dòng đã gỡ.
    @Modifying
    @Query("UPDATE Flashcard f SET f.isDeleted = true WHERE f.id IN :ids AND f.student.id = :studentId")
    int softDeleteByIds(@Param("ids") Collection<Long> ids, @Param("studentId") Long studentId);

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

    // Đếm số TỪ VỰNG "đã học" theo từng chủ đề cho thanh tiến độ VocabHome (#5).
    // Loại "phantom learned" (1D): thẻ tạo lười khi build phiên (addedReason='learn') mà CHƯA từng ôn
    // (lastReviewedAt IS NULL) không tính là đã học. Batched 1 query (GROUP BY) để tránh N+1.
    @Query(
            """
            SELECT v.topicRef.id, COUNT(f) FROM Flashcard f, Vocabulary v
            WHERE f.student.id = :studentId
              AND f.contentType = com.jlpt.feature.flashcard.Flashcard$ContentType.VOCABULARY
              AND f.contentId = v.id
              AND v.status = :status
              AND v.topicRef.id IN :topicIds
              AND NOT (f.addedReason = 'learn' AND f.lastReviewedAt IS NULL)
            GROUP BY v.topicRef.id
            """)
    List<Object[]> countLearnedVocabByTopics(
            @Param("studentId") Long studentId,
            @Param("status") Kanji.ContentStatus status,
            @Param("topicIds") Collection<Long> topicIds);

    // Đếm số TỪ VỰNG "thành thạo" (repetitionCount >= ngưỡng, FR §5) theo từng chủ đề.
    @Query(
            """
            SELECT v.topicRef.id, COUNT(f) FROM Flashcard f, Vocabulary v
            WHERE f.student.id = :studentId
              AND f.contentType = com.jlpt.feature.flashcard.Flashcard$ContentType.VOCABULARY
              AND f.contentId = v.id
              AND v.status = :status
              AND v.topicRef.id IN :topicIds
              AND f.repetitionCount >= :threshold
            GROUP BY v.topicRef.id
            """)
    List<Object[]> countMasteredVocabByTopics(
            @Param("studentId") Long studentId,
            @Param("status") Kanji.ContentStatus status,
            @Param("topicIds") Collection<Long> topicIds,
            @Param("threshold") int threshold);
}
