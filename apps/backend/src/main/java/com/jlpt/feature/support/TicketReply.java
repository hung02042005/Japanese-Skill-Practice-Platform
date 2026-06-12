/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.support;

import com.jlpt.feature.staff.StaffUser;
import com.jlpt.feature.student.StudentUser;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;

@Entity
@Table(name = "ticket_replies")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
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
