/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.notification.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

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
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

/**
 * Unit tests cho NotificationService.
 */
@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private StudentUserRepository studentUserRepository;

    @Mock
    private AdminAuditLogRepository adminAuditLogRepository;

    @Mock
    private NotificationDispatcher notificationDispatcher;

    @Mock
    private StaffManagerGuard staffManagerGuard;

    @InjectMocks
    private NotificationService notificationService;

    private StudentUser student;
    private Notification notification;

    @BeforeEach
    void setUp() {
        student = StudentUser.builder()
                .id(1L)
                .email("student@test.com")
                .status(StudentUser.StudentStatus.ACTIVE)
                .build();
        notification = Notification.builder()
                .id(10L)
                .student(student)
                .title("Welcome")
                .content("Hello World")
                .notificationType(Notification.NotificationType.SYSTEM)
                .channel(Notification.Channel.IN_APP)
                .isRead(false)
                .build();
    }

    // ── notifyStudent ─────────────────────────────────────────────────────────

    @Test
    void notifyStudent_savesNotificationInDb() {
        notificationService.notifyStudent(
                student, "Title", "Content", Notification.NotificationType.SYSTEM, "RULE_KEY", null);

        verify(notificationRepository).save(any(Notification.class));
    }

    // ── getMyNotifications ────────────────────────────────────────────────────

    @Test
    void getMyNotifications_returnsMappedPage() {
        Page<Notification> page = new PageImpl<>(List.of(notification));
        when(notificationRepository.findVisibleByStudentId(eq(1L), any(LocalDateTime.class), any(PageRequest.class)))
                .thenReturn(page);

        Page<NotificationResponse> result = notificationService.getMyNotifications(1L, 0, 10);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("Welcome", result.getContent().get(0).getTitle());
    }

    // ── markNotificationRead ──────────────────────────────────────────────────

    @Test
    void markNotificationRead_notFound_throwsResourceNotFoundException() {
        when(notificationRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> notificationService.markNotificationRead(99L, 1L));
    }

    @Test
    void markNotificationRead_wrongStudent_throwsForbiddenException() {
        when(notificationRepository.findById(10L)).thenReturn(Optional.of(notification));

        assertThrows(ForbiddenException.class, () -> notificationService.markNotificationRead(10L, 99L));
    }

    @Test
    void markNotificationRead_success_setsReadTrue() {
        when(notificationRepository.findById(10L)).thenReturn(Optional.of(notification));

        notificationService.markNotificationRead(10L, 1L);

        assertTrue(notification.getIsRead());
        assertNotNull(notification.getReadAt());
        verify(notificationRepository).save(notification);
    }

    // ── broadcast ─────────────────────────────────────────────────────────────

    @Test
    void broadcast_notManager_throwsForbiddenException() {
        when(staffManagerGuard.requireManager(eq("staff@test.com"), anyString()))
                .thenThrow(new ForbiddenException("Not a manager"));

        SendNotificationRequest req = new SendNotificationRequest();

        assertThrows(ForbiddenException.class, () -> notificationService.broadcast("staff@test.com", req));
    }

    @Test
    void broadcast_allStudentsTarget_dispatchesAsyncBroadcast() {
        StaffUser manager = StaffUser.builder().id(2L).email("manager@test.com").build();
        when(staffManagerGuard.requireManager(eq("manager@test.com"), anyString()))
                .thenReturn(manager);
        when(studentUserRepository.findAll()).thenReturn(List.of(student));

        SendNotificationRequest req = new SendNotificationRequest();
        req.setTargetJlptLevel("ALL");
        req.setNotificationType("SYSTEM");

        String jobId = notificationService.broadcast("manager@test.com", req);

        assertNotNull(jobId);
        assertTrue(jobId.startsWith("job_notification_"));
        verify(notificationDispatcher).broadcastAsync(eq(List.of(student)), eq(req), eq(manager));
        verify(adminAuditLogRepository).save(any());
    }
}
