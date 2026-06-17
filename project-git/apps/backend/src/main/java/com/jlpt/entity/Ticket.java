/* (c) JLPT E-Learning Platform */
package com.jlpt.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;

@Entity
@Table(name = "tickets")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ticket_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private StudentUser student;

    @Column(nullable = false, length = 255)
    private String subject;

    @Column(nullable = false, columnDefinition = "NVARCHAR(MAX)")
    private String content;

    @Column(length = 50)
    private String category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private Priority priority = Priority.NORMAL;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private TicketStatus status = TicketStatus.OPEN;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_to")
    private StaffUser assignedTo;

    @Column(name = "last_reply_at")
    private LocalDateTime lastReplyAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    public enum Priority {
        LOW("low"),
        NORMAL("normal"),
        HIGH("high"),
        URGENT("urgent");
        private final String v;

        Priority(String v) {
            this.v = v;
        }

        public String getValue() {
            return v;
        }
    }

    public enum TicketStatus {
        OPEN("open"),
        IN_PROGRESS("in_progress"),
        RESOLVED("resolved"),
        CLOSED("closed");
        private final String v;

        TicketStatus(String v) {
            this.v = v;
        }

        public String getValue() {
            return v;
        }
    }
}
