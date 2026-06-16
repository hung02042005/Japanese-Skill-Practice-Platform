/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.learning.grammar;

import com.jlpt.feature.learning.GrammarPoint;
import com.jlpt.feature.learning.Kanji;
import com.jlpt.feature.learning.exception.LearningException;
import com.jlpt.feature.learning.grammar.dto.response.GrammarDetailResponse;
import com.jlpt.feature.learning.grammar.dto.response.GrammarSummaryResponse;
import com.jlpt.feature.student.StudentContentProgress;
import com.jlpt.feature.student.StudentContentProgressRepository;
import com.jlpt.feature.student.StudentUser;
import com.jlpt.feature.student.StudentUserRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** UC-06 — Học Ngữ Pháp: danh sách/chi tiết theo level (chỉ published) + cờ tiến độ. */
@Slf4j
@Service
@RequiredArgsConstructor
public class GrammarService {

    private final GrammarRepository grammarRepository;
    private final StudentContentProgressRepository progressRepository;
    private final StudentUserRepository studentUserRepository;

    @Transactional(readOnly = true)
    public Page<GrammarSummaryResponse> listGrammar(String level, Long studentId, Pageable pageable) {
        StudentUser.JlptLevel jlptLevel = parseLevel(level);

        Page<GrammarPoint> page =
                grammarRepository.findByJlptLevelAndStatus(jlptLevel, Kanji.ContentStatus.PUBLISHED, pageable);

        List<Long> grammarIds =
                page.getContent().stream().map(GrammarPoint::getId).toList();
        Map<Long, StudentContentProgress> progressByGrammarId = progressRepository
                .findByStudent_IdAndContentTypeAndContentIdIn(
                        studentId, StudentContentProgress.ContentType.GRAMMAR, grammarIds)
                .stream()
                .collect(Collectors.toMap(StudentContentProgress::getContentId, Function.identity()));

        return page.map(grammar -> toSummary(grammar, progressByGrammarId.get(grammar.getId())));
    }

    @Transactional
    public GrammarDetailResponse getGrammarDetail(Long grammarId, Long studentId) {
        GrammarPoint grammar = grammarRepository
                .findByIdAndStatus(grammarId, Kanji.ContentStatus.PUBLISHED)
                .orElseThrow(() -> {
                    log.warn(
                            "[GrammarService] Truy cập nội dung không tồn tại {studentId={}, grammarId={}}",
                            studentId,
                            grammarId);
                    return LearningException.contentNotFound();
                });

        StudentContentProgress progress = progressRepository
                .findByStudent_IdAndContentTypeAndContentId(
                        studentId, StudentContentProgress.ContentType.GRAMMAR, grammarId)
                .orElse(null);

        log.info(
                "[GrammarService] Truy cập nội dung {studentId={}, contentType=grammar, contentId={}}",
                studentId,
                grammarId);
        touchLastActivity(studentId);

        return toDetail(grammar, progress);
    }

    private void touchLastActivity(Long studentId) {
        studentUserRepository.findById(studentId).ifPresent(student -> {
            student.setLastActivityDate(LocalDate.now());
            studentUserRepository.save(student);
        });
    }

    private StudentUser.JlptLevel parseLevel(String level) {
        try {
            return StudentUser.JlptLevel.valueOf(level);
        } catch (IllegalArgumentException | NullPointerException ex) {
            throw LearningException.levelMismatch();
        }
    }

    private GrammarSummaryResponse toSummary(GrammarPoint grammar, StudentContentProgress progress) {
        return GrammarSummaryResponse.builder()
                .grammarId(grammar.getId())
                .structure(grammar.getStructure())
                .formula(grammar.getFormula())
                .meaning(grammar.getMeaning())
                .jlptLevel(grammar.getJlptLevel().name())
                .isCompleted(
                        progress != null && progress.getStatus() == StudentContentProgress.ProgressStatus.COMPLETED)
                .build();
    }

    private GrammarDetailResponse toDetail(GrammarPoint grammar, StudentContentProgress progress) {
        return GrammarDetailResponse.builder()
                .grammarId(grammar.getId())
                .structure(grammar.getStructure())
                .formula(grammar.getFormula())
                .meaning(grammar.getMeaning())
                .usageExplanation(grammar.getUsageExplanation())
                .jlptLevel(grammar.getJlptLevel().name())
                .exampleSentenceJp(grammar.getExampleSentenceJp())
                .exampleSentenceVi(grammar.getExampleSentenceVi())
                .progressStatus(progress != null ? progress.getStatus().getValue() : null)
                .build();
    }
}
