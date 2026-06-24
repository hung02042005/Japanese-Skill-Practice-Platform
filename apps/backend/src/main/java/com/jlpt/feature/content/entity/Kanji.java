/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.content.entity;

import com.jlpt.feature.student.entity.StudentUser;
import com.jlpt.feature.staff.entity.StaffUser;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;

@Entity
@Table(name = "kanji")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Kanji {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "kanji_id")
    private Long id;

    @Column(name = "character_value", nullable = false, unique = true, length = 5)
    private String characterValue;

    @Column(nullable = false, length = 500)
    private String meaning;

    @Column(length = 200)
    private String onyomi;

    @Column(length = 200)
    private String kunyomi;

    @Column(name = "stroke_count")
    private Integer strokeCount;

    @Enumerated(EnumType.STRING)
    @Column(name = "jlpt_level", nullable = false, length = 5)
    private StudentUser.JlptLevel jlptLevel;

    @Column(name = "stroke_order_url", length = 500)
    private String strokeOrderUrl;

    @Column(name = "example_word", length = 100)
    private String exampleWord;

    @Column(name = "example_reading", length = 200)
    private String exampleReading;

    @Column(name = "example_meaning", length = 500)
    private String exampleMeaning;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private ContentStatus status = ContentStatus.DRAFT;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private StaffUser createdBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by")
    private StaffUser approvedBy;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public enum ContentStatus {
        DRAFT("draft"),
        PENDING_REVIEW("pending_review"),
        REJECTED("rejected"),
        PUBLISHED("published"),
        ARCHIVED("archived"),
        DELETED("deleted");
        private final String v;

        ContentStatus(String v) {
            this.v = v;
        }

        public String getValue() {
            return v;
        }
    }
}
