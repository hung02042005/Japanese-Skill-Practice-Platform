/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.staffcontent.student;

import com.jlpt.feature.assessment.AssessmentRepository;
import com.jlpt.feature.assessment.TestAttempt;
import com.jlpt.feature.assessment.TestAttemptRepository;
import com.jlpt.feature.staff.StaffManagerGuard;
import com.jlpt.feature.staffcontent.student.dto.StaffStudentListResponse;
import com.jlpt.feature.staffcontent.student.dto.StaffStudentProgressResponse;
import com.jlpt.feature.staffcontent.student.dto.StaffStudentSummaryResponse;
import com.jlpt.feature.student.StudentContentProgress.ContentType;
import com.jlpt.feature.student.StudentContentProgress.ProgressStatus;
import com.jlpt.feature.student.StudentContentProgressRepository;
import com.jlpt.feature.student.StudentUser;
import com.jlpt.feature.student.StudentUserRepository;
import com.jlpt.shared.exception.ResourceNotFoundException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/** UC — Staff xem & quản lý học viên (danh sách, tiến độ, tạm khoá/mở khoá). */
@Service
@RequiredArgsConstructor
public class StaffStudentService {

    private static final List<TestAttempt.AttemptStatus> SUBMITTED_STATUSES =
            List.of(TestAttempt.AttemptStatus.SUBMITTED, TestAttempt.AttemptStatus.AUTO_SUBMITTED);

    private final StudentUserRepository studentUserRepository;
    private final StudentContentProgressRepository progressRepository;
    private final TestAttemptRepository testAttemptRepository;
    private final AssessmentRepository assessmentRepository;
    private final StaffManagerGuard staffManagerGuard;

    private static final String FORBIDDEN_MSG = "Chỉ Staff Manager mới có quyền khoá/mở khoá học viên";

    @Transactional(readOnly = true)
    public StaffStudentListResponse listStudents(String search, String level, String status, int page, int size) {
        int safeSize = Math.min(Math.max(size, 1), 100);
        String q = StringUtils.hasText(search) ? "%" + search.trim() + "%" : null;
        String statusFilter = StringUtils.hasText(status) ? status.trim() : null;
        String levelFilter = StringUtils.hasText(level) ? level.trim() : null;

        Page<StudentUser> result = studentUserRepository.findAllAdminFiltered(
                q, statusFilter, levelFilter, PageRequest.of(Math.max(page, 0), safeSize));

        List<StaffStudentSummaryResponse> content = result.getContent().stream()
                .map(s -> StaffStudentSummaryResponse.builder()
                        .studentId(s.getId())
                        .fullName(s.getFullName())
                        .email(s.getEmail())
                        .jlptLevel(
                                s.getCurrentJlptLevel() != null
                                        ? s.getCurrentJlptLevel().name()
                                        : null)
                        .status(s.getStatus() != null ? s.getStatus().getValue() : null)
                        .subscription("FREE")
                        .build())
                .toList();

        return StaffStudentListResponse.builder()
                .content(content)
                .totalElements(result.getTotalElements())
                .totalPages(result.getTotalPages())
                .page(result.getNumber())
                .size(result.getSize())
                .build();
    }

    @Transactional(readOnly = true)
    public StaffStudentProgressResponse getProgress(Long studentId) {
        StudentUser student = studentUserRepository
                .findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("StudentUser", studentId));

        long lessonsCompleted =
                progressRepository.countCompleted(studentId, ContentType.LESSON, ProgressStatus.COMPLETED);

        List<TestAttempt> attempts = testAttemptRepository.findByStudent_IdAndStatusIn(studentId, SUBMITTED_STATUSES);

        int avg = (int)
                Math.round(attempts.stream().mapToInt(this::scorePct).average().orElse(0));

        List<StaffStudentProgressResponse.AttemptItem> recent = attempts.stream()
                .sorted(Comparator.comparing(
                        TestAttempt::getSubmittedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(10)
                .map(a -> StaffStudentProgressResponse.AttemptItem.builder()
                        .attemptId(a.getId())
                        .title(resolveTitle(a.getParentId()))
                        .score(a.getTotalScore() != null ? a.getTotalScore().intValue() : 0)
                        .maxScore(a.getMaxScore() != null ? a.getMaxScore().intValue() : 0)
                        .scorePct(scorePct(a))
                        .takenAt(a.getSubmittedAt())
                        .build())
                .toList();

        return StaffStudentProgressResponse.builder()
                .studentId(student.getId())
                .fullName(student.getFullName())
                .jlptLevel(
                        student.getCurrentJlptLevel() != null
                                ? student.getCurrentJlptLevel().name()
                                : null)
                .currentStreak(student.getCurrentStreak() != null ? student.getCurrentStreak() : 0)
                .lessonsCompleted(lessonsCompleted)
                .averageQuizScore(avg)
                .recentAttempts(recent)
                .build();
    }

    @Transactional
    public StaffStudentSummaryResponse suspend(String actorEmail, Long studentId, String reason) {
        staffManagerGuard.requireManager(actorEmail, FORBIDDEN_MSG);
        StudentUser s = studentUserRepository
                .findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("StudentUser", studentId));
        s.setStatus(StudentUser.StudentStatus.SUSPENDED);
        s.setSuspendReason(reason);
        studentUserRepository.save(s);
        return toSummary(s);
    }

    @Transactional
    public StaffStudentSummaryResponse activate(String actorEmail, Long studentId) {
        staffManagerGuard.requireManager(actorEmail, FORBIDDEN_MSG);
        StudentUser s = studentUserRepository
                .findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("StudentUser", studentId));
        s.setStatus(StudentUser.StudentStatus.ACTIVE);
        s.setSuspendReason(null);
        studentUserRepository.save(s);
        return toSummary(s);
    }

    private int scorePct(TestAttempt a) {
        if (a.getTotalScore() == null
                || a.getMaxScore() == null
                || a.getMaxScore().compareTo(BigDecimal.ZERO) <= 0) {
            return 0;
        }
        return a.getTotalScore()
                .multiply(BigDecimal.valueOf(100))
                .divide(a.getMaxScore(), 0, RoundingMode.HALF_UP)
                .intValue();
    }

    private String resolveTitle(Long assessmentId) {
        if (assessmentId == null) {
            return "(Không rõ)";
        }
        return assessmentRepository
                .findById(assessmentId)
                .map(a -> a.getTitle())
                .orElse("(Không rõ)");
    }

    private StaffStudentSummaryResponse toSummary(StudentUser s) {
        return StaffStudentSummaryResponse.builder()
                .studentId(s.getId())
                .fullName(s.getFullName())
                .email(s.getEmail())
                .jlptLevel(
                        s.getCurrentJlptLevel() != null
                                ? s.getCurrentJlptLevel().name()
                                : null)
                .status(s.getStatus() != null ? s.getStatus().getValue() : null)
                .subscription("FREE")
                .build();
    }
}
