/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.support.service;

import com.jlpt.feature.support.dto.GradeResponse;
import com.jlpt.feature.support.dto.ManualGradeRequest;
import com.jlpt.shared.dto.response.TicketDetailResponse;
import com.jlpt.shared.dto.response.TicketReplyResponse;
import com.jlpt.feature.support.dto.TicketResponse;
import com.jlpt.feature.support.repository.TicketReplyRepository;
import com.jlpt.feature.support.repository.TicketRepository;
import com.jlpt.feature.admin.AdminAuditLog;
import com.jlpt.feature.admin.AdminAuditLogRepository;
import com.jlpt.feature.assessment.StudentSubmission;
import com.jlpt.feature.assessment.StudentSubmissionRepository;
import com.jlpt.feature.notification.Notification;
import com.jlpt.feature.notification.NotificationRepository;
import com.jlpt.feature.notification.dto.NotificationResponse;
import com.jlpt.feature.staff.StaffUser;
import com.jlpt.feature.staff.StaffUserRepository;
import com.jlpt.feature.student.StudentUser;
import com.jlpt.feature.student.StudentUserRepository;
import com.jlpt.feature.support.Ticket;
import com.jlpt.feature.support.TicketReply;
import com.jlpt.shared.dto.request.SendNotificationRequest;
import com.jlpt.shared.dto.request.TicketReplyRequest;
import com.jlpt.shared.exception.BusinessException;
import com.jlpt.shared.exception.ForbiddenException;
import com.jlpt.shared.exception.ResourceNotFoundException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class SupportTicketService {

    private final TicketRepository ticketRepository;
    private final TicketReplyRepository ticketReplyRepository;
    private final NotificationRepository notificationRepository;
    private final StudentUserRepository studentUserRepository;
    private final StaffUserRepository staffUserRepository;
    private final AdminAuditLogRepository adminAuditLogRepository;

    /** READ + grade write â€” owned by NgÆ°á»i 3. */
    @Autowired(required = false)
    private StudentSubmissionRepository submissionRepository;

    // â”€â”€ UC-29: Create ticket â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    @Transactional
    public TicketResponse createTicket(Long studentId, com.jlpt.feature.support.dto.TicketRequest req) {
        var student = findStudentOrThrow(studentId);
        Ticket.Priority priority = req.getPriority() != null
                ? Ticket.Priority.valueOf(req.getPriority().toUpperCase())
                : Ticket.Priority.NORMAL;

        Ticket ticket = Ticket.builder()
                .student(student)
                .subject(req.getSubject())
                .content(req.getContent())
                .category(req.getCategory())
                .priority(priority)
                .status(Ticket.TicketStatus.OPEN)
                .build();
        ticketRepository.save(ticket);
        log.info("[Support] Student {} created ticket {}", studentId, ticket.getId());
        return toTicketResponse(ticket);
    }

    // â”€â”€ UC-29: Get my tickets (Student) â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    @Transactional(readOnly = true)
    public Page<TicketResponse> getMyTickets(Long studentId, String status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        if (status != null && !status.isBlank()) {
            Ticket.TicketStatus ts = Ticket.TicketStatus.valueOf(status.toUpperCase());
            return ticketRepository.findByStudentIdAndStatus(studentId, ts, pageable)
                    .map(this::toTicketResponse);
        }
        return ticketRepository.findByStudentId(studentId, pageable).map(this::toTicketResponse);
    }

    // â”€â”€ UC-29: Ticket detail (Student â€” ownership check) â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    @Transactional(readOnly = true)
    public TicketDetailResponse getStudentTicketDetail(Long ticketId, Long studentId) {
        Ticket ticket = findTicketOrThrow(ticketId);
        if (!ticket.getStudent().getId().equals(studentId)) {
            throw new ForbiddenException("Báº¡n khÃ´ng cÃ³ quyá»n xem ticket nÃ y");
        }
        List<TicketReply> replies = ticketReplyRepository.findByTicketIdOrderByCreatedAtAsc(ticketId);
        return toTicketDetailResponse(ticket, replies);
    }

    // â”€â”€ UC-29: Ticket detail (Staff â€” no ownership check) â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    @Transactional(readOnly = true)
    public TicketDetailResponse getStaffTicketDetail(Long ticketId) {
        Ticket ticket = findTicketOrThrow(ticketId);
        List<TicketReply> replies = ticketReplyRepository.findByTicketIdOrderByCreatedAtAsc(ticketId);
        return toTicketDetailResponse(ticket, replies);
    }

    // â”€â”€ UC-29: Reply (Student) â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    @Transactional
    public TicketReplyResponse addStudentReply(Long ticketId, Long studentId, TicketReplyRequest req) {
        Ticket ticket = findTicketOrThrow(ticketId);
        checkTicketNotClosed(ticket);
        if (!ticket.getStudent().getId().equals(studentId)) {
            throw new ForbiddenException("Báº¡n khÃ´ng cÃ³ quyá»n pháº£n há»“i ticket nÃ y");
        }
        var student = findStudentOrThrow(studentId);
        // Only studentSender set â€” DB constraint CK_replies_sender
        TicketReply reply = ticketReplyRepository.save(TicketReply.builder()
                .ticket(ticket)
                .studentSender(student)
                .message(req.getMessage())
                .attachmentUrl(req.getAttachmentUrl())
                .build());
        ticket.setLastReplyAt(LocalDateTime.now());
        ticketRepository.save(ticket);
        return toReplyResponse(reply, student.getFullName(), "STUDENT");
    }

    // â”€â”€ UC-29: Reply (Staff) â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    @Transactional
    public TicketReplyResponse addStaffReply(Long ticketId, String staffEmail, TicketReplyRequest req) {
        Ticket ticket = findTicketOrThrow(ticketId);
        checkTicketNotClosed(ticket);
        var staff = findStaffOrThrow(staffEmail);
        // Only staffSender set â€” DB constraint CK_replies_sender
        TicketReply reply = ticketReplyRepository.save(TicketReply.builder()
                .ticket(ticket)
                .staffSender(staff)
                .message(req.getMessage())
                .attachmentUrl(req.getAttachmentUrl())
                .build());
        if (ticket.getStatus() == Ticket.TicketStatus.OPEN) {
            ticket.setStatus(Ticket.TicketStatus.IN_PROGRESS);
        }
        ticket.setLastReplyAt(LocalDateTime.now());
        ticketRepository.save(ticket);
        return toReplyResponse(reply, staff.getFullName(), "STAFF");
    }

    // â”€â”€ UC-29: Close ticket (Staff/Admin) â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    @Transactional
    public TicketResponse closeTicket(Long ticketId, String actorEmail) {
        Ticket ticket = findTicketOrThrow(ticketId);
        ticket.setStatus(Ticket.TicketStatus.RESOLVED);
        ticket.setResolvedAt(LocalDateTime.now());
        ticketRepository.save(ticket);

        var staff = staffUserRepository.findByEmail(actorEmail).orElse(null);
        adminAuditLogRepository.save(AdminAuditLog.builder()
                .staffActor(staff)
                .action("TICKET_CLOSED")
                .targetTable("tickets")
                .targetId(ticketId)
                .description("Ticket '" + ticket.getSubject() + "' closed by " + actorEmail)
                .build());

        log.info("[Support] {} closed ticket {}", actorEmail, ticketId);
        return toTicketResponse(ticket);
    }

    // â”€â”€ UC-29: Close ticket (Student â€” own ticket only) â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    @Transactional
    public TicketResponse closeStudentTicket(Long ticketId, Long studentId) {
        Ticket ticket = findTicketOrThrow(ticketId);
        if (!ticket.getStudent().getId().equals(studentId)) {
            throw new ForbiddenException("Báº¡n khÃ´ng cÃ³ quyá»n Ä‘Ã³ng ticket nÃ y");
        }
        checkTicketNotClosed(ticket);
        ticket.setStatus(Ticket.TicketStatus.CLOSED);
        ticket.setResolvedAt(LocalDateTime.now());
        ticketRepository.save(ticket);
        log.info("[Support] Student {} closed ticket {}", studentId, ticketId);
        return toTicketResponse(ticket);
    }

    // â”€â”€ Assign ticket (Staff Manager/Admin) â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    @Transactional
    public TicketResponse assignTicket(Long ticketId, Long assignToStaffId, String actorEmail, boolean isAdmin) {
        if (!isAdmin) {
            StaffUser actor = findStaffOrThrow(actorEmail);
            if (actor.getStaffRole() != StaffUser.StaffRole.STAFF_MANAGER) {
                throw new ForbiddenException("Chá»‰ Staff Manager hoáº·c Admin Ä‘Æ°á»£c phÃ¢n cÃ´ng ticket");
            }
        }
        Ticket ticket = findTicketOrThrow(ticketId);
        var targetStaff = staffUserRepository.findById(assignToStaffId)
                .orElseThrow(() -> new ResourceNotFoundException("KhÃ´ng tÃ¬m tháº¥y nhÃ¢n viÃªn Ä‘Æ°á»£c giao"));
        ticket.setAssignedTo(targetStaff);
        ticketRepository.save(ticket);
        adminAuditLogRepository.save(AdminAuditLog.builder()
                .staffActor(staffUserRepository.findByEmail(actorEmail).orElse(null))
                .action("TICKET_ASSIGNED")
                .targetTable("tickets")
                .targetId(ticketId)
                .description("Ticket '" + ticket.getSubject() + "' assigned to staff " + assignToStaffId + " by " + actorEmail)
                .build());
        log.info("[Support] {} assigned ticket {} to staff {}", actorEmail, ticketId, assignToStaffId);
        return toTicketResponse(ticket);
    }

    // â”€â”€ UC-29: All tickets (Staff view) â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    @Transactional(readOnly = true)
    public Page<TicketResponse> getAllTickets(
            String status, String category, String priority, String q, int page, int size) {
        return ticketRepository
                .findAllByFilters(parseStatus(status), category, parsePriority(priority), q, PageRequest.of(page, size))
                .map(this::toTicketResponse);
    }

    // â”€â”€ UC-30: Get my notifications (Student) â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    @Transactional(readOnly = true)
    public Page<NotificationResponse> getMyNotifications(Long studentId, int page, int size) {
        return notificationRepository
                .findByStudentId(studentId, PageRequest.of(page, size))
                .map(this::toNotificationResponse);
    }

    public long getUnreadCount(Long studentId) {
        return notificationRepository.countUnreadByStudentId(studentId);
    }

    // â”€â”€ UC-30: Mark read â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    @Transactional
    public void markNotificationRead(Long notificationId, Long studentId) {
        var notif = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("KhÃ´ng tÃ¬m tháº¥y thÃ´ng bÃ¡o"));
        if (!notif.getStudent().getId().equals(studentId)) {
            throw new ForbiddenException("Báº¡n khÃ´ng cÃ³ quyá»n Ä‘Ã¡nh dáº¥u thÃ´ng bÃ¡o nÃ y");
        }
        notif.setIsRead(true);
        notif.setReadAt(LocalDateTime.now());
        notificationRepository.save(notif);
    }

    @Transactional
    public int markAllNotificationsRead(Long studentId) {
        return notificationRepository.markAllReadByStudentId(studentId, LocalDateTime.now());
    }

    // â”€â”€ UC-30: Broadcast notification (Staff â€” async) â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    public String sendNotification(String actorEmail, SendNotificationRequest req) {
        String jobId = "job_notification_" + System.currentTimeMillis();
        var staff = staffUserRepository.findByEmail(actorEmail).orElse(null);
        List<StudentUser> targets = resolveTargets(req.getTargetJlptLevel());
        broadcastAsync(targets, req, staff);
        adminAuditLogRepository.save(AdminAuditLog.builder()
                .staffActor(staff)
                .action("BROADCAST_SENT")
                .targetTable("notifications")
                .description("Broadcast jobId=" + jobId + " target=" + req.getTargetJlptLevel()
                        + " type=" + req.getNotificationType())
                .build());
        log.info("[Support] {} triggered notification broadcast jobId={} targets={}",
                actorEmail, jobId, targets.size());
        return jobId;
    }

    @Async
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
        return CompletableFuture.completedFuture(null);
    }

    // â”€â”€ UC-31: Browse speaking submissions (Staff grading queue) â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    @Transactional(readOnly = true)
    public Page<com.jlpt.feature.support.dto.SubmissionResponse> getAllSubmissions(
            String submissionType, String status, int page, int size) {
        if (submissionRepository == null) {
            return Page.empty(PageRequest.of(page, size));
        }
        var type = submissionType == null || submissionType.isBlank()
                ? null
                : StudentSubmission.SubmissionType.valueOf(submissionType.toUpperCase());
        var parsedStatus = status == null || status.isBlank()
                ? null
                : StudentSubmission.SubmissionStatus.valueOf(status.toUpperCase());
        return submissionRepository
                .findAllByTypeAndFilters(type, parsedStatus, PageRequest.of(page, size))
                .map(this::toSubmissionResponse);
    }

    @Transactional(readOnly = true)
    public com.jlpt.feature.support.dto.SubmissionResponse getSubmissionDetail(Long submissionId) {
        if (submissionRepository == null) {
            throw new BusinessException(503, "SERVICE_UNAVAILABLE", "Submission service chÆ°a sáºµn sÃ ng");
        }
        var submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new ResourceNotFoundException("KhÃ´ng tÃ¬m tháº¥y bÃ i ná»™p"));
        return toSubmissionResponse(submission);
    }

    private com.jlpt.feature.support.dto.SubmissionResponse toSubmissionResponse(
            StudentSubmission s) {
        BigDecimal finalScore = s.getManualScore() != null ? s.getManualScore() : s.getAiOverallScore();
        return com.jlpt.feature.support.dto.SubmissionResponse.builder()
                .submissionId(s.getId())
                .studentName(s.getStudent().getFullName())
                .jlptLevel(s.getStudent().getCurrentJlptLevel() != null
                        ? s.getStudent().getCurrentJlptLevel().name()
                        : null)
                .durationSeconds(s.getDurationSeconds())
                .submittedAt(s.getSubmittedAt())
                .status(s.getStatus().getValue())
                .aiOverallScore(s.getAiOverallScore())
                .recordingUrl(s.getRecordingUrl())
                .aiPronunciationScore(s.getAiPronunciationScore())
                .aiFluencyScore(s.getAiFluencyScore())
                .aiHighlightedErrors(s.getAiErrorSummary())
                .aiSuggestions(s.getAiSuggestions())
                .aiGradedAt(s.getAiGradedAt())
                .manualScore(s.getManualScore())
                .manualFeedback(s.getManualFeedback())
                .gradedBy(s.getGradedBy() != null ? s.getGradedBy().getFullName() : null)
                .gradedAt(s.getGradedAt())
                .finalScore(finalScore)
                .build();
    }

    // â”€â”€ UC-31: Manual grade speaking submission â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    @Transactional
    public GradeResponse manualGrade(Long submissionId, String actorEmail, ManualGradeRequest req) {
        if (submissionRepository == null) {
            throw new BusinessException(503, "SERVICE_UNAVAILABLE", "Submission service chÆ°a sáºµn sÃ ng");
        }
        var submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new ResourceNotFoundException("KhÃ´ng tÃ¬m tháº¥y bÃ i ná»™p"));

        if (submission.getSubmissionType() != StudentSubmission.SubmissionType.SPEAKING) {
            throw new BusinessException(422, "INVALID_SUBMISSION_TYPE",
                    "Chá»‰ cÃ³ thá»ƒ cháº¥m Ä‘iá»ƒm thá»§ cÃ´ng bÃ i nÃ³i (speaking)");
        }
        if (submission.getStatus() != StudentSubmission.SubmissionStatus.AI_GRADED) {
            throw new BusinessException(422, "INVALID_STATUS",
                    "Chá»‰ cháº¥m Ä‘Æ°á»£c bÃ i Ä‘Ã£ qua AI cháº¥m (ai_graded)");
        }

        var staff = findStaffOrThrow(actorEmail);
        submission.setManualScore(req.getManualScore());
        submission.setManualFeedback(req.getManualFeedback());
        submission.setGradedBy(staff);
        submission.setGradedAt(LocalDateTime.now());
        submission.setStatus(StudentSubmission.SubmissionStatus.GRADED);
        submissionRepository.save(submission);

        notificationRepository.save(Notification.builder()
                .student(submission.getStudent())
                .title("BÃ i nÃ³i cá»§a báº¡n Ä‘Ã£ Ä‘Æ°á»£c cháº¥m Ä‘iá»ƒm")
                .content("Äiá»ƒm cá»§a báº¡n: " + req.getManualScore() + "/100")
                .notificationType(Notification.NotificationType.ACHIEVEMENT)
                .channel(Notification.Channel.IN_APP)
                .isAuto(true)
                .ruleKey("speaking_graded_" + submissionId)
                .staffCreator(staff)
                .build());

        adminAuditLogRepository.save(AdminAuditLog.builder()
                .staffActor(staff)
                .action("SUBMISSION_GRADED")
                .targetTable("student_submissions")
                .targetId(submissionId)
                .description("Graded speaking submission " + submissionId + " score=" + req.getManualScore())
                .build());

        log.info("[Support] {} graded submission {} score={}", actorEmail, submissionId, req.getManualScore());

        BigDecimal finalScore = req.getManualScore();
        return GradeResponse.builder()
                .submissionId(submission.getId())
                .studentId(submission.getStudent().getId())
                .studentName(submission.getStudent().getFullName())
                .submissionType(submission.getSubmissionType().getValue())
                .status(submission.getStatus().getValue())
                .aiOverallScore(submission.getAiOverallScore())
                .manualScore(req.getManualScore())
                .finalScore(finalScore)
                .manualFeedback(req.getManualFeedback())
                .gradedByStaffName(staff.getFullName())
                .gradedAt(submission.getGradedAt())
                .submittedAt(submission.getSubmittedAt())
                .build();
    }

    // â”€â”€ Helpers â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    private Ticket findTicketOrThrow(Long id) {
        return ticketRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("KhÃ´ng tÃ¬m tháº¥y ticket"));
    }

    private StudentUser findStudentOrThrow(Long id) {
        return studentUserRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("KhÃ´ng tÃ¬m tháº¥y há»c viÃªn"));
    }

    private StaffUser findStaffOrThrow(String email) {
        return staffUserRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("KhÃ´ng tÃ¬m tháº¥y nhÃ¢n viÃªn: " + email));
    }

    private void checkTicketNotClosed(Ticket ticket) {
        if (ticket.getStatus() == Ticket.TicketStatus.RESOLVED
                || ticket.getStatus() == Ticket.TicketStatus.CLOSED) {
            throw new BusinessException(409, "TICKET_CLOSED", "Ticket Ä‘Ã£ Ä‘Æ°á»£c Ä‘Ã³ng, khÃ´ng thá»ƒ pháº£n há»“i thÃªm");
        }
    }

    private List<StudentUser> resolveTargets(String targetJlptLevel) {
        if (targetJlptLevel == null || targetJlptLevel.isBlank()) {
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

    private TicketResponse toTicketResponse(Ticket t) {
        long replyCount = ticketReplyRepository.countByTicketId(t.getId());
        return TicketResponse.builder()
                .ticketId(t.getId())
                .studentId(t.getStudent().getId())
                .studentName(t.getStudent().getFullName())
                .studentEmail(t.getStudent().getEmail())
                .subject(t.getSubject())
                .content(t.getContent())
                .category(t.getCategory())
                .priority(t.getPriority().getValue())
                .status(t.getStatus().getValue())
                .assignedToStaffId(t.getAssignedTo() != null ? t.getAssignedTo().getId() : null)
                .assignedToStaffName(t.getAssignedTo() != null ? t.getAssignedTo().getFullName() : null)
                .replyCount(replyCount)
                .lastReplyAt(t.getLastReplyAt())
                .createdAt(t.getCreatedAt())
                .resolvedAt(t.getResolvedAt())
                .build();
    }

    private TicketDetailResponse toTicketDetailResponse(Ticket t, List<TicketReply> replies) {
        return TicketDetailResponse.builder()
                .ticketId(t.getId())
                .studentId(t.getStudent().getId())
                .studentName(t.getStudent().getFullName())
                .studentEmail(t.getStudent().getEmail())
                .subject(t.getSubject())
                .content(t.getContent())
                .category(t.getCategory())
                .priority(t.getPriority().getValue())
                .status(t.getStatus().getValue())
                .assignedToStaffId(t.getAssignedTo() != null ? t.getAssignedTo().getId() : null)
                .assignedToStaffName(t.getAssignedTo() != null ? t.getAssignedTo().getFullName() : null)
                .lastReplyAt(t.getLastReplyAt())
                .createdAt(t.getCreatedAt())
                .resolvedAt(t.getResolvedAt())
                .replies(replies.stream().map(r -> TicketReplyResponse.builder()
                        .replyId(r.getId())
                        .senderName(r.getStudentSender() != null
                                ? r.getStudentSender().getFullName()
                                : r.getStaffSender().getFullName())
                        .senderRole(r.getStudentSender() != null ? "STUDENT" : "STAFF")
                        .message(r.getMessage())
                        .attachmentUrl(r.getAttachmentUrl())
                        .createdAt(r.getCreatedAt())
                        .build()).toList())
                .build();
    }

    private TicketReplyResponse toReplyResponse(TicketReply r, String senderName, String role) {
        return TicketReplyResponse.builder()
                .replyId(r.getId())
                .senderName(senderName)
                .senderRole(role)
                .message(r.getMessage())
                .attachmentUrl(r.getAttachmentUrl())
                .createdAt(r.getCreatedAt())
                .build();
    }

    private NotificationResponse toNotificationResponse(Notification n) {
        return NotificationResponse.builder()
                .notificationId(n.getId())
                .title(n.getTitle())
                .content(n.getContent())
                .notificationType(n.getNotificationType().getValue())
                .channel(n.getChannel().getValue())
                .isRead(n.getIsRead())
                .readAt(n.getReadAt())
                .createdAt(n.getCreatedAt())
                .build();
    }

    private Ticket.TicketStatus parseStatus(String status) {
        if (status == null) return null;
        try { return Ticket.TicketStatus.valueOf(status.toUpperCase()); }
        catch (IllegalArgumentException e) { return null; }
    }

    private Ticket.Priority parsePriority(String priority) {
        if (priority == null) return null;
        try { return Ticket.Priority.valueOf(priority.toUpperCase()); }
        catch (IllegalArgumentException e) { return null; }
    }
}


