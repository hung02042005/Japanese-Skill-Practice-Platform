/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.staffcontent.question;

import com.jlpt.feature.staff.StaffUser;
import com.jlpt.feature.staff.StaffUserRepository;
import com.jlpt.shared.exception.ForbiddenException;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * UC-24 — Staff question-bank service implementation.
 * All transactional boundaries are at this layer (NFR-24-03).
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class StaffQuestionServiceImpl implements StaffQuestionService {

    private final StaffContentQuestionRepository questionRepository;
    private final StaffContentAttemptAnswerRepository attemptAnswerRepository;
    private final StaffUserRepository staffUserRepository;

    // =====================================================================
    // Create
    // =====================================================================

    @Override
    @Transactional
    public QuestionResponse createQuestion(CreateQuestionRequest request, String staffEmail) {
        StaffUser staff = resolveStaff(staffEmail);

        // Validate type-specific constraints (FR-24-06/07/08)
        validateQuestionFields(
                request.getQuestionType(),
                request.getOptionA(),
                request.getOptionB(),
                request.getOptionC(),
                request.getOptionD(),
                request.getCorrectOption(),
                request.getCorrectAnswerText());

        StaffContentQuestionEntity entity = StaffContentQuestionEntity.builder()
                .questionText(request.getQuestionText().trim())
                .questionType(request.getQuestionType())
                .skill(request.getSkill())
                .jlptLevel(request.getJlptLevel())
                .explanation(trimToNull(request.getExplanation()))
                .audioUrl(trimToNull(request.getAudioUrl()))
                .imageUrl(trimToNull(request.getImageUrl()))
                .optionA(trimToNull(request.getOptionA()))
                .optionB(trimToNull(request.getOptionB()))
                .optionC(trimToNull(request.getOptionC()))
                .optionD(trimToNull(request.getOptionD()))
                .correctOption(trimToNull(request.getCorrectOption()))
                .correctAnswerText(trimToNull(request.getCorrectAnswerText()))
                .status("draft")
                .createdBy(staff.getId())
                .build();

        StaffContentQuestionEntity saved = questionRepository.save(entity);
        log.info("[INFO] Staff {} CREATE question {}", staff.getId(), saved.getId());
        return toResponse(saved, false);
    }

    // =====================================================================
    // List / Search / Filter
    // =====================================================================

    @Override
    @Transactional(readOnly = true)
    public Page<QuestionResponse> listQuestions(
            String q, String skill, String jlptLevel, String questionType, String status, Pageable pageable) {

        // Default: exclude deleted unless explicitly requested (FR-24-13)
        String effectiveStatus = status;
        if (!StringUtils.hasText(effectiveStatus)) {
            effectiveStatus = null; // will be excluded via query logic (no 'deleted' pass-through)
        }

        Page<StaffContentQuestionEntity> page = questionRepository.findFiltered(
                trimToNull(q),
                trimToNull(skill),
                trimToNull(jlptLevel),
                trimToNull(questionType),
                effectiveStatus,
                pageable);

        return page.map(entity -> {
            boolean locked = attemptAnswerRepository.existsByQuestionId(entity.getId());
            return toResponse(entity, locked);
        });
    }

    // =====================================================================
    // Get detail
    // =====================================================================

    @Override
    @Transactional(readOnly = true)
    public QuestionResponse getQuestion(Long questionId) {
        StaffContentQuestionEntity entity = findActive(questionId);
        boolean locked = attemptAnswerRepository.existsByQuestionId(questionId);
        return toResponse(entity, locked);
    }

    // =====================================================================
    // Update
    // =====================================================================

    @Override
    @Transactional
    public QuestionResponse updateQuestion(Long questionId, UpdateQuestionRequest request, String staffEmail) {
        StaffUser staff = resolveStaff(staffEmail);

        StaffContentQuestionEntity entity = findActive(questionId);

        // Ownership check: owner or STAFF_MANAGER (FR-24-24)
        guardOwnership(entity.getCreatedBy(), staff);

        // Lock check (FR-24-17)
        if (attemptAnswerRepository.existsByQuestionId(questionId)) {
            throw StaffQuestionBusinessException.questionLocked();
        }

        // Status check: only draft or rejected (FR-24-18)
        if (!"draft".equals(entity.getStatus()) && !"rejected".equals(entity.getStatus())) {
            throw StaffQuestionBusinessException.invalidStatusTransition();
        }

        // Apply updates (non-null fields)
        if (request.getQuestionText() != null) {
            entity.setQuestionText(request.getQuestionText().trim());
        }
        if (request.getQuestionType() != null) {
            entity.setQuestionType(request.getQuestionType());
        }
        if (request.getSkill() != null) {
            entity.setSkill(request.getSkill());
        }
        if (request.getJlptLevel() != null) {
            entity.setJlptLevel(request.getJlptLevel());
        }
        entity.setExplanation(applyUpdate(entity.getExplanation(), request.getExplanation()));
        entity.setAudioUrl(applyUpdate(entity.getAudioUrl(), request.getAudioUrl()));
        entity.setImageUrl(applyUpdate(entity.getImageUrl(), request.getImageUrl()));
        entity.setOptionA(applyUpdate(entity.getOptionA(), request.getOptionA()));
        entity.setOptionB(applyUpdate(entity.getOptionB(), request.getOptionB()));
        entity.setOptionC(applyUpdate(entity.getOptionC(), request.getOptionC()));
        entity.setOptionD(applyUpdate(entity.getOptionD(), request.getOptionD()));
        entity.setCorrectOption(applyUpdate(entity.getCorrectOption(), request.getCorrectOption()));
        entity.setCorrectAnswerText(applyUpdate(entity.getCorrectAnswerText(), request.getCorrectAnswerText()));

        // Re-validate all field constraints (FR-24-16)
        validateQuestionFields(
                entity.getQuestionType(),
                entity.getOptionA(),
                entity.getOptionB(),
                entity.getOptionC(),
                entity.getOptionD(),
                entity.getCorrectOption(),
                entity.getCorrectAnswerText());

        entity.setUpdatedAt(LocalDateTime.now());
        StaffContentQuestionEntity saved = questionRepository.save(entity);
        log.info("[INFO] Staff {} UPDATE question {}", staff.getId(), saved.getId());
        boolean locked = attemptAnswerRepository.existsByQuestionId(questionId);
        return toResponse(saved, locked);
    }

    // =====================================================================
    // Submit for review
    // =====================================================================

    @Override
    @Transactional
    public StaffQuestionSubmitReviewResponse submitForReview(Long questionId, String staffEmail) {
        StaffUser staff = resolveStaff(staffEmail);

        StaffContentQuestionEntity entity = findActive(questionId);

        // Ownership check (FR-24-24)
        guardOwnership(entity.getCreatedBy(), staff);

        // Status check: only draft or rejected can be submitted (FR-24-20/22)
        if (!"draft".equals(entity.getStatus()) && !"rejected".equals(entity.getStatus())) {
            throw StaffQuestionBusinessException.invalidStatusTransition();
        }

        // Re-run mandatory validation before transition (FR-24-21)
        validateQuestionFields(
                entity.getQuestionType(),
                entity.getOptionA(),
                entity.getOptionB(),
                entity.getOptionC(),
                entity.getOptionD(),
                entity.getCorrectOption(),
                entity.getCorrectAnswerText());

        entity.setStatus("pending_review");
        entity.setUpdatedAt(LocalDateTime.now());
        questionRepository.save(entity);

        log.info("[INFO] Staff {} SUBMIT_REVIEW question {}", staff.getId(), questionId);
        return StaffQuestionSubmitReviewResponse.builder()
                .contentId(questionId)
                .contentType("question")
                .status("pending_review")
                .build();
    }

    // =====================================================================
    // Internal helpers
    // =====================================================================

    private StaffUser resolveStaff(String email) {
        return staffUserRepository
                .findByEmail(email)
                .orElseThrow(() -> new ForbiddenException("Không có quyền thao tác câu hỏi này"));
    }

    private StaffContentQuestionEntity findActive(Long id) {
        return questionRepository
                .findByIdAndStatusNot(id, "deleted")
                .orElseThrow(() -> StaffQuestionBusinessException.questionNotFound(id));
    }

    /**
     * Ownership guard: owner or STAFF_MANAGER can operate (FR-24-24).
     */
    private void guardOwnership(Long questionOwnerId, StaffUser current) {
        if (current.getStaffRole() == StaffUser.StaffRole.STAFF_MANAGER) {
            return;
        }
        if (questionOwnerId == null || !questionOwnerId.equals(current.getId())) {
            throw StaffQuestionBusinessException.forbidden("Không có quyền thao tác câu hỏi này");
        }
    }

    /**
     * Validate type-specific mandatory fields (FR-24-06/07/08).
     */
    private void validateQuestionFields(
            String questionType,
            String optionA,
            String optionB,
            String optionC,
            String optionD,
            String correctOption,
            String correctAnswerText) {
        if ("multiple_choice".equals(questionType)) {
            if (!StringUtils.hasText(optionA)
                    || !StringUtils.hasText(optionB)
                    || !StringUtils.hasText(optionC)
                    || !StringUtils.hasText(optionD)
                    || !StringUtils.hasText(correctOption)) {
                throw StaffQuestionBusinessException.missingOptions();
            }
            if (!correctOption.matches("^[ABCD]$")) {
                throw StaffQuestionBusinessException.invalidCorrectOption();
            }
        } else if ("true_false".equals(questionType)) {
            if (!StringUtils.hasText(correctAnswerText)
                    || !"true".equalsIgnoreCase(correctAnswerText.trim())
                            && !"false".equalsIgnoreCase(correctAnswerText.trim())) {
                throw StaffQuestionBusinessException.missingOptions();
            }
        } else if ("fill_blank".equals(questionType)) {
            if (!StringUtils.hasText(correctAnswerText)) {
                throw StaffQuestionBusinessException.missingOptions();
            }
        }
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    /**
     * Apply an optional update field: if newValue is null keep the old value,
     * otherwise trim and set (empty string → null).
     */
    private String applyUpdate(String oldValue, String newValue) {
        if (newValue == null) {
            return oldValue;
        }
        String trimmed = newValue.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    // =====================================================================
    // Entity → DTO mapping (ADR-005)
    // =====================================================================

    private QuestionResponse toResponse(StaffContentQuestionEntity entity, boolean locked) {
        return QuestionResponse.builder()
                .questionId(entity.getId())
                .questionText(entity.getQuestionText())
                .questionType(entity.getQuestionType())
                .skill(entity.getSkill())
                .jlptLevel(entity.getJlptLevel())
                .explanation(entity.getExplanation())
                .audioUrl(entity.getAudioUrl())
                .imageUrl(entity.getImageUrl())
                .optionA(entity.getOptionA())
                .optionB(entity.getOptionB())
                .optionC(entity.getOptionC())
                .optionD(entity.getOptionD())
                .correctOption(entity.getCorrectOption())
                .correctAnswerText(entity.getCorrectAnswerText())
                .status(entity.getStatus())
                .isLocked(locked)
                .createdBy(entity.getCreatedBy())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
