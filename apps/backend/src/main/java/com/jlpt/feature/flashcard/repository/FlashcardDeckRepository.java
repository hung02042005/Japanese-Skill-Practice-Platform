/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.flashcard.repository;

import com.jlpt.feature.flashcard.FlashcardDeck;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface FlashcardDeckRepository extends JpaRepository<FlashcardDeck, Long> {

    // @SQLRestriction("is_deleted = 0") đã lọc deck đã xóa cho mọi truy vấn dưới đây.

    Optional<FlashcardDeck> findByIdAndStudentId(Long deckId, Long studentId);

    Optional<FlashcardDeck> findByStudentIdAndName(Long studentId, String name);

    boolean existsByStudentIdAndName(Long studentId, String name);

    // Sổ auto "Từ cần ôn lại" — get-or-create (UQ 1 sổ/student, FR-FC-40).
    Optional<FlashcardDeck> findByStudentIdAndIsReviewDeckTrue(Long studentId);

    /**
     * Tóm tắt deck của học viên + deck hệ thống, kèm tổng số thẻ và số thẻ đến hạn hôm nay.
     * LEFT JOIN để deck rỗng vẫn hiển thị (FR-FC-07). Thẻ đã xóa bị loại bởi @SQLRestriction
     * trên Flashcard. Trả Object[]: {deckId, name, description, jlptLevel, topic, color,
     * isSystem, totalCards, dueToday, isReviewDeck}.
     */
    @Query(
            """
            SELECT d.id, d.name, d.description, d.jlptLevel, d.topic, d.color, d.isSystem,
                   COUNT(f.id),
                   SUM(CASE WHEN f.nextReviewDate <= :today THEN 1 ELSE 0 END),
                   d.isReviewDeck
            FROM FlashcardDeck d
            LEFT JOIN d.flashcards f
            WHERE d.student.id = :studentId OR d.isSystem = true
            GROUP BY d.id, d.name, d.description, d.jlptLevel, d.topic, d.color, d.isSystem, d.isReviewDeck, d.displayOrder
            ORDER BY d.displayOrder ASC, d.id ASC
            """)
    List<Object[]> findDeckSummaries(@Param("studentId") Long studentId, @Param("today") LocalDate today);
}
