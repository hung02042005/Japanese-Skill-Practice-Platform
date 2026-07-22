/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.staffcontent.quiz.service;

import com.jlpt.feature.learning.Lesson;
import com.jlpt.feature.learning.LessonRepository;
import com.jlpt.feature.staff.StaffUser;
import com.jlpt.feature.staff.StaffUserRepository;
import com.jlpt.feature.staffcontent.quiz.dto.AssignQuestionsRequest;
import com.jlpt.feature.staffcontent.quiz.dto.AssignResultResponse;
import com.jlpt.feature.staffcontent.quiz.dto.CreateQuizRequest;
import com.jlpt.feature.staffcontent.quiz.dto.QuizDetailResponse;
import com.jlpt.feature.staffcontent.quiz.dto.QuizListResponse;
import com.jlpt.feature.staffcontent.quiz.dto.QuizSubmitReviewResponse;
import com.jlpt.feature.staffcontent.quiz.dto.QuizSummaryResponse;
import com.jlpt.feature.staffcontent.quiz.dto.UpdateQuizRequest;
import com.jlpt.feature.staffcontent.quiz.entity.QuizAssessmentEntity;
import com.jlpt.feature.staffcontent.quiz.entity.QuizAssignmentEntity;
import com.jlpt.feature.staffcontent.quiz.entity.QuizQuestionRefEntity;
import com.jlpt.feature.staffcontent.quiz.exception.QuizBusinessException;
import com.jlpt.feature.staffcontent.quiz.repository.QuizAssessmentRepository;
import com.jlpt.feature.staffcontent.quiz.repository.QuizAssignmentRepository;
import com.jlpt.feature.staffcontent.quiz.repository.QuizQuestionRefRepository;
import com.jlpt.shared.exception.ForbiddenException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * UC-26 — Manage Quiz. Create / list / detail / update metadata / assign questions / submit-review
 * for {@code assessment_type = 'quiz'}. All invariant checks (score sum, published-lock, ownership)
 * run at this layer inside a single {@code @Transactional} boundary (NFR-26-03/04).
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class StaffQuizService {

    private static final String TYPE_QUIZ = "quiz";
    private static final String PARENT_ASSESSMENT = "assessment";
    private static final String ST_DRAFT = "draft";
    private static final String ST_REJECTED = "rejected";
    private static final String ST_PENDING = "pending_review";
    private static final String ST_PUBLISHED = "published";
    private static final String ST_DELETED = "deleted";
    private static final String Q_PUBLISHED = "published";
    private static final Set<String> JLPT_LEVELS = Set.of("N5", "N4", "N3", "N2", "N1");

    private final QuizAssessmentRepository assessmentRepository;
    private final QuizAssignmentRepository assignmentRepository;
    private final QuizQuestionRefRepository questionRefRepository;
    private final LessonRepository lessonRepository;
    private final StaffUserRepository staffUserRepository;

    // ---------------------------------------------------------------------
    // Create
    // ---------------------------------------------------------------------

    @Transactional
    public QuizDetailResponse createQuiz(CreateQuizRequest request, String staffEmail) {
        StaffUser staff = resolveStaff(staffEmail);
        guardNoPublish(request.getStatus());

        validateLevel(request.getJlptLevel());
        validateLessonOrTopic(request.getLessonId(), request.getTopic());
        validateScoreRange(request.getDurationMin(), request.getPassScore(), request.getTotalScore());
        if (request.getLessonId() != null) {
            requireLesson(request.getLessonId());
        }

        QuizAssessmentEntity quiz = QuizAssessmentEntity.builder()
                .assessmentType(TYPE_QUIZ) // FR-26-07: force type
                .title(request.getTitle().trim())
                .lessonId(request.getLessonId())
                .topic(trimToNull(request.getTopic()))
                .jlptLevel(request.getJlptLevel().trim().toUpperCase())
                .durationMin(request.getDurationMin())
                .passScore(request.getPassScore())
                .totalScore(request.getTotalScore())
                .status(ST_DRAFT) // FR-26-01
                .createdBy(staff.getId())
                .build();

        QuizAssessmentEntity saved = assessmentRepository.save(quiz);
        log.info("[INFO] Staff {} CREATE assessment {}", staff.getId(), saved.getId());
        return toDetail(saved, List.of());
    }

    // ---------------------------------------------------------------------
    // List / Detail
    // ---------------------------------------------------------------------

    @Transactional(readOnly = true)
    public QuizListResponse listQuizzes(
            String level, String status, Long lessonId, int page, int size, String staffEmail) {
        resolveStaff(staffEmail);
        int safeSize = Math.min(Math.max(size, 1), 100);
        Pageable pageable = PageRequest.of(Math.max(page, 0), safeSize);
        String levelFilter = StringUtils.hasText(level) ? level.trim().toUpperCase() : null;
        String statusFilter = StringUtils.hasText(status) ? status.trim().toLowerCase() : null;

        Page<QuizAssessmentEntity> result =
                assessmentRepository.findQuizzesWithFilters(levelFilter, statusFilter, lessonId, pageable);

        List<QuizSummaryResponse> content =
                result.getContent().stream().map(this::toSummary).toList();

        return QuizListResponse.builder()
                .content(content)
                .totalElements(result.getTotalElements())
                .totalPages(result.getTotalPages())
                .build();
    }

    @Transactional(readOnly = true)
    public QuizDetailResponse getQuiz(Long assessmentId, String staffEmail) {
        resolveStaff(staffEmail);
        QuizAssessmentEntity quiz = requireQuiz(assessmentId);
        List<QuizAssignmentEntity> assignments =
                assignmentRepository.findByParentTypeAndParentIdOrderByDisplayOrderAsc(PARENT_ASSESSMENT, assessmentId);
        return toDetail(quiz, assignments);
    }

    // ---------------------------------------------------------------------
    // Update metadata
    // ---------------------------------------------------------------------

    @Transactional
    public QuizDetailResponse updateQuiz(Long assessmentId, UpdateQuizRequest request, String staffEmail) {
        StaffUser staff = resolveStaff(staffEmail);
        guardNoPublish(request.getStatus());

        QuizAssessmentEntity quiz = requireQuiz(assessmentId);
        guardOwnership(quiz, staff);
        guardEditableStatus(quiz.getStatus());

        validateLevel(request.getJlptLevel());
        validateLessonOrTopic(request.getLessonId(), request.getTopic());
        validateScoreRange(request.getDurationMin(), request.getPassScore(), request.getTotalScore());
        if (request.getLessonId() != null) {
            requireLesson(request.getLessonId());
        }

        quiz.setTitle(request.getTitle().trim());
        quiz.setLessonId(request.getLessonId());
        quiz.setTopic(trimToNull(request.getTopic()));
        quiz.setJlptLevel(request.getJlptLevel().trim().toUpperCase());
        quiz.setDurationMin(request.getDurationMin());
        quiz.setPassScore(request.getPassScore());
        quiz.setTotalScore(request.getTotalScore());
        // assessment_type stays 'quiz' (FR-26-17 — never touched)

        QuizAssessmentEntity saved = assessmentRepository.save(quiz);
        log.info("[INFO] Staff {} UPDATE assessment {}", staff.getId(), saved.getId());
        List<QuizAssignmentEntity> assignments =
                assignmentRepository.findByParentTypeAndParentIdOrderByDisplayOrderAsc(PARENT_ASSESSMENT, assessmentId);
        return toDetail(saved, assignments);
    }

    // ---------------------------------------------------------------------
    // Assign questions (replace semantics, atomic)
    // ---------------------------------------------------------------------

    @Transactional
    public AssignResultResponse assignQuestions(Long assessmentId, AssignQuestionsRequest request, String staffEmail) {
        StaffUser staff = resolveStaff(staffEmail);
        QuizAssessmentEntity quiz = requireQuiz(assessmentId);
        guardOwnership(quiz, staff);

        if (ST_PUBLISHED.equals(quiz.getStatus())) {
            throw QuizBusinessException.assessmentPublished(); // FR-26-24
        }
        guardEditableStatus(quiz.getStatus()); // FR-26-25

        List<AssignQuestionsRequest.AssignmentItem> items = request.getAssignments();

        // Reject duplicate questionId within the payload (FR-26-22)
        Set<Long> seen = new HashSet<>();
        for (AssignQuestionsRequest.AssignmentItem item : items) {
            if (!seen.add(item.getQuestionId())) {
                throw QuizBusinessException.duplicateAssignment(item.getQuestionId());
            }
        }

        // Each question must exist, not be deleted, and be published (FR-26-21)
        for (AssignQuestionsRequest.AssignmentItem item : items) {
            QuizQuestionRefEntity ref = questionRefRepository
                    .findById(item.getQuestionId())
                    .orElseThrow(() -> QuizBusinessException.questionNotFound(item.getQuestionId()));
            if (ref.getStatus() == null || ST_DELETED.equalsIgnoreCase(ref.getStatus())) {
                throw QuizBusinessException.questionNotFound(item.getQuestionId());
            }
            if (!Q_PUBLISHED.equalsIgnoreCase(ref.getStatus())) {
                throw QuizBusinessException.questionNotPublished(item.getQuestionId());
            }
        }

        // Replace: remove existing assignments, then insert the new set (FR-26-23)
        assignmentRepository.deleteByParent(PARENT_ASSESSMENT, assessmentId);
        List<QuizAssignmentEntity> toSave = new ArrayList<>();
        for (AssignQuestionsRequest.AssignmentItem item : items) {
            toSave.add(QuizAssignmentEntity.builder()
                    .parentType(PARENT_ASSESSMENT)
                    .parentId(assessmentId)
                    .questionId(item.getQuestionId())
                    .displayOrder(item.getDisplayOrder())
                    .score(item.getScore())
                    .sectionName(trimToNull(item.getSectionName()))
                    .build());
        }
        assignmentRepository.saveAll(toSave);
        // touch updated_at on the parent quiz
        quiz.setUpdatedAt(java.time.LocalDateTime.now());
        assessmentRepository.save(quiz);

        BigDecimal sum = toSave.stream().map(QuizAssignmentEntity::getScore).reduce(BigDecimal.ZERO, BigDecimal::add);
        boolean matched = scoreMatches(sum, quiz.getTotalScore());
        log.info(
                "[INFO] Staff {} ASSIGN-QUESTIONS assessment {} ({} items)",
                staff.getId(),
                assessmentId,
                toSave.size());

        return AssignResultResponse.builder()
                .assessmentId(assessmentId)
                .assignedCount(toSave.size())
                .assignedScoreSum(sum)
                .totalScore(quiz.getTotalScore())
                .scoreMatched(matched)
                .build();
    }

    // ---------------------------------------------------------------------
    // Submit for review
    // ---------------------------------------------------------------------

    @Transactional
    public QuizSubmitReviewResponse submitForReview(Long assessmentId, String staffEmail) {
        StaffUser staff = resolveStaff(staffEmail);
        QuizAssessmentEntity quiz = requireQuiz(assessmentId);
        guardOwnership(quiz, staff);

        // PA-3: chặn cứng bài rỗng ngay khi gửi duyệt (FR-26-28).
        long count = assignmentRepository.countByParentTypeAndParentId(PARENT_ASSESSMENT, assessmentId);
        if (count == 0) {
            throw QuizBusinessException.emptyQuiz();
        }

        // Khớp tổng điểm (FR-26-26) CỐ Ý mềm ở đây: staff còn sửa dần, `scoreMatched` là cảnh báo;
        // chốt cứng đặt ở bước manager approve (AssessmentContentHandler.approve) — defense-in-depth.

        if (!isEditable(quiz.getStatus())) {
            throw QuizBusinessException.invalidStatusTransition(quiz.getStatus()); // FR-26-27
        }

        quiz.setStatus(ST_PENDING);
        assessmentRepository.save(quiz);
        log.info("[INFO] Staff {} SUBMIT-REVIEW assessment {}", staff.getId(), assessmentId);

        return QuizSubmitReviewResponse.builder()
                .contentId(assessmentId)
                .contentType(PARENT_ASSESSMENT)
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

    private QuizAssessmentEntity requireQuiz(Long id) {
        return assessmentRepository
                .findByIdAndAssessmentTypeAndStatusNot(id, TYPE_QUIZ, ST_DELETED)
                .orElseThrow(() -> QuizBusinessException.assessmentNotFound(id));
    }

    private Lesson requireLesson(Long lessonId) {
        return lessonRepository
                .findByIdAndStatusNot(lessonId, Lesson.LessonStatus.DELETED)
                .orElseThrow(() -> QuizBusinessException.lessonNotFound(lessonId));
    }

    /** FR-26-31: owner-only, unless caller is STAFF_MANAGER. */
    private void guardOwnership(QuizAssessmentEntity quiz, StaffUser staff) {
        if (staff.getStaffRole() == StaffUser.StaffRole.STAFF_MANAGER) {
            return;
        }
        if (quiz.getCreatedBy() == null || !quiz.getCreatedBy().equals(staff.getId())) {
            throw QuizBusinessException.ownershipDenied();
        }
    }

    /** FR-26-30: Staff cannot drive a quiz to published/archived through this use case. */
    private void guardNoPublish(String requestedStatus) {
        if (requestedStatus == null) {
            return;
        }
        String s = requestedStatus.trim().toLowerCase();
        if (ST_PUBLISHED.equals(s) || "archived".equals(s)) {
            throw QuizBusinessException.publishNotAllowed();
        }
    }

    /** FR-26-16/25: editable only while status in {draft, rejected}. */
    private void guardEditableStatus(String status) {
        if (!isEditable(status)) {
            throw QuizBusinessException.invalidStatusTransition(status);
        }
    }

    private boolean isEditable(String status) {
        return ST_DRAFT.equals(status) || ST_REJECTED.equals(status);
    }

    private void validateLevel(String level) {
        if (level == null || !JLPT_LEVELS.contains(level.trim().toUpperCase())) {
            throw QuizBusinessException.invalidJlptLevel(); // FR-26-04
        }
    }

    private void validateLessonOrTopic(Long lessonId, String topic) {
        if (lessonId == null && !StringUtils.hasText(topic)) {
            throw QuizBusinessException.validationFailed("Phải có ít nhất một trong lessonId hoặc topic"); // FR-26-03
        }
    }

    private void validateScoreRange(Integer durationMin, Integer passScore, Integer totalScore) {
        // @Valid already guards non-null + basic min; re-check the cross-field invariant (FR-26-05)
        if (durationMin == null
                || durationMin <= 0
                || totalScore == null
                || totalScore <= 0
                || passScore == null
                || passScore < 0
                || passScore > totalScore) {
            throw QuizBusinessException.validationFailed(
                    "Yêu cầu durationMin>0, totalScore>0, 0<=passScore<=totalScore");
        }
    }

    private boolean scoreMatches(BigDecimal assignedSum, Integer totalScore) {
        BigDecimal sum = assignedSum == null ? BigDecimal.ZERO : assignedSum;
        return totalScore != null && sum.compareTo(BigDecimal.valueOf(totalScore)) == 0;
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    // ---------------------------------------------------------------------
    // Mapping (entity → DTO, ADR-005)
    // ---------------------------------------------------------------------

    private QuizSummaryResponse toSummary(QuizAssessmentEntity quiz) {
        long questionCount = assignmentRepository.countByParentTypeAndParentId(PARENT_ASSESSMENT, quiz.getId());
        return QuizSummaryResponse.builder()
                .assessmentId(quiz.getId())
                .title(quiz.getTitle())
                .assessmentType(quiz.getAssessmentType())
                .jlptLevel(quiz.getJlptLevel())
                .lessonId(quiz.getLessonId())
                .topic(quiz.getTopic())
                .durationMin(quiz.getDurationMin())
                .passScore(quiz.getPassScore())
                .totalScore(quiz.getTotalScore())
                .questionCount(questionCount)
                .status(quiz.getStatus())
                .updatedAt(quiz.getUpdatedAt())
                .build();
    }

    private QuizDetailResponse toDetail(QuizAssessmentEntity quiz, List<QuizAssignmentEntity> assignments) {
        BigDecimal sum =
                assignments.stream().map(QuizAssignmentEntity::getScore).reduce(BigDecimal.ZERO, BigDecimal::add);

        List<QuizDetailResponse.AssignedQuestion> questions = assignments.stream()
                .map(a -> QuizDetailResponse.AssignedQuestion.builder()
                        .assignmentId(a.getId())
                        .questionId(a.getQuestionId())
                        .displayOrder(a.getDisplayOrder())
                        .score(a.getScore())
                        .sectionName(a.getSectionName())
                        .questionText(questionRefRepository
                                .findById(a.getQuestionId())
                                .map(QuizQuestionRefEntity::getQuestionText)
                                .orElse(null))
                        .build())
                .toList();

        return QuizDetailResponse.builder()
                .assessmentId(quiz.getId())
                .assessmentType(quiz.getAssessmentType())
                .title(quiz.getTitle())
                .lessonId(quiz.getLessonId())
                .topic(quiz.getTopic())
                .jlptLevel(quiz.getJlptLevel())
                .durationMin(quiz.getDurationMin())
                .passScore(quiz.getPassScore())
                .totalScore(quiz.getTotalScore())
                .status(quiz.getStatus())
                .assignedScoreSum(sum)
                .scoreMatched(scoreMatches(sum, quiz.getTotalScore()))
                .questions(questions)
                .createdBy(quiz.getCreatedBy())
                .createdAt(quiz.getCreatedAt())
                .updatedAt(quiz.getUpdatedAt())
                .build();
    }
}
