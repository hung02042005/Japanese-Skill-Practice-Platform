/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.flashcard.repository;

import com.jlpt.feature.flashcard.FlashcardDeck;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface FlashcardDeckRepository extends JpaRepository<FlashcardDeck, Long> {

    // @SQLRestriction("is_deleted = 0") đã lọc deck đã xóa cho mọi truy vấn dưới đây.

    Optional<FlashcardDeck> findByStudentIdAndName(Long studentId, String name);

    // Sổ auto "Từ cần ôn lại" — get-or-create (UQ 1 sổ/student, FR-FC-40).
    Optional<FlashcardDeck> findByStudentIdAndIsReviewDeckTrue(Long studentId);

    /**
     * Tóm tắt deck của học viên + deck hệ thống, kèm tổng số thẻ. LEFT JOIN để deck rỗng vẫn hiển
     * thị (FR-FC-07). Thẻ đã xóa bị loại bởi @SQLRestriction trên Flashcard.
     * Trả Object[]: {deckId, name, totalCards, isReviewDeck}.
     */
    @Query(
            """
            SELECT d.id, d.name, COUNT(f.id), d.isReviewDeck
            FROM FlashcardDeck d
            LEFT JOIN d.flashcards f
            WHERE d.student.id = :studentId OR d.isSystem = true
            GROUP BY d.id, d.name, d.isReviewDeck, d.displayOrder
            ORDER BY d.displayOrder ASC, d.id ASC
            """)
    List<Object[]> findDeckSummaries(@Param("studentId") Long studentId);
}
