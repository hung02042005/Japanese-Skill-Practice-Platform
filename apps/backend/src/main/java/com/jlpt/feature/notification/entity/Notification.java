/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.notification.entity;

import com.jlpt.feature.admin.entity.AdminUser;
import com.jlpt.feature.staff.entity.StaffUser;
import com.jlpt.feature.student.entity.StudentUser;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;

@Entity
@Table(name = "notifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notification_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private StudentUser student;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(nullable = false, columnDefinition = "NVARCHAR(MAX)")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(name = "notification_type", nullable = false, length = 30)
    @Builder.Default
    private NotificationType notificationType = NotificationType.NEWS;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private Channel channel = Channel.IN_APP;

    @Column(name = "is_auto", nullable = false)
    @Builder.Default
    private Boolean isAuto = false;

    @Column(name = "rule_key", length = 100)
    private String ruleKey;

    @Column(name = "scheduled_at")
    private LocalDateTime scheduledAt;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    @Column(name = "is_read", nullable = false)
    @Builder.Default
    private Boolean isRead = false;

    @Column(name = "read_at")
    private LocalDateTime readAt;

    @Column(name = "delivered_at")
    private LocalDateTime deliveredAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_creator_id")
    private AdminUser adminCreator;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "staff_creator_id")
    private StaffUser staffCreator;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    public enum NotificationType {
        NEWS("news"),
        WARNING("warning"),
        PROMOTION("promotion"),
        SYSTEM("system"),
        ACHIEVEMENT("achievement"),
        REMINDER("reminder");
        private final String v;

        NotificationType(String v) {
            this.v = v;
        }

        public String getValue() {
            return v;
        }
    }

    public enum Channel {
        IN_APP("in_app"),
        EMAIL("email"),
        BOTH("both");
        private final String v;

        Channel(String v) {
            this.v = v;
        }

        public String getValue() {
            return v;
        }
    }
}
