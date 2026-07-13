/* (c) JLPT E-Learning Platform */
package com.jlpt.shared.email;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.jlpt.feature.admin.SystemSettingRepository;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * Test cơ chế outbox mới của EmailService: email gửi thất bại sau khi hết retry được lưu lại
 * để retryFailedOutbox() (chạy định kỳ) thử gửi lại, thay vì mất hẳn.
 */
@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private SystemSettingRepository settingRepository;

    @Mock
    private EmailOutboxRepository emailOutboxRepository;

    @InjectMocks
    private EmailService emailService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(emailService, "fromEmail", "noreply@test.com");
        // lenient: một số test (skip do đạt max attempts / không có entry nào) không bao giờ
        // thực sự gọi resolveFromEmail()/createMimeMessage(), Mockito strict-stubs sẽ báo unused.
        lenient()
                .when(settingRepository.findBySettingGroupAndSettingKey(eq("smtp"), any()))
                .thenReturn(java.util.Optional.empty());
        lenient().when(mailSender.createMimeMessage()).thenAnswer(inv -> new MimeMessage((Session) null));
    }

    @Test
    void sendNotificationEmail_AllRetriesFail_SavesToOutbox() {
        doThrow(new MailSendException("SMTP down")).when(mailSender).send(any(MimeMessage.class));

        emailService.sendNotificationEmail("student@example.com", "Tiêu đề", "Nội dung");

        ArgumentCaptor<EmailOutbox> captor = ArgumentCaptor.forClass(EmailOutbox.class);
        verify(emailOutboxRepository).save(captor.capture());
        EmailOutbox saved = captor.getValue();
        assertEquals("student@example.com", saved.getToEmail());
        assertEquals(EmailOutbox.Status.FAILED, saved.getStatus());
        assertEquals(3, saved.getAttemptCount());
        assertNotNull(saved.getLastError());
        // 3 lần thử thật sự (không chỉ log rồi bỏ qua) trước khi lưu outbox.
        verify(mailSender, times(3)).send(any(MimeMessage.class));
    }

    @Test
    void sendNotificationEmail_SucceedsOnFirstAttempt_NeverTouchesOutbox() {
        doNothing().when(mailSender).send(any(MimeMessage.class));

        emailService.sendNotificationEmail("student@example.com", "Tiêu đề", "Nội dung");

        verify(mailSender, times(1)).send(any(MimeMessage.class));
        verify(emailOutboxRepository, never()).save(any());
    }

    @Test
    void retryFailedOutbox_Success_MarksSent() {
        EmailOutbox entry = EmailOutbox.builder()
                .id(1L)
                .toEmail("a@example.com")
                .subject("S")
                .bodyHtml("<p>B</p>")
                .status(EmailOutbox.Status.FAILED)
                .attemptCount(2)
                .build();
        when(emailOutboxRepository.findByStatusOrderByCreatedAtAsc(eq(EmailOutbox.Status.FAILED), any(Pageable.class)))
                .thenReturn(List.of(entry));
        doNothing().when(mailSender).send(any(MimeMessage.class));

        emailService.retryFailedOutbox();

        assertEquals(EmailOutbox.Status.SENT, entry.getStatus());
        assertEquals(3, entry.getAttemptCount());
        assertNotNull(entry.getSentAt());
        verify(emailOutboxRepository).save(entry);
    }

    @Test
    void retryFailedOutbox_StillFailing_StaysFailedAndIncrementsAttempts() {
        EmailOutbox entry = EmailOutbox.builder()
                .id(2L)
                .toEmail("b@example.com")
                .subject("S")
                .bodyHtml("<p>B</p>")
                .status(EmailOutbox.Status.FAILED)
                .attemptCount(2)
                .build();
        when(emailOutboxRepository.findByStatusOrderByCreatedAtAsc(eq(EmailOutbox.Status.FAILED), any(Pageable.class)))
                .thenReturn(List.of(entry));
        doThrow(new MailSendException("still down")).when(mailSender).send(any(MimeMessage.class));

        emailService.retryFailedOutbox();

        assertEquals(EmailOutbox.Status.FAILED, entry.getStatus());
        assertEquals(3, entry.getAttemptCount());
        assertNull(entry.getSentAt());
        verify(emailOutboxRepository).save(entry);
    }

    @Test
    void retryFailedOutbox_MaxAttemptsReached_SkipsEntryEntirely() {
        EmailOutbox entry = EmailOutbox.builder()
                .id(3L)
                .toEmail("c@example.com")
                .subject("S")
                .bodyHtml("<p>B</p>")
                .status(EmailOutbox.Status.FAILED)
                .attemptCount(10)
                .lastAttemptAt(LocalDateTime.now())
                .build();
        when(emailOutboxRepository.findByStatusOrderByCreatedAtAsc(eq(EmailOutbox.Status.FAILED), any(Pageable.class)))
                .thenReturn(List.of(entry));

        emailService.retryFailedOutbox();

        verify(mailSender, never()).send(any(MimeMessage.class));
        verify(emailOutboxRepository, never()).save(any());
    }

    @Test
    void retryFailedOutbox_NoFailedEntries_DoesNothing() {
        when(emailOutboxRepository.findByStatusOrderByCreatedAtAsc(eq(EmailOutbox.Status.FAILED), any(Pageable.class)))
                .thenReturn(List.of());

        emailService.retryFailedOutbox();

        verify(mailSender, never()).send(any(MimeMessage.class));
        verify(emailOutboxRepository, never()).save(any());
    }
}
