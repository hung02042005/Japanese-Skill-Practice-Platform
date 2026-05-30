package com.jlpt.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "lessons")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Lesson {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "lesson_id")
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "lesson_type", nullable = false, length = 20)
    @Builder.Default
    private LessonType lessonType = LessonType.LESSON;

    @Column(nullable = false, length = 255)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(name = "jlpt_level", nullable = false, length = 5)
    private StudentUser.JlptLevel jlptLevel;

    @Column(name = "content_text", columnDefinition = "NVARCHAR(MAX)")
    private String contentText;

    @Column(name = "video_url", length = 500)
    private String videoUrl;

    @Column(name = "audio_url", length = 500)
    private String audioUrl;

    @Column(name = "attachment_url", length = 500)
    private String attachmentUrl;

    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String explanation;

    @Column(name = "display_order", nullable = false)
    @Builder.Default
    private Integer displayOrder = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private LessonStatus status = LessonStatus.DRAFT;

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

    public enum LessonType { LESSON("lesson"), READING("reading"), LISTENING("listening"), SPEAKING("speaking");
        private final String v; LessonType(String v) { this.v = v; } public String getValue() { return v; } }
    public enum LessonStatus {
        DRAFT("draft"), PENDING_REVIEW("pending_review"), REJECTED("rejected"),
        PUBLISHED("published"), ARCHIVED("archived"), DELETED("deleted");
        private final String v; LessonStatus(String v) { this.v = v; } public String getValue() { return v; } }
}
