import os
DIR = "apps/backend/src/main/java/com/jlpt/entity"
FILES = {}

FILES["Assessment.java"] = """package com.jlpt.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "assessments")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Assessment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "assessment_id")
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "assessment_type", nullable = false, length = 20)
    private AssessmentType assessmentType;

    @Column(nullable = false, length = 255)
    private String title;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lesson_id")
    private Lesson lesson;

    @Column(length = 100)
    private String topic;

    @Enumerated(EnumType.STRING)
    @Column(name = "jlpt_level", length = 5)
    private StudentUser.JlptLevel jlptLevel;

    @Column(name = "duration_min")
    private Integer durationMin;

    @Column(name = "pass_score")
    private Integer passScore;

    @Column(name = "total_score")
    private Integer totalScore;

    @Column(name = "audio_url", length = 500)
    private String audioUrl;

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

    public enum AssessmentType { QUIZ("quiz"), EXAM("exam");
        private final String v; AssessmentType(String v) { this.v = v; } public String getValue() { return v; } }
}
"""

FILES["QuestionAssignment.java"] = """package com.jlpt.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "question_assignments")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class QuestionAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "assignment_id")
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "parent_type", nullable = false, length = 30)
    private ParentType parentType;

    @Column(name = "parent_id", nullable = false)
    private Long parentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    @Column(name = "section_name", length = 100)
    private String sectionName;

    @Column(nullable = false, precision = 6, scale = 2)
    @Builder.Default
    private BigDecimal score = BigDecimal.ONE;

    @Column(name = "display_order", nullable = false)
    @Builder.Default
    private Integer displayOrder = 0;

    public enum ParentType { ASSESSMENT("assessment"), LESSON("lesson");
        private final String v; ParentType(String v) { this.v = v; } public String getValue() { return v; } }
}
"""

for fname, content in FILES.items():
    with open(os.path.join(DIR, fname), "w", encoding="utf-8") as f:
        f.write(content.lstrip())
    print(f"OK {fname}")
