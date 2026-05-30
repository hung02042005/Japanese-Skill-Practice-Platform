import os
DIR = "apps/backend/src/main/java/com/jlpt/entity"

FILES = {}
FILES["KanaCharacter.java"] = """package com.jlpt.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "kana_characters")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class KanaCharacter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "kana_id")
    private Integer id;

    @Column(name = "character_value", nullable = false, length = 5)
    private String characterValue;

    @Column(nullable = false, length = 10)
    private String romaji;

    @Enumerated(EnumType.STRING)
    @Column(name = "kana_type", nullable = false, length = 15)
    private KanaType kanaType;

    @Column(name = "audio_url", length = 500)
    private String audioUrl;

    @Column(name = "stroke_order_url", length = 500)
    private String strokeOrderUrl;

    @Column(name = "display_order", nullable = false)
    @Builder.Default
    private Integer displayOrder = 0;

    public enum KanaType { HIRAGANA("hiragana"), KATAKANA("katakana");
        private final String v; KanaType(String v) { this.v = v; } public String getValue() { return v; } }
}
"""

FILES["Kanji.java"] = """package com.jlpt.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "kanji")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
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
    protected void onUpdate() { updatedAt = LocalDateTime.now(); }

    public enum ContentStatus {
        DRAFT("draft"), PENDING_REVIEW("pending_review"), REJECTED("rejected"),
        PUBLISHED("published"), ARCHIVED("archived"), DELETED("deleted");
        private final String v; ContentStatus(String v) { this.v = v; } public String getValue() { return v; } }
}
"""

FILES["Vocabulary.java"] = """package com.jlpt.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "vocabulary")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Vocabulary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "vocabulary_id")
    private Long id;

    @Column(nullable = false, length = 100)
    private String word;

    @Column(length = 200)
    private String furigana;

    @Column(nullable = false, length = 500)
    private String meaning;

    @Column(name = "word_type", length = 50)
    private String wordType;

    @Enumerated(EnumType.STRING)
    @Column(name = "jlpt_level", nullable = false, length = 5)
    private StudentUser.JlptLevel jlptLevel;

    @Column(length = 100)
    private String topic;

    @Column(name = "audio_url", length = 500)
    private String audioUrl;

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

for fname, content in FILES.items():
    with open(os.path.join(DIR, fname), "w", encoding="utf-8") as f:
        f.write(content.lstrip())
    print(f"OK {fname}")
