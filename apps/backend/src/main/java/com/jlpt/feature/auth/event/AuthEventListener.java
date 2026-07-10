/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.auth.event;

import com.jlpt.shared.email.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuthEventListener {

    private final EmailService emailService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onSendVerificationEmailEvent(SendVerificationEmailEvent event) {
        log.info("[AuthEventListener] Triggering verification email for: {}", event.email());
        emailService.sendVerificationEmail(event.email(), event.token());
    }

    // BUG-02 FIX: forgotPassword dùng event pattern nhất quán với register/resend.
    // Email chỉ gửi SAU KHI transaction commit thành công → tránh gửi email khi DB rollback.
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onSendPasswordResetEmailEvent(SendPasswordResetEmailEvent event) {
        log.info("[AuthEventListener] Triggering password reset email for: {}", event.email());
        emailService.sendPasswordResetEmail(event.email(), event.token());
    }
}
