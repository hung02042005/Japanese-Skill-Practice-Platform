/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.flashcard;

import com.jlpt.feature.student.StudentUser;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "flashcards")
@SQLRestriction("is_deleted = 0")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Flashcard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "flashcard_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id")
    private StudentUser student;

    // Sổ tay first-class (V9). Nguồn sự thật của deck; cột deck_name đã bỏ ở V15 (dùng deck.name).
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "deck_id")
    private FlashcardDeck deck;

    @Column(name = "is_system", nullable = false)
    @Builder.Default
    private Boolean isSystem = false;

    @Column(name = "is_deleted", nullable = false)
    @Builder.Default
    private Boolean isDeleted = false;

    @Convert(converter = FlashcardContentTypeConverter.class)
    @Column(name = "content_type", nullable = false, length = 20)
    private ContentType contentType;

    @Column(name = "content_id")
    private Long contentId;

    // Vì sao thẻ vào sổ (SPEC-notebook §7): 'wrong' | 'manual' | 'learn'. NULL với thẻ cũ.
    @Column(name = "added_reason", length = 20)
    private String addedReason;

    // UUID phiên ôn gần nhất đóng dấu lên thẻ (V17) — gom "từ sai trong phiên" theo session_id
    // thay cửa sổ thời gian 2h. NULL với thẻ chưa từng ôn trong phiên có session.
    @Column(name = "last_session_id", length = 36)
    private String lastSessionId;

    @Column(name = "front_text", columnDefinition = "NVARCHAR(MAX)")
    private String frontText;

    @Column(name = "back_text", columnDefinition = "NVARCHAR(MAX)")
    private String backText;

    @Convert(converter = FlashcardLastRatingConverter.class)
    @Column(name = "last_rating", length = 10)
    private LastRating lastRating;

    @Column(name = "interval_days", nullable = false)
    @Builder.Default
    private Integer intervalDays = 1;

    // SM-2 ease factor (FR-FC-23/24). Cột DECIMAL(5,2) đã có sẵn trong DB — không cần migration.
    @Column(name = "ease_factor", nullable = false, precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal easeFactor = new BigDecimal("2.50");

    @Column(name = "repetition_count", nullable = false)
    @Builder.Default
    private Integer repetitionCount = 0;

    @Column(name = "next_review_date")
    private LocalDate nextReviewDate;

    @Column(name = "last_reviewed_at")
    private LocalDateTime lastReviewedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    public enum ContentType {
        KANJI("kanji"),
        VOCABULARY("vocabulary"),
        GRAMMAR("grammar"),
        CUSTOM("custom");
        private final String v;

        ContentType(String v) {
            this.v = v;
        }

        public String getValue() {
            return v;
        }
    }

    public enum LastRating {
        EASY("easy"),
        HARD("hard"),
        WRONG("wrong");
        private final String v;

        LastRating(String v) {
            this.v = v;
        }

        public String getValue() {
            return v;
        }
    }
}
