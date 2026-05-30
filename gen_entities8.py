import os
DIR = "apps/backend/src/main/java/com/jlpt/entity"
FILES = {}

FILES["Flashcard.java"] = """package com.jlpt.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "flashcards")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
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

    public enum ContentType { KANJI("kanji"), VOCABULARY("vocabulary"), GRAMMAR("grammar"), CUSTOM("custom");
        private final String v; ContentType(String v) { this.v = v; } public String getValue() { return v; } }
    public enum LastRating { EASY("easy"), HARD("hard"), WRONG("wrong");
        private final String v; LastRating(String v) { this.v = v; } public String getValue() { return v; } }
}
"""

FILES["Ticket.java"] = """package com.jlpt.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "tickets")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ticket_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private StudentUser student;

    @Column(nullable = false, length = 255)
    private String subject;

    @Column(nullable = false, columnDefinition = "NVARCHAR(MAX)")
    private String content;

    @Column(length = 50)
    private String category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private Priority priority = Priority.NORMAL;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private TicketStatus status = TicketStatus.OPEN;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_to")
    private StaffUser assignedTo;

    @Column(name = "last_reply_at")
    private LocalDateTime lastReplyAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    public enum Priority { LOW("low"), NORMAL("normal"), HIGH("high"), URGENT("urgent");
        private final String v; Priority(String v) { this.v = v; } public String getValue() { return v; } }
    public enum TicketStatus { OPEN("open"), IN_PROGRESS("in_progress"), RESOLVED("resolved"), CLOSED("closed");
        private final String v; TicketStatus(String v) { this.v = v; } public String getValue() { return v; } }
}
"""

for fname, content in FILES.items():
    with open(os.path.join(DIR, fname), "w", encoding="utf-8") as f:
        f.write(content.lstrip())
    print(f"OK {fname}")
