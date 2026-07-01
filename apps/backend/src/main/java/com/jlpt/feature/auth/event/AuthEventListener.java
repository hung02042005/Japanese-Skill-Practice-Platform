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
        log.info("[AuthEventListener] Triggering email sending for: {}", event.email());
        emailService.sendVerificationEmail(event.email(), event.token());
    }
}
