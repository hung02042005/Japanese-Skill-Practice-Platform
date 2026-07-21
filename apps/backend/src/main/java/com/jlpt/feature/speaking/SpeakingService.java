/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.speaking;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jlpt.feature.assessment.StudentSubmission;
import com.jlpt.feature.assessment.StudentSubmissionRepository;
import com.jlpt.feature.learning.Lesson;
import com.jlpt.feature.learning.LessonRepository;
import com.jlpt.feature.speaking.dto.SpeakingAiDetail;
import com.jlpt.feature.speaking.dto.SpeakingExerciseResponse;
import com.jlpt.feature.speaking.dto.SpeakingResultResponse;
import com.jlpt.feature.speaking.dto.SpeakingSubmitResponse;
import com.jlpt.feature.student.StudentUser;
import com.jlpt.shared.exception.BadRequestException;
import com.jlpt.shared.exception.ResourceNotFoundException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/** Nghiệp vụ luyện nói + chấm điểm AI async (UC-13). */
@Service
@RequiredArgsConstructor
@Slf4j
public class SpeakingService {

    private static final String CATEGORY_LABEL = "Shadowing";
    private static final String REJECTED_MESSAGE = "Bài nộp không hợp lệ. Vui lòng ghi âm và thử lại.";

    private final LessonRepository lessonRepository;
    private final StudentSubmissionRepository submissionRepository;
    private final SpeakingAudioStorageService audioStorage;
    private final SpeakingAsyncProcessor asyncProcessor;
    private final ObjectMapper objectMapper;

    /** Danh sách bài luyện nói đã publish theo cấp độ, kèm tiến độ của student. */
    public List<SpeakingExerciseResponse> getExercises(String level, Long studentId) {
        StudentUser.JlptLevel jlptLevel = parseLevel(level);
        List<Lesson> lessons = lessonRepository.findByJlptLevelAndLessonTypeAndStatusOrderByDisplayOrderAscIdAsc(
                jlptLevel, Lesson.LessonType.SPEAKING, Lesson.LessonStatus.PUBLISHED);

        Map<Long, int[]> stats = loadStats(studentId, lessons); // exerciseId → [attemptCount, bestScore]

        return lessons.stream()
                .map(lesson -> {
                    int[] s = stats.get(lesson.getId());
                    return SpeakingExerciseResponse.builder()
                            .exerciseId(lesson.getId())
                            .title(lesson.getTitle())
                            .level(lesson.getJlptLevel().name())
                            .category(CATEGORY_LABEL)
                            .targetText(lesson.getContentText())
                            .sampleAudioUrl(lesson.getAudioUrl())
                            .attemptCount(s == null ? 0 : s[0])
                            .bestScore(s == null || s[0] == 0 ? null : s[1])
                            .build();
                })
                .toList();
    }

    /** Nộp bài: lưu audio, tạo submission PENDING, enqueue chấm điểm AI. Trả ngay jobId (async). */
    public SpeakingSubmitResponse submit(Long exerciseId, MultipartFile audio, StudentUser student) {
        if (exerciseId == null || exerciseId <= 0) {
            throw new BadRequestException("Thiếu hoặc sai exerciseId");
        }
        Lesson exercise = lessonRepository
                .findByIdAndStatus(exerciseId, Lesson.LessonStatus.PUBLISHED)
                .filter(l -> l.getLessonType() == Lesson.LessonType.SPEAKING)
                .orElseThrow(() -> new ResourceNotFoundException("Bài luyện nói", exerciseId));

        SpeakingAudioStorageService.StoredAudio stored = audioStorage.store(audio, student.getId());

        StudentSubmission submission = StudentSubmission.builder()
                .student(student)
                .submissionType(StudentSubmission.SubmissionType.SPEAKING)
                .status(StudentSubmission.SubmissionStatus.PENDING)
                .exercise(exercise)
                .recordingUrl(stored.url())
                .build();
        submission = submissionRepository.save(submission); // commit ngay để luồng async đọc được

        asyncProcessor.process(submission.getId(), exercise.getContentText(), stored.path());

        return SpeakingSubmitResponse.builder()
                .jobId(submission.getId())
                .status("PENDING")
                .build();
    }

    /** Poll kết quả — chỉ trả bài của chính student. */
    public SpeakingResultResponse getResult(Long jobId, Long studentId) {
        StudentSubmission submission = submissionRepository
                .findByIdAndStudent_Id(jobId, studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Bài nộp", jobId));

        return switch (submission.getStatus()) {
            case PENDING -> SpeakingResultResponse.builder()
                    .jobId(jobId)
                    .status("PENDING")
                    .build();
            case REJECTED -> SpeakingResultResponse.builder()
                    .jobId(jobId)
                    .status("FAILED")
                    .error(REJECTED_MESSAGE)
                    .build();
            case AI_GRADED, GRADED -> toGradedResponse(submission);
        };
    }

    private SpeakingResultResponse toGradedResponse(StudentSubmission submission) {
        BigDecimal score =
                submission.getManualScore() != null ? submission.getManualScore() : submission.getAiOverallScore();

        // AI_GRADED nhưng không có điểm → fallback thất bại (UC-13 §3.3), hiện message thân thiện.
        if (score == null) {
            return SpeakingResultResponse.builder()
                    .jobId(submission.getId())
                    .status("FAILED")
                    .error(
                            submission.getAiSuggestions() != null
                                    ? submission.getAiSuggestions()
                                    : "Không thể xử lý bài nộp. Vui lòng thử lại sau.")
                    .build();
        }

        SpeakingAiDetail detail = parseDetail(submission.getAiErrorSummary());
        return SpeakingResultResponse.builder()
                .jobId(submission.getId())
                .status("COMPLETED")
                .score(score.setScale(0, RoundingMode.HALF_UP).intValue())
                .transcript(detail == null ? null : detail.getTranscript())
                .wordResults(detail == null ? null : detail.getWordResults())
                .build();
    }

    private Map<Long, int[]> loadStats(Long studentId, List<Lesson> lessons) {
        Map<Long, int[]> stats = new HashMap<>();
        if (lessons.isEmpty()) {
            return stats;
        }
        List<Long> ids = lessons.stream().map(Lesson::getId).toList();
        for (Object[] row :
                submissionRepository.findSpeakingStats(studentId, StudentSubmission.SubmissionType.SPEAKING, ids)) {
            Long exerciseId = (Long) row[0];
            int attempts = ((Number) row[1]).intValue();
            BigDecimal best = (BigDecimal) row[2];
            int bestScore =
                    best == null ? 0 : best.setScale(0, RoundingMode.HALF_UP).intValue();
            stats.put(exerciseId, new int[] {attempts, bestScore});
        }
        return stats;
    }

    private SpeakingAiDetail parseDetail(String json) {
        if (json == null || json.isBlank()) {
            return null;
        }
        try {
            return objectMapper.readValue(json, SpeakingAiDetail.class);
        } catch (Exception e) {
            log.warn("[Speaking] Không đọc được chi tiết AI (submission detail): {}", e.getMessage());
            return null;
        }
    }

    private StudentUser.JlptLevel parseLevel(String level) {
        if (level == null) {
            throw new BadRequestException("Thiếu tham số level");
        }
        try {
            return StudentUser.JlptLevel.valueOf(level.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Cấp độ JLPT không hợp lệ: " + level);
        }
    }
}
