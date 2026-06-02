/* (c) JLPT E-Learning Platform */
package com.jlpt.entity;

import com.jlpt.converter.StaffRoleConverter;
import com.jlpt.converter.StaffStatusConverter;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "staff_users")
@SQLRestriction("status <> 'DELETED' and status <> 'deleted'")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StaffUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "staff_id")
    private Long id;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(name = "password_hash", length = 255)
    private String passwordHash;

    @Column(name = "full_name", nullable = false, length = 150)
    private String fullName;

    @Convert(converter = StaffRoleConverter.class)
    @Column(name = "staff_role", nullable = false, length = 30)
    @Builder.Default
    private StaffRole staffRole = StaffRole.STAFF;

    @Convert(converter = StaffStatusConverter.class)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private StaffStatus status = StaffStatus.ACTIVE;

    @Column(name = "suspend_reason", length = 500)
    private String suspendReason;

    @Column(name = "email_verified_at")
    private LocalDateTime emailVerifiedAt;

    @Column(name = "login_attempts", nullable = false)
    @Builder.Default
    private Integer loginAttempts = 0;

    @Column(name = "locked_until")
    private LocalDateTime lockedUntil;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    @Column(name = "last_login_ip", length = 45)
    private String lastLoginIp;

    @Column(name = "password_changed_at")
    private LocalDateTime passwordChangedAt;

    @Column(name = "must_change_password", nullable = false)
    @Builder.Default
    private Boolean mustChangePassword = false;

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

    public enum StaffRole {
        STAFF("staff"),
        STAFF_MANAGER("staff_manager");
        private final String v;

        StaffRole(String v) {
            this.v = v;
        }

        public String getValue() {
            return v;
        }
    }

    public enum StaffStatus {
        ACTIVE("active"),
        SUSPENDED("suspended"),
        PENDING("pending"),
        DELETED("deleted");
        private final String v;

        StaffStatus(String v) {
            this.v = v;
        }

        public String getValue() {
            return v;
        }
    }
}
