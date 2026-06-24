/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.studentmanagement.service;

import com.jlpt.feature.studentmanagement.dto.StudentDetailResponse;
import com.jlpt.feature.studentmanagement.dto.StudentProgressResponse;
import com.jlpt.feature.studentmanagement.dto.SubmissionSummaryResponse;
import com.jlpt.shared.audit.AdminAuditLog;
import com.jlpt.feature.assessment.repository.TestAttemptRepository;
import com.jlpt.feature.corelearning.entity.StudentContentProgress;
import com.jlpt.feature.corelearning.entity.StudentSubmission;
import com.jlpt.feature.student.entity.StudentUser;
import com.jlpt.shared.exception.BusinessException;
import com.jlpt.shared.exception.ResourceNotFoundException;
import com.jlpt.shared.audit.AdminAuditLogRepository;
import com.jlpt.feature.auth.repository.AuthTokenRepository;
import com.jlpt.feature.staff.repository.StaffUserRepository;
import com.jlpt.feature.corelearning.repository.StudentContentProgressRepository;
import com.jlpt.feature.corelearning.repository.StudentSubmissionRepository;
import com.jlpt.feature.student.repository.StudentUserRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class StudentManagementService {

    private final StudentUserRepository studentUserRepository;
    private final AuthTokenRepository authTokenRepository;
    private final AdminAuditLogRepository adminAuditLogRepository;
    private final StaffUserRepository staffUserRepository;

    /** READ-ONLY — owned by Người 3. Inject optional to avoid compile error if not yet merged. */
    @Autowired(required = false)
    private StudentContentProgressRepository progressRepository;

    @Autowired(required = false)
    private StudentSubmissionRepository submissionRepository;

    /** READ-ONLY — owned by Người 3 (feat-assessment). Inject optional to avoid compile error if not yet merged. */
    @Autowired(required = false)
    private TestAttemptRepository testAttemptRepository;

    // ── UC-22: List students ─────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public Page<StudentDetailResponse> getStudentList(
            String q, String status, String jlptLevel, int page, int size) {
        String likeQ = (q != null && !q.isBlank()) ? "%" + q.trim() + "%" : null;
        Pageable pageable = PageRequest.of(page, size);
        return studentUserRepository
                .findAllAdminFiltered(likeQ, status, jlptLevel, pageable)
                .map(this::toStudentDetailResponse);
    }

    // ── UC-21: Student detail ────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public StudentDetailResponse getStudentDetail(Long studentId) {
        StudentUser student = findStudentOrThrow(studentId);
        return toStudentDetailResponse(student);
    }

    // ── UC-21: Progress summary ──────────────────────────────────────────────

    @Transactional(readOnly = true)
    public StudentProgressResponse getStudentProgressSummary(Long studentId) {
        StudentUser student = findStudentOrThrow(studentId);

        long lessons = countCompleted(studentId, StudentContentProgress.ContentType.LESSON);
        long kanji = countCompleted(studentId, StudentContentProgress.ContentType.KANJI);
        long vocabulary = countCompleted(studentId, StudentContentProgress.ContentType.VOCABULARY);
        long grammar = countCompleted(studentId, StudentContentProgress.ContentType.GRAMMAR);
        long kana = countCompleted(studentId, StudentContentProgress.ContentType.KANA);

        long totalExams = countExams(studentId);
        BigDecimal avgScore = testAttemptRepository != null
                ? testAttemptRepository.avgExamScoreByStudentId(studentId) : null;
        BigDecimal maxScore = testAttemptRepository != null
                ? testAttemptRepository.maxExamScoreByStudentId(studentId) : null;

        return StudentProgressResponse.builder()
                .studentId(student.getId())
                .fullName(student.getFullName())
                .currentStreak(student.getCurrentStreak())
                .longestStreak(student.getLongestStreak())
                .lastActivityDate(student.getLastActivityDate())
                .lessonsCompleted((int) lessons)
                .kanjiCompleted((int) kanji)
                .vocabularyCompleted((int) vocabulary)
                .grammarCompleted((int) grammar)
                .kanaCompleted((int) kana)
                .totalExamsTaken(totalExams)
                .averageExamScore(avgScore)
                .highestExamScore(maxScore)
                .build();
    }

    // ── UC-21: Submission list ───────────────────────────────────────────────

    @Transactional(readOnly = true)
    public Page<SubmissionSummaryResponse> getStudentSubmissions(
            Long studentId, String type, String status, int page, int size) {
        findStudentOrThrow(studentId);
        if (submissionRepository == null) {
            return Page.empty(PageRequest.of(page, size));
        }
        StudentSubmission.SubmissionType subType = parseSubmissionType(type);
        StudentSubmission.SubmissionStatus subStatus = parseSubmissionStatus(status);
        return submissionRepository
                .findByStudentIdAndFilters(studentId, subType, subStatus, PageRequest.of(page, size))
                .map(this::toSubmissionSummary);
    }

    // ── UC-23: Suspend account ───────────────────────────────────────────────

    @Transactional
    public StudentDetailResponse suspendStudent(String actorEmail, Long studentId, String reason) {
        if (reason == null || reason.trim().length() < 10 || reason.trim().length() > 500) {
            throw new BusinessException(400, "VALIDATION_FAILED", "Lý do khóa phải từ 10 đến 500 ký tự");
        }
        StudentUser student = findStudentOrThrow(studentId);
        if (student.getStatus() == StudentUser.StudentStatus.SUSPENDED) {
            throw new BusinessException(409, "ALREADY_IN_STATE", "Tài khoản đã ở trạng thái bị đình chỉ");
        }
        student.setStatus(StudentUser.StudentStatus.SUSPENDED);
        student.setSuspendReason(reason.trim());
        studentUserRepository.save(student);

        // Revoke ALL active tokens — must be sync, same transaction (NFR-STUDENT-02)
        authTokenRepository.revokeAllActiveByStudentId(studentId, LocalDateTime.now());

        var staffUser = staffUserRepository.findByEmail(actorEmail).orElse(null);
        adminAuditLogRepository.save(AdminAuditLog.builder()
                .staffActor(staffUser)
                .action("STUDENT_SUSPENDED")
                .targetTable("student_users")
                .targetId(studentId)
                .description("Suspended student " + student.getEmail() + ". Reason: " + reason.trim())
                .build());

        log.info("[StudentMgmt] Staff {} suspended student {} reason: {}", actorEmail, studentId, reason.trim());
        return toStudentDetailResponse(student);
    }

    // ── UC-23: Activate account ──────────────────────────────────────────────

    @Transactional
    public StudentDetailResponse activateStudent(String actorEmail, Long studentId) {
        StudentUser student = findStudentOrThrow(studentId);
        if (student.getStatus() == StudentUser.StudentStatus.ACTIVE) {
            throw new BusinessException(409, "ALREADY_IN_STATE", "Tài khoản đã đang ở trạng thái hoạt động");
        }
        student.setStatus(StudentUser.StudentStatus.ACTIVE);
        student.setSuspendReason(null);
        studentUserRepository.save(student);

        var staffUser = staffUserRepository.findByEmail(actorEmail).orElse(null);
        adminAuditLogRepository.save(AdminAuditLog.builder()
                .staffActor(staffUser)
                .action("STUDENT_ACTIVATED")
                .targetTable("student_users")
                .targetId(studentId)
                .description("Activated student " + student.getEmail())
                .build());

        log.info("[StudentMgmt] Staff {} activated student {}", actorEmail, studentId);
        return toStudentDetailResponse(student);
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private StudentUser findStudentOrThrow(Long id) {
        return studentUserRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tài khoản học viên"));
    }

    private long countCompleted(Long studentId, StudentContentProgress.ContentType type) {
        if (progressRepository == null) return 0L;
        return progressRepository.countCompletedByStudentIdAndContentType(studentId, type);
    }

    private long countExams(Long studentId) {
        if (testAttemptRepository == null) return 0L;
        return testAttemptRepository.countExamsByStudentId(studentId);
    }

    private StudentDetailResponse toStudentDetailResponse(StudentUser s) {
        return StudentDetailResponse.builder()
                .studentId(s.getId())
                .fullName(s.getFullName())
                .email(s.getEmail())
                .phone(s.getPhone())
                .avatarUrl(s.getAvatarUrl())
                .status(s.getStatus().getValue())
                .suspendReason(s.getSuspendReason())
                .currentJlptLevel(s.getCurrentJlptLevel() != null ? s.getCurrentJlptLevel().name() : null)
                .targetJlptLevel(s.getTargetJlptLevel() != null ? s.getTargetJlptLevel().name() : null)
                .currentStreak(s.getCurrentStreak())
                .longestStreak(s.getLongestStreak())
                .lastActivityDate(s.getLastActivityDate())
                .lastLoginAt(s.getLastLoginAt())
                .createdAt(s.getCreatedAt())
                .build();
    }

    private SubmissionSummaryResponse toSubmissionSummary(StudentSubmission s) {
        BigDecimal finalScore = s.getManualScore() != null ? s.getManualScore() : s.getAiOverallScore();
        return SubmissionSummaryResponse.builder()
                .submissionId(s.getId())
                .submissionType(s.getSubmissionType().getValue())
                .status(s.getStatus().getValue())
                .aiOverallScore(s.getAiOverallScore())
                .manualScore(s.getManualScore())
                .finalScore(finalScore)
                .gradedByStaffName(s.getGradedBy() != null ? s.getGradedBy().getFullName() : null)
                .submittedAt(s.getSubmittedAt())
                .build();
    }

    private StudentSubmission.SubmissionType parseSubmissionType(String type) {
        if (type == null) return null;
        try {
            return StudentSubmission.SubmissionType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BusinessException(400, "VALIDATION_FAILED", "Loại bài nộp không hợp lệ: " + type);
        }
    }

    private StudentSubmission.SubmissionStatus parseSubmissionStatus(String status) {
        if (status == null) return null;
        try {
            return StudentSubmission.SubmissionStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BusinessException(400, "VALIDATION_FAILED", "Trạng thái bài nộp không hợp lệ: " + status);
        }
    }
}
