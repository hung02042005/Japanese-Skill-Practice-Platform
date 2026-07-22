/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.staffcontent.exam.service;

import com.jlpt.feature.staff.StaffUser;
import com.jlpt.feature.staff.StaffUserRepository;
import com.jlpt.feature.staffcontent.exam.dto.CreateExamRequest;
import com.jlpt.feature.staffcontent.exam.dto.ExamAssignQuestionsRequest;
import com.jlpt.feature.staffcontent.exam.dto.ExamAssignResultResponse;
import com.jlpt.feature.staffcontent.exam.dto.ExamDetailResponse;
import com.jlpt.feature.staffcontent.exam.dto.ExamListResponse;
import com.jlpt.feature.staffcontent.exam.dto.ExamSubmitReviewResponse;
import com.jlpt.feature.staffcontent.exam.dto.ExamSummaryResponse;
import com.jlpt.feature.staffcontent.exam.dto.UpdateExamRequest;
import com.jlpt.feature.staffcontent.exam.entity.ExamAssessmentEntity;
import com.jlpt.feature.staffcontent.exam.entity.ExamAssignmentEntity;
import com.jlpt.feature.staffcontent.exam.entity.ExamQuestionRefEntity;
import com.jlpt.feature.staffcontent.exam.exception.ExamBusinessException;
import com.jlpt.feature.staffcontent.exam.repository.ExamAssessmentRepository;
import com.jlpt.feature.staffcontent.exam.repository.ExamAssignmentRepository;
import com.jlpt.feature.staffcontent.exam.repository.ExamQuestionRefRepository;
import com.jlpt.shared.exception.ForbiddenException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * UC-28 — Manage JLPT Mock Exams. Create / list / detail / update metadata / assign questions
 * / submit-review for {@code assessment_type = 'exam'}. All invariant checks (section validity,
 * level match, score sum, published-lock, ownership) run at this layer inside a single
 * {@code @Transactional} boundary.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class StaffExamService {

    private static final String TYPE_EXAM = "exam";
    private static final String PARENT_ASSESSMENT = "assessment";
    private static final String ST_DRAFT = "draft";
    private static final String ST_REJECTED = "rejected";
    private static final String ST_PENDING = "pending_review";
    private static final String ST_PUBLISHED = "published";
    private static final String ST_DELETED = "deleted";
    private static final String Q_PUBLISHED = "published";

    private static final Set<String> JLPT_LEVELS = Set.of("N5", "N4", "N3", "N2", "N1");
    private static final Set<String> VALID_SECTIONS = Set.of("vocabulary", "grammar", "kanji", "reading", "listening");

    private final ExamAssessmentRepository assessmentRepository;
    private final ExamAssignmentRepository assignmentRepository;
    private final ExamQuestionRefRepository questionRefRepository;
    private final StaffUserRepository staffUserRepository;

    // ---------------------------------------------------------------------
    // Create
    // ---------------------------------------------------------------------

    @Transactional
    public ExamDetailResponse createExam(CreateExamRequest request, String staffEmail) {
        StaffUser staff = resolveStaff(staffEmail);
        guardNoPublish(request.getStatus());

        validateLevel(request.getJlptLevel());
        validateScoreRange(request.getDurationMin(), request.getPassScore(), request.getTotalScore());

        ExamAssessmentEntity exam = ExamAssessmentEntity.builder()
                .assessmentType(TYPE_EXAM) // FR-28-05: force type
                .title(request.getTitle().trim())
                .jlptLevel(request.getJlptLevel().trim().toUpperCase())
                .durationMin(request.getDurationMin())
                .passScore(request.getPassScore())
                .totalScore(request.getTotalScore())
                .description(trimToNull(request.getDescription()))
                .status(ST_DRAFT) // FR-28-01: new exam = draft
                .createdBy(staff.getId())
                .build();

        ExamAssessmentEntity saved = assessmentRepository.save(exam);
        log.info("[EXAM] Staff {} CREATE exam {}", staff.getId(), saved.getId());
        return toDetail(saved, List.of(), List.of());
    }

    // ---------------------------------------------------------------------
    // List / Detail
    // ---------------------------------------------------------------------

    @Transactional(readOnly = true)
    public ExamListResponse listExams(String level, String status, int page, int size, String staffEmail) {
        resolveStaff(staffEmail);
        int safeSize = Math.min(Math.max(size, 1), 100);
        Pageable pageable = PageRequest.of(Math.max(page, 0), safeSize);
        String levelFilter = StringUtils.hasText(level) ? level.trim().toUpperCase() : null;
        String statusFilter = StringUtils.hasText(status) ? status.trim().toLowerCase() : null;

        Page<ExamAssessmentEntity> result =
                assessmentRepository.findExamsWithFilters(levelFilter, statusFilter, pageable);

        List<ExamSummaryResponse> content =
                result.getContent().stream().map(this::toSummary).toList();

        return ExamListResponse.builder()
                .content(content)
                .totalElements(result.getTotalElements())
                .totalPages(result.getTotalPages())
                .build();
    }

    @Transactional(readOnly = true)
    public ExamDetailResponse getExam(Long assessmentId, String staffEmail) {
        resolveStaff(staffEmail);
        ExamAssessmentEntity exam = requireExam(assessmentId);
        List<ExamAssignmentEntity> assignments =
                assignmentRepository.findByParentTypeAndParentIdOrderByDisplayOrderAsc(PARENT_ASSESSMENT, assessmentId);
        List<Object[]> sectionAggregates = assignmentRepository.sumBySectionGrouped(PARENT_ASSESSMENT, assessmentId);
        return toDetail(exam, assignments, sectionAggregates);
    }

    // ---------------------------------------------------------------------
    // Update metadata
    // ---------------------------------------------------------------------

    @Transactional
    public ExamDetailResponse updateExam(Long assessmentId, UpdateExamRequest request, String staffEmail) {
        StaffUser staff = resolveStaff(staffEmail);
        guardNoPublish(request.getStatus());

        ExamAssessmentEntity exam = requireExam(assessmentId);
        guardOwnership(exam, staff);
        guardEditableStatus(exam.getStatus());

        validateLevel(request.getJlptLevel());
        validateScoreRange(request.getDurationMin(), request.getPassScore(), request.getTotalScore());

        exam.setTitle(request.getTitle().trim());
        exam.setJlptLevel(request.getJlptLevel().trim().toUpperCase());
        exam.setDurationMin(request.getDurationMin());
        exam.setPassScore(request.getPassScore());
        exam.setTotalScore(request.getTotalScore());
        exam.setDescription(trimToNull(request.getDescription()));
        // assessment_type stays 'exam' (FR-28-18 — never touched)

        ExamAssessmentEntity saved = assessmentRepository.save(exam);
        log.info("[EXAM] Staff {} UPDATE exam {}", staff.getId(), saved.getId());
        List<ExamAssignmentEntity> assignments =
                assignmentRepository.findByParentTypeAndParentIdOrderByDisplayOrderAsc(PARENT_ASSESSMENT, assessmentId);
        List<Object[]> sectionAggregates = assignmentRepository.sumBySectionGrouped(PARENT_ASSESSMENT, assessmentId);
        return toDetail(saved, assignments, sectionAggregates);
    }

    // ---------------------------------------------------------------------
    // Assign questions (replace semantics, atomic)
    // ---------------------------------------------------------------------

    @Transactional
    public ExamAssignResultResponse assignQuestions(
            Long assessmentId, ExamAssignQuestionsRequest request, String staffEmail) {
        StaffUser staff = resolveStaff(staffEmail);
        ExamAssessmentEntity exam = requireExam(assessmentId);
        guardOwnership(exam, staff);

        if (ST_PUBLISHED.equals(exam.getStatus())) {
            throw ExamBusinessException.examPublished(); // FR-28-28
        }
        guardEditableStatus(exam.getStatus()); // FR-28-29

        List<ExamAssignQuestionsRequest.ExamAssignmentItem> items = request.getAssignments();

        // Reject duplicate questionId within the payload (FR-28-22)
        Set<Long> seen = new HashSet<>();
        for (ExamAssignQuestionsRequest.ExamAssignmentItem item : items) {
            if (!seen.add(item.getQuestionId())) {
                throw ExamBusinessException.duplicateAssignment(item.getQuestionId());
            }
        }

        // Validate each item
        String examLevel = exam.getJlptLevel();
        for (ExamAssignQuestionsRequest.ExamAssignmentItem item : items) {
            // sectionName must be valid (FR-28-21)
            String section = item.getSectionName().trim().toLowerCase();
            if (!VALID_SECTIONS.contains(section)) {
                throw ExamBusinessException.invalidSection(item.getSectionName());
            }

            // Question must exist and be published (FR-28-21)
            ExamQuestionRefEntity ref = questionRefRepository
                    .findById(item.getQuestionId())
                    .orElseThrow(() -> ExamBusinessException.questionNotFound(item.getQuestionId()));
            if (ref.getStatus() == null || ST_DELETED.equalsIgnoreCase(ref.getStatus())) {
                throw ExamBusinessException.questionNotFound(item.getQuestionId());
            }
            if (!Q_PUBLISHED.equalsIgnoreCase(ref.getStatus())) {
                throw ExamBusinessException.questionNotPublished(item.getQuestionId());
            }

            // Level must match exam level (FR-28-25)
            if (ref.getJlptLevel() != null && !ref.getJlptLevel().equalsIgnoreCase(examLevel)) {
                throw ExamBusinessException.levelMismatch(item.getQuestionId(), ref.getJlptLevel(), examLevel);
            }
        }

        // Replace: remove existing assignments, then insert the new set (FR-28-23)
        assignmentRepository.deleteByParent(PARENT_ASSESSMENT, assessmentId);
        List<ExamAssignmentEntity> toSave = new ArrayList<>();
        for (ExamAssignQuestionsRequest.ExamAssignmentItem item : items) {
            toSave.add(ExamAssignmentEntity.builder()
                    .parentType(PARENT_ASSESSMENT)
                    .parentId(assessmentId)
                    .questionId(item.getQuestionId())
                    .sectionName(item.getSectionName().trim().toLowerCase())
                    .displayOrder(item.getDisplayOrder())
                    .score(item.getScore())
                    .build());
        }
        assignmentRepository.saveAll(toSave);
        // touch updated_at on the parent exam
        exam.setUpdatedAt(LocalDateTime.now());
        assessmentRepository.save(exam);

        BigDecimal sum = toSave.stream().map(ExamAssignmentEntity::getScore).reduce(BigDecimal.ZERO, BigDecimal::add);
        boolean matched = scoreMatches(sum, exam.getTotalScore());

        // Build section summaries
        Map<String, List<ExamAssignmentEntity>> bySection =
                toSave.stream().collect(Collectors.groupingBy(ExamAssignmentEntity::getSectionName));
        List<ExamAssignResultResponse.SectionSummary> sectionSummaries = bySection.entrySet().stream()
                .map(e -> ExamAssignResultResponse.SectionSummary.builder()
                        .sectionName(e.getKey())
                        .sectionScore(e.getValue().stream()
                                .map(ExamAssignmentEntity::getScore)
                                .reduce(BigDecimal.ZERO, BigDecimal::add))
                        .questionCount(e.getValue().size())
                        .build())
                .sorted(java.util.Comparator.comparing(ExamAssignResultResponse.SectionSummary::getSectionName))
                .toList();

        log.info("[EXAM] Staff {} ASSIGN-QUESTIONS exam {} ({} items)", staff.getId(), assessmentId, toSave.size());

        return ExamAssignResultResponse.builder()
                .assessmentId(assessmentId)
                .assignedCount(toSave.size())
                .assignedScoreSum(sum)
                .totalScore(exam.getTotalScore())
                .scoreMatched(matched)
                .sectionSummaries(sectionSummaries)
                .build();
    }

    // ---------------------------------------------------------------------
    // Submit for review
    // ---------------------------------------------------------------------

    @Transactional
    public ExamSubmitReviewResponse submitForReview(Long assessmentId, String staffEmail) {
        StaffUser staff = resolveStaff(staffEmail);
        ExamAssessmentEntity exam = requireExam(assessmentId);
        guardOwnership(exam, staff);

        // PA-3: chặn cứng bài rỗng ngay khi gửi duyệt (FR-28-31).
        long count = assignmentRepository.countByParentTypeAndParentId(PARENT_ASSESSMENT, assessmentId);
        if (count == 0) {
            throw ExamBusinessException.emptyExam();
        }

        // Khớp tổng điểm (FR-28-30) CỐ Ý mềm ở đây: staff còn sửa dần, `scoreMatched` là cảnh báo;
        // chốt cứng đặt ở bước manager approve (AssessmentContentHandler.approve) — defense-in-depth.

        if (!isEditable(exam.getStatus())) {
            throw ExamBusinessException.invalidStatusTransition(exam.getStatus()); // FR-28-32
        }

        exam.setStatus(ST_PENDING);
        assessmentRepository.save(exam);
        log.info("[EXAM] Staff {} SUBMIT-REVIEW exam {}", staff.getId(), assessmentId);

        return ExamSubmitReviewResponse.builder()
                .contentId(assessmentId)
                .contentType("exam")
                .status(ST_PENDING)
                .build();
    }

    // ---------------------------------------------------------------------
    // Guards & validation
    // ---------------------------------------------------------------------

    private StaffUser resolveStaff(String email) {
        return staffUserRepository
                .findByEmail(email)
                .orElseThrow(() -> new ForbiddenException("Tài khoản nhân viên không tồn tại"));
    }

    private ExamAssessmentEntity requireExam(Long id) {
        return assessmentRepository
                .findByIdAndAssessmentTypeAndStatusNot(id, TYPE_EXAM, ST_DELETED)
                .orElseThrow(() -> ExamBusinessException.examNotFound(id));
    }

    /** FR-28-34: owner-only, unless caller is STAFF_MANAGER. */
    private void guardOwnership(ExamAssessmentEntity exam, StaffUser staff) {
        if (staff.getStaffRole() == StaffUser.StaffRole.STAFF_MANAGER) {
            return;
        }
        if (exam.getCreatedBy() == null || !exam.getCreatedBy().equals(staff.getId())) {
            throw ExamBusinessException.ownershipDenied();
        }
    }

    /** FR-28-33: Staff cannot drive an exam to published/archived through this use case. */
    private void guardNoPublish(String requestedStatus) {
        if (requestedStatus == null) {
            return;
        }
        String s = requestedStatus.trim().toLowerCase();
        if (ST_PUBLISHED.equals(s) || "archived".equals(s)) {
            throw ExamBusinessException.publishNotAllowed();
        }
    }

    /** FR-28-17/29: editable only while status in {draft, rejected}. */
    private void guardEditableStatus(String status) {
        if (!isEditable(status)) {
            throw ExamBusinessException.invalidStatusTransition(status);
        }
    }

    private boolean isEditable(String status) {
        return ST_DRAFT.equals(status) || ST_REJECTED.equals(status);
    }

    private void validateLevel(String level) {
        if (level == null || !JLPT_LEVELS.contains(level.trim().toUpperCase())) {
            throw ExamBusinessException.invalidJlptLevel(); // FR-28-04
        }
    }

    private void validateScoreRange(Integer durationMin, Integer passScore, Integer totalScore) {
        if (durationMin == null
                || durationMin <= 0
                || totalScore == null
                || totalScore <= 0
                || passScore == null
                || passScore < 0
                || passScore > totalScore) {
            throw ExamBusinessException.validationFailed(
                    "Yêu cầu durationMin>0, totalScore>0, 0<=passScore<=totalScore"); // FR-28-02..04
        }
    }

    private boolean scoreMatches(BigDecimal assignedSum, Integer totalScore) {
        BigDecimal sum = assignedSum == null ? BigDecimal.ZERO : assignedSum;
        return totalScore != null && sum.compareTo(BigDecimal.valueOf(totalScore)) == 0;
    }

    private String trimToNull(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    // ---------------------------------------------------------------------
    // Mapping (entity → DTO, ADR-005)
    // ---------------------------------------------------------------------

    private ExamSummaryResponse toSummary(ExamAssessmentEntity exam) {
        long questionCount = assignmentRepository.countByParentTypeAndParentId(PARENT_ASSESSMENT, exam.getId());
        return ExamSummaryResponse.builder()
                .assessmentId(exam.getId())
                .title(exam.getTitle())
                .assessmentType(exam.getAssessmentType())
                .jlptLevel(exam.getJlptLevel())
                .durationMin(exam.getDurationMin())
                .passScore(exam.getPassScore())
                .totalScore(exam.getTotalScore())
                .questionCount(questionCount)
                .status(exam.getStatus())
                .createdBy(String.valueOf(exam.getCreatedBy()))
                .updatedAt(exam.getUpdatedAt())
                .build();
    }

    private ExamDetailResponse toDetail(
            ExamAssessmentEntity exam, List<ExamAssignmentEntity> assignments, List<Object[]> sectionAggregates) {

        BigDecimal sum =
                assignments.stream().map(ExamAssignmentEntity::getScore).reduce(BigDecimal.ZERO, BigDecimal::add);

        // Build section list from aggregate query
        List<ExamDetailResponse.ExamSection> sections = sectionAggregates.stream()
                .map(row -> ExamDetailResponse.ExamSection.builder()
                        .sectionName((String) row[0])
                        .sectionScore((BigDecimal) row[1])
                        .questionCount((Long) row[2])
                        .build())
                .sorted(java.util.Comparator.comparing(ExamDetailResponse.ExamSection::getSectionName))
                .toList();

        List<ExamDetailResponse.AssignedQuestion> questions = assignments.stream()
                .map(a -> ExamDetailResponse.AssignedQuestion.builder()
                        .assignmentId(a.getId())
                        .questionId(a.getQuestionId())
                        .displayOrder(a.getDisplayOrder())
                        .score(a.getScore())
                        .sectionName(a.getSectionName())
                        .questionText(questionRefRepository
                                .findById(a.getQuestionId())
                                .map(ExamQuestionRefEntity::getQuestionText)
                                .orElse(null))
                        .build())
                .toList();

        return ExamDetailResponse.builder()
                .assessmentId(exam.getId())
                .assessmentType(exam.getAssessmentType())
                .title(exam.getTitle())
                .jlptLevel(exam.getJlptLevel())
                .description(exam.getDescription())
                .durationMin(exam.getDurationMin())
                .passScore(exam.getPassScore())
                .totalScore(exam.getTotalScore())
                .status(exam.getStatus())
                .assignedScoreSum(sum)
                .scoreMatched(scoreMatches(sum, exam.getTotalScore()))
                .sections(sections)
                .questions(questions)
                .createdBy(exam.getCreatedBy())
                .createdAt(exam.getCreatedAt())
                .updatedAt(exam.getUpdatedAt())
                .build();
    }
}
