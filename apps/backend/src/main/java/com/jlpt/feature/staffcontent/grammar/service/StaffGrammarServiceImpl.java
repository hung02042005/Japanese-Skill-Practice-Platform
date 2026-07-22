/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.staffcontent.grammar.service;

import com.jlpt.feature.learning.GrammarPoint;
import com.jlpt.feature.learning.Kanji.ContentStatus;
import com.jlpt.feature.learning.Lesson;
import com.jlpt.feature.learning.LessonRepository;
import com.jlpt.feature.staff.StaffUser;
import com.jlpt.feature.staff.StaffUserRepository;
import com.jlpt.feature.staffcontent.grammar.dto.*;
import com.jlpt.feature.staffcontent.grammar.exception.GrammarBusinessException;
import com.jlpt.feature.staffcontent.grammar.repository.StaffGrammarRepository;
import com.jlpt.feature.student.StudentUser.JlptLevel;
import com.jlpt.shared.exception.ForbiddenException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * UC-25 — Service implementation for Staff grammar operations.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class StaffGrammarServiceImpl implements StaffGrammarService {

    private final StaffGrammarRepository grammarRepository;
    private final StaffUserRepository staffUserRepository;
    private final LessonRepository lessonRepository;

    @Override
    @Transactional
    public GrammarDetailResponse createGrammar(CreateGrammarRequest request, String staffEmail) {
        StaffUser staff = resolveStaff(staffEmail);

        JlptLevel level;
        try {
            level = JlptLevel.valueOf(request.getJlptLevel());
        } catch (IllegalArgumentException e) {
            throw GrammarBusinessException.invalidJlptLevel();
        }

        GrammarPoint grammar = GrammarPoint.builder()
                .title(request.getTitle())
                .structure(request.getStructure().trim())
                .formula(trimToNull(request.getFormula()))
                .meaning(request.getMeaning().trim())
                .usageExplanation(request.getUsageExplanation().trim())
                .jlptLevel(level)
                .exampleSentenceJp(request.getExampleSentenceJp().trim())
                .exampleSentenceVi(trimToNull(request.getExampleSentenceVi()))
                .status(ContentStatus.DRAFT) // FR-01: always draft
                .createdBy(staff) // FR-02: system sets creator
                .build();

        if (request.getLessonId() != null) {
            Lesson lesson = resolveLesson(request.getLessonId());
            if (lesson.getJlptLevel() != level) {
                throw GrammarBusinessException.levelMismatch();
            }
            grammar.setLesson(lesson);
        }

        GrammarPoint saved = grammarRepository.save(grammar);
        log.info("[INFO] Staff {} CREATED grammar {}", staff.getId(), saved.getId());

        return toDetailResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<GrammarSummaryResponse> listGrammars(
            String jlptLevelStr, String statusStr, Pageable pageable, String staffEmail) {
        StaffUser staff = resolveStaff(staffEmail);

        JlptLevel jlptLevel = null;
        if (StringUtils.hasText(jlptLevelStr)) {
            try {
                jlptLevel = JlptLevel.valueOf(jlptLevelStr);
            } catch (Exception ignored) {
            }
        }

        ContentStatus status = null;
        if (StringUtils.hasText(statusStr)) {
            try {
                status = ContentStatus.valueOf(statusStr.toUpperCase());
            } catch (Exception ignored) {
            }
        }

        Page<GrammarPoint> page = grammarRepository.findByCreatedByWithFilters(
                staff.getId(), jlptLevel, status, ContentStatus.DELETED, pageable);

        return page.map(this::toSummaryResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public GrammarDetailResponse getGrammar(Long grammarId, String staffEmail) {
        StaffUser staff = resolveStaff(staffEmail);
        GrammarPoint grammar = grammarRepository
                .findActiveByIdWithLesson(grammarId, ContentStatus.DELETED)
                .orElseThrow(() -> GrammarBusinessException.grammarNotFound(grammarId));

        guardOwnershipOrManager(grammar, staff);

        return toDetailResponse(grammar);
    }

    @Override
    @Transactional
    public GrammarDetailResponse updateGrammar(Long grammarId, UpdateGrammarRequest request, String staffEmail) {
        StaffUser staff = resolveStaff(staffEmail);
        GrammarPoint grammar = grammarRepository
                .findActiveByIdWithLesson(grammarId, ContentStatus.DELETED)
                .orElseThrow(() -> GrammarBusinessException.grammarNotFound(grammarId));

        guardOwnershipOrManager(grammar, staff);

        if (grammar.getStatus() == ContentStatus.PUBLISHED) {
            throw GrammarBusinessException.editNotAllowedPublished();
        }
        if (grammar.getStatus() != ContentStatus.DRAFT && grammar.getStatus() != ContentStatus.REJECTED) {
            throw GrammarBusinessException.editNotAllowedCurrentStatus(
                    grammar.getStatus().getValue());
        }

        if (request.getTitle() != null) grammar.setTitle(request.getTitle());
        if (request.getStructure() != null)
            grammar.setStructure(request.getStructure().trim());
        if (request.getFormula() != null) grammar.setFormula(trimToNull(request.getFormula()));
        if (request.getMeaning() != null)
            grammar.setMeaning(request.getMeaning().trim());
        if (request.getUsageExplanation() != null)
            grammar.setUsageExplanation(request.getUsageExplanation().trim());
        if (request.getExampleSentenceJp() != null)
            grammar.setExampleSentenceJp(request.getExampleSentenceJp().trim());
        if (request.getExampleSentenceVi() != null)
            grammar.setExampleSentenceVi(trimToNull(request.getExampleSentenceVi()));

        if (request.getJlptLevel() != null) {
            try {
                grammar.setJlptLevel(JlptLevel.valueOf(request.getJlptLevel()));
            } catch (IllegalArgumentException e) {
                throw GrammarBusinessException.invalidJlptLevel();
            }
        }

        if (request.isClearLesson()) {
            grammar.setLesson(null);
        } else if (request.getLessonId() != null) {
            Lesson lesson = resolveLesson(request.getLessonId());
            if (lesson.getJlptLevel() != grammar.getJlptLevel()) {
                throw GrammarBusinessException.levelMismatch();
            }
            grammar.setLesson(lesson);
        } else if (grammar.getLesson() != null) {
            // Validate existing lesson against new level if changed
            if (grammar.getLesson().getJlptLevel() != grammar.getJlptLevel()) {
                throw GrammarBusinessException.levelMismatch();
            }
        }

        GrammarPoint saved = grammarRepository.save(grammar);
        log.info("[INFO] Staff {} UPDATED grammar {}", staff.getId(), saved.getId());

        return toDetailResponse(saved);
    }

    @Override
    @Transactional
    public GrammarSubmitReviewResponse submitForReview(Long grammarId, String staffEmail) {
        StaffUser staff = resolveStaff(staffEmail);
        GrammarPoint grammar = grammarRepository
                .findByIdAndStatusNot(grammarId, ContentStatus.DELETED)
                .orElseThrow(() -> GrammarBusinessException.grammarNotFound(grammarId));

        guardOwnershipOrManager(grammar, staff);

        if (grammar.getStatus() != ContentStatus.DRAFT && grammar.getStatus() != ContentStatus.REJECTED) {
            throw GrammarBusinessException.submitInvalidStatus(
                    grammar.getStatus().getValue());
        }

        // FR-20: Guard mandatory fields before submitting
        if (!StringUtils.hasText(grammar.getStructure())) throw GrammarBusinessException.submitIncomplete("structure");
        if (!StringUtils.hasText(grammar.getMeaning())) throw GrammarBusinessException.submitIncomplete("meaning");
        if (!StringUtils.hasText(grammar.getUsageExplanation()))
            throw GrammarBusinessException.submitIncomplete("usageExplanation");
        if (!StringUtils.hasText(grammar.getExampleSentenceJp()))
            throw GrammarBusinessException.submitIncomplete("exampleSentenceJp");
        if (grammar.getJlptLevel() == null) throw GrammarBusinessException.submitIncomplete("jlptLevel");

        grammar.setStatus(ContentStatus.PENDING_REVIEW);
        grammarRepository.save(grammar);

        log.info("[INFO] Staff {} SUBMITTED grammar {} for review", staff.getId(), grammar.getId());

        return GrammarSubmitReviewResponse.builder()
                .contentId(grammar.getId())
                .contentType("grammar")
                .status(ContentStatus.PENDING_REVIEW.getValue())
                .build();
    }

    private StaffUser resolveStaff(String email) {
        return staffUserRepository
                .findByEmail(email)
                .orElseThrow(() -> new ForbiddenException("Tài khoản nhân viên không tồn tại"));
    }

    private Lesson resolveLesson(Long lessonId) {
        return lessonRepository
                .findByIdAndStatusNot(lessonId, Lesson.LessonStatus.DELETED)
                .orElseThrow(() -> GrammarBusinessException.lessonNotFound(lessonId));
    }

    private void guardOwnershipOrManager(GrammarPoint grammar, StaffUser staff) {
        if (staff.getStaffRole() == StaffUser.StaffRole.STAFF_MANAGER) {
            return;
        }
        if (grammar.getCreatedBy() == null || !grammar.getCreatedBy().getId().equals(staff.getId())) {
            throw GrammarBusinessException.ownershipDenied();
        }
    }

    private String trimToNull(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private GrammarSummaryResponse toSummaryResponse(GrammarPoint entity) {
        return GrammarSummaryResponse.builder()
                .grammarId(entity.getId())
                .title(entity.getTitle())
                .structure(entity.getStructure())
                .meaning(entity.getMeaning())
                .jlptLevel(entity.getJlptLevel().name())
                .status(entity.getStatus().getValue())
                .createdBy(entity.getCreatedBy() != null ? entity.getCreatedBy().getId() : null)
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    private GrammarDetailResponse toDetailResponse(GrammarPoint entity) {
        GrammarDetailResponse.LessonRef lessonRef = null;
        if (entity.getLesson() != null) {
            lessonRef = GrammarDetailResponse.LessonRef.builder()
                    .lessonId(entity.getLesson().getId())
                    .title(entity.getLesson().getTitle())
                    .jlptLevel(entity.getLesson().getJlptLevel().name())
                    .build();
        }

        return GrammarDetailResponse.builder()
                .grammarId(entity.getId())
                .title(entity.getTitle())
                .structure(entity.getStructure())
                .formula(entity.getFormula())
                .meaning(entity.getMeaning())
                .usageExplanation(entity.getUsageExplanation())
                .jlptLevel(entity.getJlptLevel().name())
                .exampleSentenceJp(entity.getExampleSentenceJp())
                .exampleSentenceVi(entity.getExampleSentenceVi())
                .status(entity.getStatus().getValue())
                .createdBy(entity.getCreatedBy() != null ? entity.getCreatedBy().getId() : null)
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .lesson(lessonRef)
                .build();
    }
}
