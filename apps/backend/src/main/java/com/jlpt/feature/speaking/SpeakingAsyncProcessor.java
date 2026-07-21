/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.speaking;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jlpt.feature.assessment.StudentSubmission;
import com.jlpt.feature.assessment.StudentSubmissionRepository;
import com.jlpt.feature.speaking.dto.SpeakingAiDetail;
import com.jlpt.feature.speaking.dto.WordResultDto;
import com.jlpt.feature.speaking.engine.SpeechAnalysis;
import com.jlpt.feature.speaking.engine.SpeechRecognitionEngine;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Xử lý chấm điểm AI bất đồng bộ cho một bài speaking (UC-13 §3.1 bước 12-13, §3.3 fallback).
 *
 * <p>Bean riêng để {@code @Async} + gọi lại tầng persistence hoạt động qua proxy (không tự-gọi).
 * Chịu trách nhiệm: retry (BR-13-03), clamp điểm (BR-13-05), fallback không lộ raw error
 * (BR-13-04), và log đầy đủ mỗi lần thử (BR-13-07 / LESSON-006).
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SpeakingAsyncProcessor {

    private static final int MAX_ATTEMPTS = 3;
    private static final long[] BACKOFF_MS = {1000L, 2000L, 4000L};
    private static final String FALLBACK_MESSAGE = "Không thể xử lý bài nộp lúc này. Vui lòng thử lại sau.";

    private final SpeechRecognitionEngine engine;
    private final StudentSubmissionRepository submissionRepository;
    private final ObjectMapper objectMapper;

    /**
     * Chấm điểm bài nộp trong luồng nền. targetText được truyền vào thay vì đọc lazy từ entity để
     * tránh LazyInitializationException ngoài transaction gốc.
     */
    @Async
    public void process(Long submissionId, String targetText, Path audioPath) {
        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
            long startedAt = System.currentTimeMillis();
            try {
                SpeechAnalysis analysis = engine.analyze(audioPath, targetText);
                saveSuccess(submissionId, analysis);
                log.info(
                        "[Speaking] submissionId={} engine={} attempt={} status=OK durationMs={} score={}",
                        submissionId,
                        engine.engineName(),
                        attempt,
                        System.currentTimeMillis() - startedAt,
                        analysis.overallScore());
                return;
            } catch (Exception ex) {
                log.warn(
                        "[Speaking] submissionId={} engine={} attempt={} status=FAIL durationMs={} error={}",
                        submissionId,
                        engine.engineName(),
                        attempt,
                        System.currentTimeMillis() - startedAt,
                        ex.getMessage());
                if (attempt < MAX_ATTEMPTS) {
                    sleep(BACKOFF_MS[attempt - 1]);
                }
            }
        }

        log.error(
                "[Speaking] submissionId={} engine={} — thất bại sau {} lần thử, dùng fallback",
                submissionId,
                engine.engineName(),
                MAX_ATTEMPTS);
        saveFailure(submissionId);
    }

    // save() của Spring Data tự chạy trong transaction riêng và merge entity detached → cập nhật đủ
    // các cột scalar. Không cần @Transactional bao ngoài (và self-invocation cũng làm nó vô hiệu).
    private void saveSuccess(Long submissionId, SpeechAnalysis analysis) {
        StudentSubmission submission =
                submissionRepository.findById(submissionId).orElse(null);
        if (submission == null) {
            log.error("[Speaking] submissionId={} biến mất trước khi lưu kết quả AI", submissionId);
            return;
        }
        submission.setAiOverallScore(BigDecimal.valueOf(clamp(analysis.overallScore())));
        submission.setAiPronunciationScore(BigDecimal.valueOf(clamp(analysis.pronunciationScore())));
        submission.setAiFluencyScore(BigDecimal.valueOf(clamp(analysis.fluencyScore())));
        submission.setAiSuggestions(analysis.suggestions());
        submission.setAiErrorSummary(serializeDetail(analysis));
        submission.setStatus(StudentSubmission.SubmissionStatus.AI_GRADED);
        submission.setAiGradedAt(LocalDateTime.now());
        submissionRepository.save(submission);
    }

    private void saveFailure(Long submissionId) {
        StudentSubmission submission =
                submissionRepository.findById(submissionId).orElse(null);
        if (submission == null) {
            return;
        }
        // status = AI_GRADED nhưng điểm null → tầng đọc suy ra FAILED, hiển thị message thân thiện.
        submission.setAiOverallScore(null);
        submission.setAiSuggestions(FALLBACK_MESSAGE);
        submission.setStatus(StudentSubmission.SubmissionStatus.AI_GRADED);
        submission.setAiGradedAt(LocalDateTime.now());
        submissionRepository.save(submission);
    }

    private String serializeDetail(SpeechAnalysis analysis) {
        List<WordResultDto> words = analysis.wordResults().stream()
                .map(w -> WordResultDto.builder()
                        .word(w.word())
                        .correct(w.correct())
                        .feedback(w.feedback())
                        .build())
                .toList();
        try {
            return objectMapper.writeValueAsString(new SpeakingAiDetail(analysis.transcript(), words));
        } catch (JsonProcessingException e) {
            log.warn("[Speaking] Không serialize được chi tiết AI: {}", e.getMessage());
            return null;
        }
    }

    private int clamp(int v) {
        return Math.max(0, Math.min(100, v));
    }

    private void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
