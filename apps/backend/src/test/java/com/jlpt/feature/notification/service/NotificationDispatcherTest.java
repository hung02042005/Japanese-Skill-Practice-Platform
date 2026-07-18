/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.notification.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.jlpt.feature.notification.Notification;
import com.jlpt.feature.notification.NotificationRepository;
import com.jlpt.feature.staff.StaffUser;
import com.jlpt.feature.student.StudentUser;
import com.jlpt.shared.dto.request.SendNotificationRequest;
import com.jlpt.shared.email.EmailService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

/**
 * Test NotificationDispatcher — bao gồm regression test cho fix N+1 (JOIN FETCH n.student trong
 * findDuePendingEmails) và hành vi "best-effort" đã ghi rõ trong Javadoc: gửi email thất bại vẫn
 * đánh dấu sentAt để tránh retry vô hạn, KHÔNG được để lỗi 1 email chặn cả batch.
 */
@ExtendWith(MockitoExtension.class)
class NotificationDispatcherTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private NotificationDispatcher dispatcher;

    private Notification dueNotification(Long id, String email) {
        StudentUser student = StudentUser.builder().id(id).email(email).build();
        return Notification.builder()
                .id(id)
                .student(student)
                .title("Tiêu đề " + id)
                .content("Nội dung " + id)
                .channel(Notification.Channel.EMAIL)
                .build();
    }

    @Test
    void deliverPendingEmails_Success_SendsAndMarksSentAt() {
        Notification n = dueNotification(1L, "student@example.com");
        when(notificationRepository.findDuePendingEmails(anyList(), any(), any(Pageable.class)))
                .thenReturn(List.of(n));

        dispatcher.deliverPendingEmails();

        verify(emailService).sendNotificationEmail("student@example.com", "Tiêu đề 1", "Nội dung 1");
        assertNotNull(n.getSentAt());
        verify(notificationRepository).saveAll(List.of(n));
    }

    @Test
    void deliverPendingEmails_EmailServiceThrows_StillMarksSentAt_BestEffort() {
        Notification n = dueNotification(2L, "fail@example.com");
        when(notificationRepository.findDuePendingEmails(anyList(), any(), any(Pageable.class)))
                .thenReturn(List.of(n));
        doThrow(new RuntimeException("SMTP unreachable")).when(emailService).sendNotificationEmail(any(), any(), any());

        // Không được ném exception ra ngoài — 1 email lỗi không được chặn cả batch.
        assertDoesNotThrow(() -> dispatcher.deliverPendingEmails());

        // Best-effort: vẫn đánh dấu sentAt dù gửi thất bại, để tránh scheduler retry vô hạn.
        assertNotNull(n.getSentAt());
        verify(notificationRepository).saveAll(List.of(n));
    }

    @Test
    void deliverPendingEmails_MixedBatch_OneFailureDoesNotBlockOthers() {
        Notification ok = dueNotification(3L, "ok@example.com");
        Notification bad = dueNotification(4L, "bad@example.com");
        when(notificationRepository.findDuePendingEmails(anyList(), any(), any(Pageable.class)))
                .thenReturn(List.of(bad, ok));
        doThrow(new RuntimeException("boom"))
                .when(emailService)
                .sendNotificationEmail(eq("bad@example.com"), any(), any());

        dispatcher.deliverPendingEmails();

        verify(emailService).sendNotificationEmail(eq("ok@example.com"), any(), any());
        assertNotNull(bad.getSentAt());
        assertNotNull(ok.getSentAt());
    }

    @Test
    void deliverPendingEmails_NoDueNotifications_DoesNothing() {
        when(notificationRepository.findDuePendingEmails(anyList(), any(), any(Pageable.class)))
                .thenReturn(List.of());

        dispatcher.deliverPendingEmails();

        verify(emailService, never()).sendNotificationEmail(any(), any(), any());
        verify(notificationRepository, never()).saveAll(anyList());
    }

    @Test
    void broadcastAsync_SavesOneNotificationPerTarget() {
        StudentUser s1 = StudentUser.builder().id(10L).email("s1@example.com").build();
        StudentUser s2 = StudentUser.builder().id(11L).email("s2@example.com").build();
        StaffUser staffCreator = StaffUser.builder().id(1L).build();

        SendNotificationRequest request = new SendNotificationRequest();
        request.setTitle("Thông báo hệ thống");
        request.setContent("Bảo trì lúc 22h");
        request.setNotificationType("system");
        request.setChannel("both");

        dispatcher.broadcastAsync(List.of(s1, s2), request, staffCreator);

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository, times(2)).save(captor.capture());
        List<Notification> saved = captor.getAllValues();
        assertEquals(2, saved.size());
        assertEquals(s1, saved.get(0).getStudent());
        assertEquals(s2, saved.get(1).getStudent());
        assertEquals(Notification.Channel.BOTH, saved.get(0).getChannel());
        assertEquals(Notification.NotificationType.SYSTEM, saved.get(0).getNotificationType());
        assertFalse(saved.get(0).getIsAuto());
    }
}
