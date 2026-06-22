/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.student.grammar;

import com.jlpt.feature.learning.GrammarPoint;
import com.jlpt.feature.learning.Kanji.ContentStatus;
import com.jlpt.feature.student.StudentContentProgress;
import com.jlpt.feature.student.StudentUser;
import com.jlpt.feature.student.StudentUser.JlptLevel;
import com.jlpt.feature.student.StudentUserRepository;
import com.jlpt.feature.student.exception.StudentLearningException;
import com.jlpt.feature.student.grammar.dto.GrammarDetailResponse;
import com.jlpt.feature.student.grammar.dto.GrammarListResponse;
import com.jlpt.feature.student.grammar.dto.GrammarSummaryResponse;
import com.jlpt.feature.student.progress.StudentContentProgressRepository;
import java.time.LocalDate;
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

/**
 * UC-06 — Service implementation for Student grammar operations.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class StudentGrammarServiceImpl implements StudentGrammarService {

    private final StudentGrammarRepository grammarRepository;
    private final StudentUserRepository studentUserRepository;
    private final StudentContentProgressRepository progressRepository;

    @Override
    @Transactional(readOnly = true)
    public GrammarListResponse getGrammarList(String levelStr, Long studentId, int page, int size) {
        // Resolve student
        studentUserRepository.findById(studentId)
                .orElseThrow(StudentLearningException::contentNotFound);

        // Validate level
        JlptLevel jlptLevel;
        try {
            jlptLevel = JlptLevel.valueOf(levelStr.toUpperCase());
        } catch (Exception e) {
            throw StudentLearningException.levelMismatch();
        }

        // Clamp parameters
        int effectivePage = Math.max(0, page);
        int effectiveSize = Math.max(1, Math.min(50, size));

        Pageable pageable = PageRequest.of(effectivePage, effectiveSize);
        Page<GrammarPoint> grammarPage = grammarRepository.findByJlptLevelAndStatus(
                jlptLevel, ContentStatus.PUBLISHED, pageable);

        // Fetch student progress for completed check
        List<Long> grammarIds = grammarPage.getContent().stream()
                .map(GrammarPoint::getId)
                .toList();

        Set<Long> completedIds = Set.of();
        if (!grammarIds.isEmpty()) {
            List<StudentContentProgress> progressList = progressRepository
                    .findByStudentIdAndContentTypeAndContentIdIn(
                            studentId, StudentContentProgress.ContentType.GRAMMAR, grammarIds);
            completedIds = progressList.stream()
                    .filter(p -> p.getStatus() == StudentContentProgress.ProgressStatus.COMPLETED)
                    .map(StudentContentProgress::getContentId)
                    .collect(Collectors.toSet());
        }

        final Set<Long> finalCompletedIds = completedIds;
        List<GrammarSummaryResponse> summaries = grammarPage.getContent().stream()
                .map(g -> GrammarSummaryResponse.builder()
                        .grammarId(g.getId())
                        .title(g.getTitle())
                        .structure(g.getStructure())
                        .meaning(g.getMeaning())
                        .jlptLevel(g.getJlptLevel().name())
                        .isCompleted(finalCompletedIds.contains(g.getId()))
                        .build())
                .toList();

        return GrammarListResponse.builder()
                .content(summaries)
                .totalElements(grammarPage.getTotalElements())
                .totalPages(grammarPage.getTotalPages())
                .page(effectivePage)
                .size(effectiveSize)
                .build();
    }

    @Override
    @Transactional
    public GrammarDetailResponse getGrammarDetail(Long grammarId, Long studentId) {
        // Resolve student
        StudentUser student = studentUserRepository.findById(studentId)
                .orElseThrow(StudentLearningException::contentNotFound);

        // Resolve grammar
        GrammarPoint grammar = grammarRepository.findByIdAndStatus(grammarId, ContentStatus.PUBLISHED)
                .orElseThrow(() -> {
                    log.warn("[WARN] [GrammarService] Truy cập nội dung không tồn tại {studentId={}, grammarId={}}", studentId, grammarId);
                    return StudentLearningException.contentNotFound();
                });

        // Mock VIP Check Logic:
        // A grammar point is VIP if id % 5 == 0 OR title contains "[VIP]"
        boolean isVipOnly = (grammar.getId() != null && grammar.getId() % 5 == 0)
                || (grammar.getTitle() != null && grammar.getTitle().contains("[VIP]"));

        // A student is VIP if email contains "vip"
        boolean isStudentVip = student.getEmail() != null && student.getEmail().toLowerCase().contains("vip");

        if (isVipOnly && !isStudentVip) {
            throw StudentLearningException.vipRequired();
        }

        // Ghi log access
        log.info("[ACCESS LOG] studentId={}, contentType='grammar', contentId={}", studentId, grammarId);

        // Cập nhật last_activity_date và tính streak
        updateStudentStreak(student);
        studentUserRepository.save(student);

        // Get student progress status if present
        StudentContentProgress progress = progressRepository
                .findByStudentIdAndContentTypeAndContentId(
                        studentId, StudentContentProgress.ContentType.GRAMMAR, grammarId)
                .orElse(null);

        String progressStatus = (progress != null) ? progress.getStatus().name().toLowerCase() : null;

        return GrammarDetailResponse.builder()
                .grammarId(grammar.getId())
                .title(grammar.getTitle())
                .structure(grammar.getStructure())
                .formula(grammar.getFormula())
                .meaning(grammar.getMeaning())
                .usageExplanation(grammar.getUsageExplanation())
                .jlptLevel(grammar.getJlptLevel().name())
                .exampleSentenceJp(grammar.getExampleSentenceJp())
                .exampleSentenceVi(grammar.getExampleSentenceVi())
                .progressStatus(progressStatus)
                .build();
    }

    private void updateStudentStreak(StudentUser student) {
        LocalDate today = LocalDate.now();
        LocalDate lastActive = student.getLastActivityDate();

        if (lastActive == null) {
            student.setCurrentStreak(1);
            student.setLongestStreak(Math.max(student.getLongestStreak(), 1));
        } else if (lastActive.equals(today.minusDays(1))) {
            int newStreak = student.getCurrentStreak() + 1;
            student.setCurrentStreak(newStreak);
            student.setLongestStreak(Math.max(student.getLongestStreak(), newStreak));
        } else if (!lastActive.equals(today)) {
            // More than 1 day missed, reset streak to 1
            student.setCurrentStreak(1);
            student.setLongestStreak(Math.max(student.getLongestStreak(), 1));
        }
        // If lastActive is today, keep the current streak as is.
        student.setLastActivityDate(today);
    }
}
