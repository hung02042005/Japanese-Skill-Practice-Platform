import os

DIR = "apps/backend/src/main/java/com/jlpt/entity"
os.makedirs(DIR, exist_ok=True)

FILES = {}

FILES["AuthToken.java"] = """package com.jlpt.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "auth_tokens")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class AuthToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "token_id")
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "actor_type", nullable = false, length = 20)
    private ActorType actorType;

    @Column(name = "admin_id")
    private Long adminId;

    @Column(name = "staff_id")
    private Long staffId;

    @Column(name = "student_id")
    private Long studentId;

    @Enumerated(EnumType.STRING)
    @Column(name = "token_type", nullable = false, length = 30)
    private TokenType tokenType;

    @Column(name = "token_value", nullable = false, length = 500)
    private String tokenValue;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "revoked_at")
    private LocalDateTime revokedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    public enum ActorType { ADMIN("admin"), STAFF("staff"), STUDENT("student");
        private final String v; ActorType(String v) { this.v = v; } public String getValue() { return v; } }
    public enum TokenType {
        SESSION("session"), EMAIL_VERIFICATION("email_verification"),
        PASSWORD_RESET("password_reset"), TFA_TEMP("2fa_temp"), REFRESH("refresh");
        private final String v; TokenType(String v) { this.v = v; } public String getValue() { return v; } }
}
"""

FILES["Lesson.java"] = """package com.jlpt.entity;

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
"""

for fname, content in FILES.items():
    path = os.path.join(DIR, fname)
    with open(path, "w", encoding="utf-8") as f:
        f.write(content.lstrip())
    print(f"OK {fname}")
