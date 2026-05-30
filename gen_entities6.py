import os
DIR = "apps/backend/src/main/java/com/jlpt/entity"
FILES = {}

FILES["TestAttempt.java"] = """package com.jlpt.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "test_attempts")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class TestAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "attempt_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private StudentUser student;

    @Enumerated(EnumType.STRING)
    @Column(name = "attempt_type", nullable = false, length = 20)
    private AttemptType attemptType;

    @Enumerated(EnumType.STRING)
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

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private AttemptStatus status = AttemptStatus.IN_PROGRESS;

    public enum AttemptType { EXAM("exam"), QUIZ("quiz"), PRACTICE("practice"), READING("reading"), LISTENING("listening");
        private final String v; AttemptType(String v) { this.v = v; } public String getValue() { return v; } }
    public enum ParentType { ASSESSMENT("assessment"), LESSON("lesson"), RANDOM_PRACTICE("random_practice");
        private final String v; ParentType(String v) { this.v = v; } public String getValue() { return v; } }
    public enum AttemptStatus { IN_PROGRESS("in_progress"), SUBMITTED("submitted"), AUTO_SUBMITTED("auto_submitted"), ABANDONED("abandoned");
        private final String v; AttemptStatus(String v) { this.v = v; } public String getValue() { return v; } }
}
"""

FILES["AttemptAnswer.java"] = """package com.jlpt.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "attempt_answers")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class AttemptAnswer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "answer_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attempt_id", nullable = false)
    private TestAttempt attempt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    @Column(name = "selected_option", length = 1)
    private String selectedOption;

    @Column(name = "answer_text", columnDefinition = "NVARCHAR(MAX)")
    private String answerText;

    @Column(name = "is_correct")
    private Boolean isCorrect;

    @Column(precision = 6, scale = 2)
    private BigDecimal score;

    @Column(name = "answered_at", nullable = false)
    @Builder.Default
    private LocalDateTime answeredAt = LocalDateTime.now();
}
"""

for fname, content in FILES.items():
    with open(os.path.join(DIR, fname), "w", encoding="utf-8") as f:
        f.write(content.lstrip())
    print(f"OK {fname}")
