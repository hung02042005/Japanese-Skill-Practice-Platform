import os
DIR = "apps/backend/src/main/java/com/jlpt/entity"
FILES = {}

FILES["StudentSubmission.java"] = """package com.jlpt.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "student_submissions")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class StudentSubmission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "submission_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private StudentUser student;

    @Enumerated(EnumType.STRING)
    @Column(name = "submission_type", nullable = false, length = 20)
    private SubmissionType submissionType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private SubmissionStatus status = SubmissionStatus.PENDING;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exercise_id")
    private Lesson exercise;

    @Column(name = "recording_url", length = 500)
    private String recordingUrl;

    @Column(name = "duration_seconds")
    private Integer durationSeconds;

    @Column(name = "ai_overall_score", precision = 5, scale = 2)
    private BigDecimal aiOverallScore;

    @Column(name = "ai_pronunciation_score", precision = 5, scale = 2)
    private BigDecimal aiPronunciationScore;

    @Column(name = "ai_fluency_score", precision = 5, scale = 2)
    private BigDecimal aiFluencyScore;

    @Column(name = "ai_error_summary", columnDefinition = "NVARCHAR(MAX)")
    private String aiErrorSummary;

    @Column(name = "ai_suggestions", columnDefinition = "NVARCHAR(MAX)")
    private String aiSuggestions;

    @Column(name = "ai_graded_at")
    private LocalDateTime aiGradedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "kanji_id")
    private Kanji kanji;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "kana_id")
    private KanaCharacter kana;

    @Column(name = "handwriting_image_url", length = 500)
    private String handwritingImageUrl;

    @Column(name = "expected_character", length = 5)
    private String expectedCharacter;

    @Column(name = "recognized_character", length = 5)
    private String recognizedCharacter;

    @Column(name = "similarity_percent", precision = 5, scale = 2)
    private BigDecimal similarityPercent;

    @Column(name = "is_correct")
    private Boolean isCorrect;

    @Column(name = "ocr_processed_at")
    private LocalDateTime ocrProcessedAt;

    @Column(name = "manual_score", precision = 5, scale = 2)
    private BigDecimal manualScore;

    @Column(name = "manual_feedback", columnDefinition = "NVARCHAR(MAX)")
    private String manualFeedback;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "graded_by")
    private StaffUser gradedBy;

    @Column(name = "graded_at")
    private LocalDateTime gradedAt;

    @Column(name = "submitted_at", nullable = false)
    @Builder.Default
    private LocalDateTime submittedAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PreUpdate
    protected void onUpdate() { updatedAt = LocalDateTime.now(); }

    public enum SubmissionType { SPEAKING("speaking"), HANDWRITING("handwriting");
        private final String v; SubmissionType(String v) { this.v = v; } public String getValue() { return v; } }
    public enum SubmissionStatus { PENDING("pending"), AI_GRADED("ai_graded"), GRADED("graded"), REJECTED("rejected");
        private final String v; SubmissionStatus(String v) { this.v = v; } public String getValue() { return v; } }
}
"""

FILES["StudentContentProgress.java"] = """package com.jlpt.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "student_content_progress")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class StudentContentProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "progress_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private StudentUser student;

    @Enumerated(EnumType.STRING)
    @Column(name = "content_type", nullable = false, length = 30)
    private ContentType contentType;

    @Column(name = "content_id", nullable = false)
    private Long contentId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private ProgressStatus status = ProgressStatus.LEARNING;

    @Column(name = "progress_percent", nullable = false, precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal progressPercent = BigDecimal.ZERO;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "is_bookmarked", nullable = false)
    @Builder.Default
    private Boolean isBookmarked = false;

    @Column(name = "bookmark_note", length = 500)
    private String bookmarkNote;

    @Column(name = "bookmarked_at")
    private LocalDateTime bookmarkedAt;

    @Column(name = "last_studied_at", nullable = false)
    @Builder.Default
    private LocalDateTime lastStudiedAt = LocalDateTime.now();

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    public enum ContentType { LESSON("lesson"), VOCABULARY("vocabulary"), KANJI("kanji"), KANA("kana"), GRAMMAR("grammar");
        private final String v; ContentType(String v) { this.v = v; } public String getValue() { return v; } }
    public enum ProgressStatus { LEARNING("learning"), COMPLETED("completed"), REVIEWING("reviewing");
        private final String v; ProgressStatus(String v) { this.v = v; } public String getValue() { return v; } }
}
"""

for fname, content in FILES.items():
    with open(os.path.join(DIR, fname), "w", encoding="utf-8") as f:
        f.write(content.lstrip())
    print(f"OK {fname}")
