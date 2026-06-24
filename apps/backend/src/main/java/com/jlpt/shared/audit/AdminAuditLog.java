/* (c) JLPT E-Learning Platform */
package com.jlpt.shared.audit;

import com.jlpt.feature.admin.entity.AdminUser;
import com.jlpt.feature.staff.entity.StaffUser;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;

@Entity
@Table(name = "admin_audit_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminAuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "audit_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_actor_id")
    private AdminUser adminActor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "staff_actor_id")
    private StaffUser staffActor;

    @Column(nullable = false, length = 100)
    private String action;

    @Column(name = "target_table", length = 100)
    private String targetTable;

    @Column(name = "target_id")
    private Long targetId;

    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String description;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
