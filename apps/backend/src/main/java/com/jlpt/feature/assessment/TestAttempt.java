/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.assessment;

import com.jlpt.feature.student.StudentUser;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.*;

@Entity
@Table(name = "test_attempts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TestAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "attempt_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private StudentUser student;

    @Convert(converter = TestAttemptTypeConverter.class)
    @Column(name = "attempt_type", nullable = false, length = 20)
    private AttemptType attemptType;

    @Convert(converter = TestAttemptParentTypeConverter.class)
    @Column(name = "parent_type", nullable = false, length = 30)
    private ParentType parentType;

    @Column(name = "parent_id")
    private Long parentId;

    @Column(name = "started_at", nullable = false)
    @Builder.Default
    private LocalDateTime startedAt = LocalDateTime.now();

    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;

    @Column(name = "duration_seconds")
    private Integer durationSeconds;

    @Column(name = "total_score", precision = 8, scale = 2)
    private BigDecimal totalScore;

    @Column(name = "max_score", precision = 8, scale = 2)
    private BigDecimal maxScore;

    @Column(name = "is_passed")
    private Boolean isPassed;

    @Column(name = "language_knowledge_score", precision = 8, scale = 2)
    private BigDecimal languageKnowledgeScore;

    @Column(name = "reading_score", precision = 8, scale = 2)
    private BigDecimal readingScore;

    @Column(name = "listening_score", precision = 8, scale = 2)
    private BigDecimal listeningScore;

    @Convert(converter = TestAttemptStatusConverter.class)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private AttemptStatus status = AttemptStatus.IN_PROGRESS;

    public enum AttemptType {
        EXAM("exam"),
        QUIZ("quiz"),
        PRACTICE("practice"),
        READING("reading"),
        LISTENING("listening");
        private final String v;

        AttemptType(String v) {
            this.v = v;
        }

        public String getValue() {
            return v;
        }
    }

    public enum ParentType {
        ASSESSMENT("assessment"),
        LESSON("lesson"),
        RANDOM_PRACTICE("random_practice");
        private final String v;

        ParentType(String v) {
            this.v = v;
        }

        public String getValue() {
            return v;
        }
    }

    public enum AttemptStatus {
        IN_PROGRESS("in_progress"),
        SUBMITTED("submitted"),
        AUTO_SUBMITTED("auto_submitted"),
        ABANDONED("abandoned");
        private final String v;

        AttemptStatus(String v) {
            this.v = v;
        }

        public String getValue() {
            return v;
        }
    }
}
