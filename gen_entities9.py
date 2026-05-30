import os
DIR = "apps/backend/src/main/java/com/jlpt/entity"
FILES = {}

FILES["TicketReply.java"] = """package com.jlpt.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "ticket_replies")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class TicketReply {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reply_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_id", nullable = false)
    private Ticket ticket;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_sender_id")
    private StudentUser studentSender;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "staff_sender_id")
    private StaffUser staffSender;

    @Column(nullable = false, columnDefinition = "NVARCHAR(MAX)")
    private String message;

    @Column(name = "attachment_url", length = 500)
    private String attachmentUrl;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
"""

FILES["Notification.java"] = """package com.jlpt.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
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

    public enum NotificationType { NEWS("news"), WARNING("warning"), PROMOTION("promotion"), SYSTEM("system"), ACHIEVEMENT("achievement"), REMINDER("reminder");
        private final String v; NotificationType(String v) { this.v = v; } public String getValue() { return v; } }
    public enum Channel { IN_APP("in_app"), EMAIL("email"), BOTH("both");
        private final String v; Channel(String v) { this.v = v; } public String getValue() { return v; } }
}
"""

for fname, content in FILES.items():
    with open(os.path.join(DIR, fname), "w", encoding="utf-8") as f:
        f.write(content.lstrip())
    print(f"OK {fname}")
