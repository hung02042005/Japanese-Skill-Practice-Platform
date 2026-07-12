/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.support.service;

import com.jlpt.feature.admin.AdminAuditLog;
import com.jlpt.feature.admin.AdminAuditLogRepository;
import com.jlpt.feature.assessment.StudentSubmission;
import com.jlpt.feature.assessment.StudentSubmissionRepository;
import com.jlpt.feature.notification.Notification;
import com.jlpt.feature.notification.service.NotificationService;
import com.jlpt.feature.staff.StaffUser;
import com.jlpt.feature.staff.StaffUserRepository;
import com.jlpt.feature.student.StudentUser;
import com.jlpt.feature.student.StudentUserRepository;
import com.jlpt.feature.support.Ticket;
import com.jlpt.feature.support.TicketReply;
import com.jlpt.feature.support.dto.GradeResponse;
import com.jlpt.feature.support.dto.ManualGradeRequest;
import com.jlpt.feature.support.dto.TicketDetailResponse;
import com.jlpt.feature.support.dto.TicketReplyRequest;
import com.jlpt.feature.support.dto.TicketReplyResponse;
import com.jlpt.feature.support.dto.TicketRequest;
import com.jlpt.feature.support.dto.TicketResponse;
import com.jlpt.feature.support.repository.TicketReplyRepository;
import com.jlpt.feature.support.repository.TicketRepository;
import com.jlpt.shared.exception.BusinessException;
import com.jlpt.shared.exception.ForbiddenException;
import com.jlpt.shared.exception.ResourceNotFoundException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Support ticket (UC-29) + chấm điểm speaking thủ công (UC-31).
 * Thông báo (UC-30) đã tách sang {@link NotificationService}; service này chỉ gọi
 * {@code notificationService.notifyStudent(...)} khi có sự kiện ticket/chấm điểm.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SupportTicketService {

    private final TicketRepository ticketRepository;
    private final TicketReplyRepository ticketReplyRepository;
    private final NotificationService notificationService;
    private final StudentUserRepository studentUserRepository;
    private final StaffUserRepository staffUserRepository;
    private final AdminAuditLogRepository adminAuditLogRepository;

    /** READ + grade write — owned by Người 3. */
    @Autowired(required = false)
    private StudentSubmissionRepository submissionRepository;

    // ── UC-29: Create ticket ──────────────────────────────────────────────────

    @Transactional
    public TicketResponse createTicket(Long studentId, TicketRequest req) {
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

    // ── UC-29: Get my tickets (Student) ───────────────────────────────────────

    @Transactional(readOnly = true)
    public Page<TicketResponse> getMyTickets(Long studentId, String status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        if (status != null && !status.isBlank()) {
            Ticket.TicketStatus ts;
            try {
                ts = Ticket.TicketStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new BusinessException(400, "INVALID_STATUS", "Trang thai ticket khong hop le: " + status);
            }
            return ticketRepository
                    .findByStudentIdAndStatus(studentId, ts, pageable)
                    .map(this::toTicketResponse);
        }
        return ticketRepository.findByStudentId(studentId, pageable).map(this::toTicketResponse);
    }

    // ── UC-29: Ticket detail (Student — ownership check) ──────────────────────

    @Transactional(readOnly = true)
    public TicketDetailResponse getStudentTicketDetail(Long ticketId, Long studentId) {
        Ticket ticket = findTicketOrThrow(ticketId);
        if (!ticket.getStudent().getId().equals(studentId)) {
            throw new ForbiddenException("Bạn không có quyền xem ticket này");
        }
        List<TicketReply> replies = ticketReplyRepository.findByTicketIdOrderByCreatedAtAsc(ticketId);
        return toTicketDetailResponse(ticket, replies);
    }

    // ── UC-29: Ticket detail (Staff — no ownership check) ─────────────────────

    @Transactional(readOnly = true)
    public TicketDetailResponse getStaffTicketDetail(Long ticketId) {
        Ticket ticket = findTicketOrThrow(ticketId);
        List<TicketReply> replies = ticketReplyRepository.findByTicketIdOrderByCreatedAtAsc(ticketId);
        return toTicketDetailResponse(ticket, replies);
    }

    // ── UC-29: Reply (Student) ────────────────────────────────────────────────

    @Transactional
    public TicketReplyResponse addStudentReply(Long ticketId, Long studentId, TicketReplyRequest req) {
        Ticket ticket = findTicketOrThrow(ticketId);
        checkTicketNotClosed(ticket);
        if (!ticket.getStudent().getId().equals(studentId)) {
            throw new ForbiddenException("Bạn không có quyền phản hồi ticket này");
        }
        var student = findStudentOrThrow(studentId);
        // Only studentSender set — DB constraint CK_replies_sender
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

    // ── UC-29: Reply (Staff) ──────────────────────────────────────────────────

    @Transactional
    public TicketReplyResponse addStaffReply(Long ticketId, String staffEmail, TicketReplyRequest req) {
        Ticket ticket = findTicketOrThrow(ticketId);
        checkTicketNotClosed(ticket);
        var staff = findStaffOrThrow(staffEmail);
        // Chi staff duoc giao hoac Staff Manager moi duoc ho tro ticket nay
        boolean isManager = staff.getStaffRole() == StaffUser.StaffRole.STAFF_MANAGER;
        boolean isAssignee =
                ticket.getAssignedTo() != null && ticket.getAssignedTo().getId().equals(staff.getId());
        if (!isManager && !isAssignee) {
            throw new ForbiddenException("Chi staff duoc phan cong hoac Staff Manager moi duoc ho tro ticket nay");
        }
        // Only staffSender set — DB constraint CK_replies_sender
        TicketReply reply = ticketReplyRepository.save(TicketReply.builder()
                .ticket(ticket)
                .staffSender(staff)
                .message(req.getMessage())
                .attachmentUrl(req.getAttachmentUrl())
                .build());
        if (ticket.getStatus() == Ticket.TicketStatus.OPEN || ticket.getStatus() == Ticket.TicketStatus.ASSIGNED) {
            ticket.setStatus(Ticket.TicketStatus.IN_PROGRESS);
        }
        ticket.setLastReplyAt(LocalDateTime.now());
        ticketRepository.save(ticket);
        // Noi ticket -> notification: bao cho student co phan hoi moi
        notificationService.notifyStudent(
                ticket.getStudent(),
                "Ticket cua ban co phan hoi moi",
                "Nhan vien ho tro vua phan hoi ticket: " + ticket.getSubject(),
                Notification.NotificationType.SYSTEM,
                "ticket_reply_" + reply.getId(),
                staff);
        return toReplyResponse(reply, staff.getFullName(), "STAFF");
    }

    // ── UC-29: Close ticket (Staff/Admin) ─────────────────────────────────────

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

        // Noi ticket -> notification: bao cho student ticket da duoc xu ly
        notificationService.notifyStudent(
                ticket.getStudent(),
                "Ticket cua ban da duoc xu ly",
                "Ticket '" + ticket.getSubject() + "' da duoc nhan vien ho tro dong.",
                Notification.NotificationType.SYSTEM,
                "ticket_resolved_" + ticketId,
                staff);

        log.info("[Support] {} closed ticket {}", actorEmail, ticketId);
        return toTicketResponse(ticket);
    }

    // ── UC-29: Close ticket (Student — own ticket only) ───────────────────────

    @Transactional
    public TicketResponse closeStudentTicket(Long ticketId, Long studentId) {
        Ticket ticket = findTicketOrThrow(ticketId);
        if (!ticket.getStudent().getId().equals(studentId)) {
            throw new ForbiddenException("Bạn không có quyền đóng ticket này");
        }
        checkTicketNotClosed(ticket);
        ticket.setStatus(Ticket.TicketStatus.CLOSED);
        ticket.setResolvedAt(LocalDateTime.now());
        ticketRepository.save(ticket);
        log.info("[Support] Student {} closed ticket {}", studentId, ticketId);
        return toTicketResponse(ticket);
    }

    // ── Assign ticket (Staff Manager/Admin) ───────────────────────────────────

    @Transactional
    public TicketResponse assignTicket(Long ticketId, Long assignToStaffId, String actorEmail, boolean isAdmin) {
        if (!isAdmin) {
            StaffUser actor = findStaffOrThrow(actorEmail);
            if (actor.getStaffRole() != StaffUser.StaffRole.STAFF_MANAGER) {
                throw new ForbiddenException("Chỉ Staff Manager hoặc Admin được phân công ticket");
            }
        }
        Ticket ticket = findTicketOrThrow(ticketId);
        checkTicketNotClosed(ticket);
        var targetStaff = staffUserRepository
                .findById(assignToStaffId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy nhân viên được giao"));
        if (targetStaff.getStatus() != StaffUser.StaffStatus.ACTIVE) {
            throw new BusinessException(
                    422, "STAFF_NOT_ACTIVE", "Không thể giao ticket cho nhân viên đang không hoạt động");
        }
        ticket.setAssignedTo(targetStaff);
        // Staff Manager duyet + giao -> OPEN chuyen ASSIGNED (da duyet, cho staff xu ly)
        if (ticket.getStatus() == Ticket.TicketStatus.OPEN) {
            ticket.setStatus(Ticket.TicketStatus.ASSIGNED);
        }
        ticketRepository.save(ticket);
        adminAuditLogRepository.save(AdminAuditLog.builder()
                .staffActor(staffUserRepository.findByEmail(actorEmail).orElse(null))
                .action("TICKET_ASSIGNED")
                .targetTable("tickets")
                .targetId(ticketId)
                .description("Ticket '" + ticket.getSubject() + "' assigned to staff " + assignToStaffId + " by "
                        + actorEmail)
                .build());
        log.info("[Support] {} assigned ticket {} to staff {}", actorEmail, ticketId, assignToStaffId);
        return toTicketResponse(ticket);
    }

    // ── UC-29: All tickets (Staff view) ───────────────────────────────────────

    @Transactional(readOnly = true)
    public Page<TicketResponse> getAllTickets(
            String status, String category, String priority, String q, int page, int size) {
        return ticketRepository
                .findAllByFilters(parseStatus(status), category, parsePriority(priority), q, PageRequest.of(page, size))
                .map(this::toTicketResponse);
    }

    // ── UC-31: Browse speaking submissions (Staff grading queue) ──────────────

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
            throw new BusinessException(503, "SERVICE_UNAVAILABLE", "Submission service chưa sẵn sàng");
        }
        var submission = submissionRepository
                .findById(submissionId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bài nộp"));
        return toSubmissionResponse(submission);
    }

    private com.jlpt.feature.support.dto.SubmissionResponse toSubmissionResponse(StudentSubmission s) {
        BigDecimal finalScore = s.getManualScore() != null ? s.getManualScore() : s.getAiOverallScore();
        return com.jlpt.feature.support.dto.SubmissionResponse.builder()
                .submissionId(s.getId())
                .studentName(s.getStudent().getFullName())
                .jlptLevel(
                        s.getStudent().getCurrentJlptLevel() != null
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

    // ── UC-31: Manual grade speaking submission ───────────────────────────────

    @Transactional
    public GradeResponse manualGrade(Long submissionId, String actorEmail, ManualGradeRequest req) {
        if (submissionRepository == null) {
            throw new BusinessException(503, "SERVICE_UNAVAILABLE", "Submission service chưa sẵn sàng");
        }
        var submission = submissionRepository
                .findById(submissionId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bài nộp"));

        if (submission.getSubmissionType() != StudentSubmission.SubmissionType.SPEAKING) {
            throw new BusinessException(
                    422, "INVALID_SUBMISSION_TYPE", "Chỉ có thể chấm điểm thủ công bài nói (speaking)");
        }
        if (submission.getStatus() != StudentSubmission.SubmissionStatus.AI_GRADED) {
            throw new BusinessException(422, "INVALID_STATUS", "Chỉ chấm được bài đã qua AI chấm (ai_graded)");
        }

        var staff = findStaffOrThrow(actorEmail);
        submission.setManualScore(req.getManualScore());
        submission.setManualFeedback(req.getManualFeedback());
        submission.setGradedBy(staff);
        submission.setGradedAt(LocalDateTime.now());
        submission.setStatus(StudentSubmission.SubmissionStatus.GRADED);
        submissionRepository.save(submission);

        // Noi grading -> notification: bao cho student bai noi da duoc cham diem
        notificationService.notifyStudent(
                submission.getStudent(),
                "Bài nói của bạn đã được chấm điểm",
                "Điểm của bạn: " + req.getManualScore() + "/100",
                Notification.NotificationType.ACHIEVEMENT,
                "speaking_graded_" + submissionId,
                staff);

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

    // ── Helpers ───────────────────────────────────────────────────────────────

    private Ticket findTicketOrThrow(Long id) {
        return ticketRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy ticket"));
    }

    private StudentUser findStudentOrThrow(Long id) {
        return studentUserRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy học viên"));
    }

    private StaffUser findStaffOrThrow(String email) {
        return staffUserRepository
                .findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy nhân viên: " + email));
    }

    private void checkTicketNotClosed(Ticket ticket) {
        if (ticket.getStatus() == Ticket.TicketStatus.RESOLVED || ticket.getStatus() == Ticket.TicketStatus.CLOSED) {
            throw new BusinessException(409, "TICKET_CLOSED", "Ticket đã được đóng, không thể phản hồi thêm");
        }
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
                .assignedToStaffName(
                        t.getAssignedTo() != null ? t.getAssignedTo().getFullName() : null)
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
                .assignedToStaffName(
                        t.getAssignedTo() != null ? t.getAssignedTo().getFullName() : null)
                .lastReplyAt(t.getLastReplyAt())
                .createdAt(t.getCreatedAt())
                .resolvedAt(t.getResolvedAt())
                .replies(replies.stream()
                        .map(r -> TicketReplyResponse.builder()
                                .replyId(r.getId())
                                .senderName(
                                        r.getStudentSender() != null
                                                ? r.getStudentSender().getFullName()
                                                : r.getStaffSender().getFullName())
                                .senderRole(r.getStudentSender() != null ? "STUDENT" : "STAFF")
                                .message(r.getMessage())
                                .attachmentUrl(r.getAttachmentUrl())
                                .createdAt(r.getCreatedAt())
                                .build())
                        .toList())
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

    private Ticket.TicketStatus parseStatus(String status) {
        if (status == null) return null;
        try {
            return Ticket.TicketStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private Ticket.Priority parsePriority(String priority) {
        if (priority == null) return null;
        try {
            return Ticket.Priority.valueOf(priority.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
