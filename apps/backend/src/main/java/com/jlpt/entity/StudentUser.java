/* (c) JLPT E-Learning Platform */
package com.jlpt.entity;

import com.jlpt.converter.OauthProviderConverter;
import com.jlpt.converter.StudentStatusConverter;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "student_users")
@SQLRestriction("status <> 'DELETED' and status <> 'deleted'")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "student_id")
    private Long id;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(name = "password_hash", length = 255)
    private String passwordHash;

    @Column(name = "full_name", nullable = false, length = 150)
    private String fullName;

    @Convert(converter = StudentStatusConverter.class)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private StudentStatus status = StudentStatus.ACTIVE;

    @Column(name = "suspend_reason", length = 500)
    private String suspendReason;

    @Column(name = "email_verified_at")
    private LocalDateTime emailVerifiedAt;

    @Column(name = "avatar_url", length = 500)
    private String avatarUrl;

    @Column(length = 20)
    private String phone;

    @Convert(converter = OauthProviderConverter.class)
    @Column(name = "oauth_provider", length = 30)
    private OauthProvider oauthProvider;

    @Column(name = "oauth_provider_id", length = 255)
    private String oauthProviderId;

    @Column(name = "oauth_provider_email", length = 255)
    private String oauthProviderEmail;

    @Column(name = "oauth_linked_at")
    private LocalDateTime oauthLinkedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "current_jlpt_level", length = 5)
    private JlptLevel currentJlptLevel;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_jlpt_level", length = 5)
    private JlptLevel targetJlptLevel;

    @Column(name = "current_streak", nullable = false)
    @Builder.Default
    private Integer currentStreak = 0;

    @Column(name = "longest_streak", nullable = false)
    @Builder.Default
    private Integer longestStreak = 0;

    @Column(name = "last_activity_date")
    private LocalDate lastActivityDate;

    @Column(name = "login_attempts", nullable = false)
    @Builder.Default
    private Integer loginAttempts = 0;

    @Column(name = "locked_until")
    private LocalDateTime lockedUntil;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

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

    public enum StudentStatus {
        ACTIVE("active"),
        SUSPENDED("suspended"),
        PENDING("pending"),
        DELETED("deleted");
        private final String v;

        StudentStatus(String v) {
            this.v = v;
        }

        public String getValue() {
            return v;
        }
    }

    public enum OauthProvider {
        GOOGLE("google"),
        FACEBOOK("facebook"),
        APPLE("apple"),
        GITHUB("github");
        private final String v;

        OauthProvider(String v) {
            this.v = v;
        }

        public String getValue() {
            return v;
        }
    }

    public enum JlptLevel {
        N5,
        N4,
        N3,
        N2,
        N1
    }
}
