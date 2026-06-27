/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.notification.service;

import com.jlpt.feature.notification.Notification;
import com.jlpt.feature.notification.NotificationRepository;
import com.jlpt.feature.staff.StaffUser;
import com.jlpt.feature.student.StudentUser;
import com.jlpt.shared.dto.request.SendNotificationRequest;
import com.jlpt.shared.email.EmailService;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Fan-out thong bao — bean rieng de @Async/@Scheduled khong bi self-invocation.
 * - broadcastAsync: ghi nhanh cac dong notification (record IN_APP) tren thread rieng.
 * - deliverPendingEmails: dinh ky gui email cho cac thong bao kenh email/both da den han.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationDispatcher {

    private static final int EMAIL_BATCH = 100;

    private final NotificationRepository notificationRepository;
    private final EmailService emailService;

    /** Broadcast bat dong bo cho nhieu student (UC-30). Chi ghi record; email do scheduler giao. */
    @Async
    @Transactional
    public CompletableFuture<Void> broadcastAsync(
            List<StudentUser> targets, SendNotificationRequest req, StaffUser staffCreator) {
        Notification.NotificationType notifType =
                Notification.NotificationType.valueOf(req.getNotificationType().toUpperCase());
        Notification.Channel channel = Notification.Channel.valueOf(req.getChannel().toUpperCase());
        for (StudentUser student : targets) {
            notificationRepository.save(Notification.builder()
                    .student(student)
                    .title(req.getTitle())
                    .content(req.getContent())
                    .notificationType(notifType)
                    .channel(channel)
                    .isAuto(false)
                    .scheduledAt(req.getScheduledAt())
                    .staffCreator(staffCreator)
                    .build());
        }
        log.info("[Notification] broadcast saved for {} students (channel={})", targets.size(), channel);
        return CompletableFuture.completedFuture(null);
    }

    /**
     * Giao email cho thong bao kenh email/both da den han va chua gui (sent_at IS NULL).
     * Best-effort: that bai van danh dau sent_at de tranh retry vo han (giong @Async email khac).
     */
    @Scheduled(fixedDelay = 60_000)
    @Transactional
    public void deliverPendingEmails() {
        List<Notification> due = notificationRepository.findDuePendingEmails(
                List.of(Notification.Channel.EMAIL, Notification.Channel.BOTH),
                LocalDateTime.now(),
                org.springframework.data.domain.PageRequest.of(0, EMAIL_BATCH));
        if (due.isEmpty()) return;
        LocalDateTime now = LocalDateTime.now();
        for (Notification n : due) {
            try {
                emailService.sendNotificationEmail(n.getStudent().getEmail(), n.getTitle(), n.getContent());
            } catch (Exception e) {
                log.error("[Notification] email delivery failed for notif {}: {}",
                        n.getId(), e.getMessage());
            }
            n.setSentAt(now);
        }
        notificationRepository.saveAll(due);
        log.info("[Notification] delivered {} pending email notifications", due.size());
    }
}
