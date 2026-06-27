/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.notification.service;

import com.jlpt.feature.admin.AdminAuditLog;
import com.jlpt.feature.admin.AdminAuditLogRepository;
import com.jlpt.feature.notification.Notification;
import com.jlpt.feature.notification.NotificationRepository;
import com.jlpt.feature.notification.dto.NotificationResponse;
import com.jlpt.feature.staff.StaffManagerGuard;
import com.jlpt.feature.staff.StaffUser;
import com.jlpt.feature.student.StudentUser;
import com.jlpt.feature.student.StudentUserRepository;
import com.jlpt.shared.dto.request.SendNotificationRequest;
import com.jlpt.shared.exception.ForbiddenException;
import com.jlpt.shared.exception.ResourceNotFoundException;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Notification (UC-30) — tach khoi SupportTicketService.
 * Ghi/doc thong bao + dieu phoi broadcast. Fan-out bat dong bo + gui email
 * nam o {@link NotificationDispatcher} (bean rieng de @Async khong bi self-invocation).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final StudentUserRepository studentUserRepository;
    private final AdminAuditLogRepository adminAuditLogRepository;
    private final NotificationDispatcher notificationDispatcher;
    private final StaffManagerGuard staffManagerGuard;

    // ── Tao thong bao he thong cho 1 student (ticket reply, ticket dong, cham diem...) ──

    /** Tao 1 thong bao IN_APP cho 1 student tu su kien he thong. */
    @Transactional
    public void notifyStudent(
            StudentUser student,
            String title,
            String content,
            Notification.NotificationType type,
            String ruleKey,
            StaffUser staffCreator) {
        notificationRepository.save(Notification.builder()
                .student(student)
                .title(title)
                .content(content)
                .notificationType(type)
                .channel(Notification.Channel.IN_APP)
                .isAuto(true)
                .ruleKey(ruleKey)
                .staffCreator(staffCreator)
                .build());
    }

    // ── UC-30: Doc thong bao (Student) ────────────────────────────────────────

    @Transactional(readOnly = true)
    public Page<NotificationResponse> getMyNotifications(Long studentId, int page, int size) {
        return notificationRepository
                .findVisibleByStudentId(studentId, LocalDateTime.now(), PageRequest.of(page, size))
                .map(this::toNotificationResponse);
    }

    @Transactional(readOnly = true)
    public long getUnreadCount(Long studentId) {
        return notificationRepository.countUnreadVisibleByStudentId(studentId, LocalDateTime.now());
    }

    @Transactional
    public void markNotificationRead(Long notificationId, Long studentId) {
        var notif = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Khong tim thay thong bao"));
        if (!notif.getStudent().getId().equals(studentId)) {
            throw new ForbiddenException("Ban khong co quyen danh dau thong bao nay");
        }
        notif.setIsRead(true);
        notif.setReadAt(LocalDateTime.now());
        notificationRepository.save(notif);
    }

    @Transactional
    public int markAllNotificationsRead(Long studentId) {
        return notificationRepository.markAllReadByStudentId(studentId, LocalDateTime.now());
    }

    // ── UC-30: Broadcast (Staff/Manager) — async fan-out, tra jobId ────────────

    @Transactional
    public String broadcast(String actorEmail, SendNotificationRequest req) {
        String jobId = "job_notification_" + System.currentTimeMillis();
        // Broadcast toàn hệ thống là quyền quản lý — chỉ staff_manager (không tin việc FE đã ẩn nút).
        StaffUser staff =
                staffManagerGuard.requireManager(actorEmail, "Chỉ Staff Manager mới có quyền gửi thông báo broadcast");
        List<StudentUser> targets = resolveTargets(req.getTargetJlptLevel());
        // Cross-bean call -> @Async cua dispatcher hoat dong (khong bi bypass proxy)
        notificationDispatcher.broadcastAsync(targets, req, staff);
        adminAuditLogRepository.save(AdminAuditLog.builder()
                .staffActor(staff)
                .action("BROADCAST_SENT")
                .targetTable("notifications")
                .description("Broadcast jobId=" + jobId + " target=" + req.getTargetJlptLevel()
                        + " type=" + req.getNotificationType())
                .build());
        log.info("[Notification] {} triggered broadcast jobId={} targets={}",
                actorEmail, jobId, targets.size());
        return jobId;
    }

    private List<StudentUser> resolveTargets(String targetJlptLevel) {
        // null / rong / "ALL" -> tat ca student dang ACTIVE
        if (targetJlptLevel == null
                || targetJlptLevel.isBlank()
                || "ALL".equalsIgnoreCase(targetJlptLevel)) {
            return studentUserRepository.findAll().stream()
                    .filter(s -> s.getStatus() == StudentUser.StudentStatus.ACTIVE)
                    .toList();
        }
        StudentUser.JlptLevel level = StudentUser.JlptLevel.valueOf(targetJlptLevel.toUpperCase());
        return studentUserRepository.findAll().stream()
                .filter(s -> s.getStatus() == StudentUser.StudentStatus.ACTIVE
                        && level.equals(s.getCurrentJlptLevel()))
                .toList();
    }

    private NotificationResponse toNotificationResponse(Notification n) {
        return NotificationResponse.builder()
                .notificationId(n.getId())
                .title(n.getTitle())
                .content(n.getContent())
                .notificationType(n.getNotificationType().getValue())
                .channel(n.getChannel().getValue())
                .isRead(n.getIsRead())
                .isAuto(n.getIsAuto())
                .ruleKey(n.getRuleKey())
                .scheduledAt(n.getScheduledAt())
                .sentAt(n.getSentAt())
                .readAt(n.getReadAt())
                .createdAt(n.getCreatedAt())
                .build();
    }
}
