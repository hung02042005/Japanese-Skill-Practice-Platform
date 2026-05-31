/* (c) JLPT E-Learning Platform */
package com.jlpt.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;

@Entity
@Table(name = "auth_tokens")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
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

    public enum ActorType {
        ADMIN("admin"),
        STAFF("staff"),
        STUDENT("student");
        private final String v;

        ActorType(String v) {
            this.v = v;
        }

        public String getValue() {
            return v;
        }
    }

    public enum TokenType {
        SESSION("session"),
        EMAIL_VERIFICATION("email_verification"),
        PASSWORD_RESET("password_reset"),
        TFA_TEMP("2fa_temp"),
        REFRESH("refresh");
        private final String v;

        TokenType(String v) {
            this.v = v;
        }

        public String getValue() {
            return v;
        }
    }
}
