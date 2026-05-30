import os
DIR = "apps/backend/src/main/java/com/jlpt/entity"
FILES = {}

FILES["GrammarPoint.java"] = """package com.jlpt.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "grammar_points")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class GrammarPoint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "grammar_id")
    private Long id;

    @Column(nullable = false, length = 255)
    private String structure;

    @Column(length = 500)
    private String formula;

    @Column(nullable = false, length = 500)
    private String meaning;

    @Column(name = "usage_explanation", columnDefinition = "NVARCHAR(MAX)")
    private String usageExplanation;

    @Enumerated(EnumType.STRING)
    @Column(name = "jlpt_level", nullable = false, length = 5)
    private StudentUser.JlptLevel jlptLevel;

    @Column(name = "example_sentence_jp", columnDefinition = "NVARCHAR(MAX)")
    private String exampleSentenceJp;

    @Column(name = "example_sentence_vi", columnDefinition = "NVARCHAR(MAX)")
    private String exampleSentenceVi;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lesson_id")
    private Lesson lesson;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private Kanji.ContentStatus status = Kanji.ContentStatus.DRAFT;

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
    protected void onUpdate() { updatedAt = LocalDateTime.now(); }
}
"""

FILES["Question.java"] = """package com.jlpt.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "questions")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "question_id")
    private Long id;

    @Column(name = "question_text", nullable = false, columnDefinition = "NVARCHAR(MAX)")
    private String questionText;

    @Enumerated(EnumType.STRING)
    @Column(name = "question_type", nullable = false, length = 30)
    private QuestionType questionType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private Skill skill;

    @Enumerated(EnumType.STRING)
    @Column(name = "jlpt_level", nullable = false, length = 5)
    private StudentUser.JlptLevel jlptLevel;

    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String explanation;

    @Column(name = "audio_url", length = 500)
    private String audioUrl;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Column(name = "option_a", columnDefinition = "NVARCHAR(MAX)")
    private String optionA;

    @Column(name = "option_b", columnDefinition = "NVARCHAR(MAX)")
    private String optionB;

    @Column(name = "option_c", columnDefinition = "NVARCHAR(MAX)")
    private String optionC;

    @Column(name = "option_d", columnDefinition = "NVARCHAR(MAX)")
    private String optionD;

    @Column(name = "correct_option", length = 1)
    private String correctOption;

    @Column(name = "correct_answer_text", columnDefinition = "NVARCHAR(MAX)")
    private String correctAnswerText;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private StaffUser createdBy;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private Kanji.ContentStatus status = Kanji.ContentStatus.DRAFT;

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
    protected void onUpdate() { updatedAt = LocalDateTime.now(); }

    public enum QuestionType { MULTIPLE_CHOICE("multiple_choice"), FILL_BLANK("fill_blank"), TRUE_FALSE("true_false");
        private final String v; QuestionType(String v) { this.v = v; } public String getValue() { return v; } }
    public enum Skill { VOCABULARY("vocabulary"), GRAMMAR("grammar"), KANJI("kanji"), READING("reading"), LISTENING("listening"), MIXED("mixed");
        private final String v; Skill(String v) { this.v = v; } public String getValue() { return v; } }
}
"""

for fname, content in FILES.items():
    with open(os.path.join(DIR, fname), "w", encoding="utf-8") as f:
        f.write(content.lstrip())
    print(f"OK {fname}")
