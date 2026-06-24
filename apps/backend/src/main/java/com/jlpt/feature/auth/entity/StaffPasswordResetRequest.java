/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.auth.entity;

import com.jlpt.feature.auth.converter.StaffPasswordResetStatusConverter;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;

@Entity
@Table(name = "staff_password_reset_requests")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StaffPasswordResetRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "request_id")
    private Long id;

    @Column(name = "staff_id", nullable = false)
    private Long staffId;

    @Convert(converter = StaffPasswordResetStatusConverter.class)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private ResetStatus status = ResetStatus.PENDING;

    @Column(name = "requested_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime requestedAt = LocalDateTime.now();

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "completed_by")
    private Long completedBy;

    @Column(name = "request_ip", length = 45)
    private String requestIp;

    public enum ResetStatus {
        PENDING("pending"),
        COMPLETED("completed"),
        EXPIRED("expired"),
        CANCELLED("cancelled");

        private final String value;

        ResetStatus(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }
}
