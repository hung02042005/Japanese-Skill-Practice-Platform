/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.flashcard.entity;

import com.jlpt.feature.student.entity.StudentUser;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.*;

@Entity
@Table(name = "flashcards")
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

    @Column(name = "deck_name", nullable = false, length = 255)
    @Builder.Default
    private String deckName = "Mặc định";

    @Column(name = "is_system", nullable = false)
    @Builder.Default
    private Boolean isSystem = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "content_type", nullable = false, length = 20)
    private ContentType contentType;

    @Column(name = "content_id")
    private Long contentId;

    @Column(name = "front_text", columnDefinition = "NVARCHAR(MAX)")
    private String frontText;

    @Column(name = "back_text", columnDefinition = "NVARCHAR(MAX)")
    private String backText;

    @Enumerated(EnumType.STRING)
    @Column(name = "last_rating", length = 10)
    private LastRating lastRating;

    @Column(name = "interval_days", nullable = false)
    @Builder.Default
    private Integer intervalDays = 1;

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
