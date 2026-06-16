/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.learning.kanji;

import com.jlpt.feature.learning.Kanji;
import com.jlpt.feature.learning.exception.LearningException;
import com.jlpt.feature.learning.kanji.dto.response.KanjiDetailResponse;
import com.jlpt.feature.learning.kanji.dto.response.KanjiProgressSummaryResponse;
import com.jlpt.feature.learning.kanji.dto.response.KanjiSummaryResponse;
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

/** UC-07 — Học Kanji: danh sách/chi tiết theo level (chỉ published) + cờ tiến độ. */
@Slf4j
@Service
@RequiredArgsConstructor
public class KanjiService {

    private final KanjiRepository kanjiRepository;
    private final StudentContentProgressRepository progressRepository;
    private final StudentUserRepository studentUserRepository;

    @Transactional(readOnly = true)
    public Page<KanjiSummaryResponse> listKanji(String level, Long studentId, Pageable pageable) {
        StudentUser.JlptLevel jlptLevel = parseLevel(level);

        Page<Kanji> page = kanjiRepository.findByJlptLevelAndStatus(jlptLevel, Kanji.ContentStatus.PUBLISHED, pageable);

        List<Long> kanjiIds = page.getContent().stream().map(Kanji::getId).toList();
        Map<Long, StudentContentProgress> progressByKanjiId = progressRepository
                .findByStudent_IdAndContentTypeAndContentIdIn(
                        studentId, StudentContentProgress.ContentType.KANJI, kanjiIds)
                .stream()
                .collect(Collectors.toMap(StudentContentProgress::getContentId, Function.identity()));

        return page.map(kanji -> toSummary(kanji, progressByKanjiId.get(kanji.getId())));
    }

    @Transactional(readOnly = true)
    public KanjiProgressSummaryResponse getProgressSummary(String level, Long studentId) {
        StudentUser.JlptLevel jlptLevel = parseLevel(level);

        List<Long> kanjiIds = kanjiRepository.findIdsByJlptLevelAndStatus(jlptLevel, Kanji.ContentStatus.PUBLISHED);
        long completed = kanjiIds.isEmpty()
                ? 0
                : progressRepository.countByStudent_IdAndContentTypeAndContentIdInAndStatus(
                        studentId,
                        StudentContentProgress.ContentType.KANJI,
                        kanjiIds,
                        StudentContentProgress.ProgressStatus.COMPLETED);

        return KanjiProgressSummaryResponse.builder()
                .jlptLevel(jlptLevel.name())
                .completed(completed)
                .total(kanjiIds.size())
                .build();
    }

    @Transactional
    public KanjiDetailResponse getKanjiDetail(Long kanjiId, Long studentId) {
        Kanji kanji = kanjiRepository
                .findByIdAndStatus(kanjiId, Kanji.ContentStatus.PUBLISHED)
                .orElseThrow(() -> {
                    log.warn(
                            "[KanjiService] Truy cập nội dung không tồn tại {studentId={}, kanjiId={}}",
                            studentId,
                            kanjiId);
                    return LearningException.contentNotFound();
                });

        StudentContentProgress progress = progressRepository
                .findByStudent_IdAndContentTypeAndContentId(studentId, StudentContentProgress.ContentType.KANJI, kanjiId)
                .orElse(null);

        log.info(
                "[KanjiService] Truy cập nội dung {studentId={}, contentType=kanji, contentId={}}",
                studentId,
                kanjiId);
        touchLastActivity(studentId);

        Long prevKanjiId = kanjiRepository
                .findFirstByJlptLevelAndStatusAndIdLessThanOrderByIdDesc(
                        kanji.getJlptLevel(), Kanji.ContentStatus.PUBLISHED, kanji.getId())
                .map(Kanji::getId)
                .orElse(null);
        Long nextKanjiId = kanjiRepository
                .findFirstByJlptLevelAndStatusAndIdGreaterThanOrderByIdAsc(
                        kanji.getJlptLevel(), Kanji.ContentStatus.PUBLISHED, kanji.getId())
                .map(Kanji::getId)
                .orElse(null);

        return toDetail(kanji, progress, prevKanjiId, nextKanjiId);
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

    private KanjiSummaryResponse toSummary(Kanji kanji, StudentContentProgress progress) {
        return KanjiSummaryResponse.builder()
                .kanjiId(kanji.getId())
                .characterValue(kanji.getCharacterValue())
                .meaning(kanji.getMeaning())
                .strokeCount(kanji.getStrokeCount())
                .jlptLevel(kanji.getJlptLevel().name())
                .isCompleted(
                        progress != null && progress.getStatus() == StudentContentProgress.ProgressStatus.COMPLETED)
                .build();
    }

    private KanjiDetailResponse toDetail(
            Kanji kanji, StudentContentProgress progress, Long prevKanjiId, Long nextKanjiId) {
        return KanjiDetailResponse.builder()
                .kanjiId(kanji.getId())
                .characterValue(kanji.getCharacterValue())
                .strokeCount(kanji.getStrokeCount())
                .strokeOrderUrl(kanji.getStrokeOrderUrl())
                .onyomi(kanji.getOnyomi())
                .kunyomi(kanji.getKunyomi())
                .meaning(kanji.getMeaning())
                .exampleWord(kanji.getExampleWord())
                .exampleReading(kanji.getExampleReading())
                .exampleMeaning(kanji.getExampleMeaning())
                .jlptLevel(kanji.getJlptLevel().name())
                .progressStatus(progress != null ? progress.getStatus().getValue() : null)
                .prevKanjiId(prevKanjiId)
                .nextKanjiId(nextKanjiId)
                .build();
    }
}
