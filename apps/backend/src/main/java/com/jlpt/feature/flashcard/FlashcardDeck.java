/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.flashcard;

import com.jlpt.feature.student.StudentUser;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;

/**
 * Sổ tay flashcard first-class (V9). Thay cho cột chuỗi {@code flashcards.deck_name}:
 * deck có id riêng, metadata, đổi tên rẻ (single-row UPDATE). Xem SPEC §5.
 *
 * <p>student_id NULL = deck hệ thống. is_review_deck = sổ auto "Từ cần ôn lại" (per-student).
 */
@Entity
@Table(name = "flashcard_decks")
@SQLRestriction("is_deleted = 0")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FlashcardDeck {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "deck_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id")
    private StudentUser student;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "jlpt_level", length = 5)
    private String jlptLevel;

    @Column(name = "topic", length = 100)
    private String topic;

    @Column(name = "color", length = 20)
    private String color;

    @Column(name = "display_order", nullable = false)
    @Builder.Default
    private Integer displayOrder = 0;

    @Column(name = "is_system", nullable = false)
    @Builder.Default
    private Boolean isSystem = false;

    @Column(name = "is_review_deck", nullable = false)
    @Builder.Default
    private Boolean isReviewDeck = false;

    @Column(name = "is_deleted", nullable = false)
    @Builder.Default
    private Boolean isDeleted = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();

    // Nghịch đảo của Flashcard.deck — chỉ dùng cho aggregate/JOIN, không cascade.
    @OneToMany(mappedBy = "deck", fetch = FetchType.LAZY)
    private List<Flashcard> flashcards;
}
