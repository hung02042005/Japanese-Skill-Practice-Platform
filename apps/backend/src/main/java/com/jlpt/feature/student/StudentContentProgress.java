/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.student;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.*;

@Entity
@Table(name = "student_content_progress")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentContentProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "progress_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private StudentUser student;

    @Convert(converter = ContentProgressTypeConverter.class)
    @Column(name = "content_type", nullable = false, length = 30)
    private ContentType contentType;

    @Column(name = "content_id", nullable = false)
    private Long contentId;

    @Convert(converter = ContentProgressStatusConverter.class)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private ProgressStatus status = ProgressStatus.LEARNING;

    @Column(name = "progress_percent", nullable = false, precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal progressPercent = BigDecimal.ZERO;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "last_studied_at", nullable = false)
    @Builder.Default
    private LocalDateTime lastStudiedAt = LocalDateTime.now();

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    public enum ContentType {
        LESSON("lesson"),
        VOCABULARY("vocabulary"),
        KANJI("kanji"),
        KANA("kana"),
        GRAMMAR("grammar");
        private final String v;

        ContentType(String v) {
            this.v = v;
        }

        public String getValue() {
            return v;
        }
    }

    public enum ProgressStatus {
        LEARNING("learning"),
        COMPLETED("completed"),
        REVIEWING("reviewing");
        private final String v;

        ProgressStatus(String v) {
            this.v = v;
        }

        public String getValue() {
            return v;
        }
    }
}
