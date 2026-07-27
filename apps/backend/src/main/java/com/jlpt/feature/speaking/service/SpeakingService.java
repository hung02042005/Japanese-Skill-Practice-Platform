/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.speaking.service;

import com.jlpt.feature.assessment.StudentSubmission;
import com.jlpt.feature.assessment.StudentSubmissionRepository;
import com.jlpt.feature.learning.Lesson;
import com.jlpt.feature.learning.LessonRepository;
import com.jlpt.feature.speaking.dto.SpeakingExerciseResponse;
import com.jlpt.feature.speaking.dto.SpeakingQuestionDto;
import com.jlpt.feature.speaking.dto.SpeakingResultResponse;
import com.jlpt.feature.speaking.dto.SpeakingSubmitResponse;
import com.jlpt.feature.speaking.entity.SpeakingQuestion;
import com.jlpt.feature.speaking.repository.SpeakingQuestionRepository;
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

/**
 * Nghiệp vụ luyện nói (UC-13). Học viên ghi âm & nộp bài; bài được GIÁO VIÊN (staff) chấm thủ công
 * — không có bước AI tự chấm. Bài nộp giữ trạng thái PENDING cho tới khi staff chấm (→ GRADED).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SpeakingService {

    private static final String CATEGORY_LABEL = "Shadowing";
    private static final String REJECTED_MESSAGE = "Bài nộp không hợp lệ. Vui lòng ghi âm và thử lại.";

    private final LessonRepository lessonRepository;
    private final SpeakingQuestionRepository speakingQuestionRepository;
    private final StudentSubmissionRepository submissionRepository;
    private final SpeakingAudioStorageService audioStorage;

    /** Danh sách bài luyện nói đã publish theo cấp độ, kèm tiến độ (điểm giáo viên chấm) của student. */
    public List<SpeakingExerciseResponse> getExercises(String level, Long studentId) {
        StudentUser.JlptLevel jlptLevel = parseLevel(level);
        List<Lesson> lessons = lessonRepository.findByJlptLevelAndLessonTypeAndStatusOrderByDisplayOrderAscIdAsc(
                jlptLevel, Lesson.LessonType.SPEAKING, Lesson.LessonStatus.PUBLISHED);

        Map<Long, int[]> stats = loadStats(studentId, lessons); // exerciseId → [attemptCount, bestScore]

        return lessons.stream()
                .map(lesson -> {
                    int[] s = stats.get(lesson.getId());
                    List<SpeakingQuestionDto> questions = loadQuestions(lesson);
                    return SpeakingExerciseResponse.builder()
                            .exerciseId(lesson.getId())
                            .title(lesson.getTitle())
                            .level(lesson.getJlptLevel().name())
                            .category(CATEGORY_LABEL)
                            .targetText(lesson.getContentText())
                            .sampleAudioUrl(lesson.getAudioUrl())
                            .questions(questions)
                            .attemptCount(s == null ? 0 : s[0])
                            .bestScore(s == null || s[1] < 0 ? null : s[1])
                            .build();
                })
                .toList();
    }

    /** Nộp bài: lưu audio, tạo submission PENDING chờ giáo viên chấm. Trả jobId để tra cứu kết quả. */
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
        submission = submissionRepository.save(submission);

        log.info(
                "[Speaking] student={} nộp bài {} → submission {} (chờ giáo viên chấm)",
                student.getId(),
                exerciseId,
                submission.getId());

        return SpeakingSubmitResponse.builder()
                .jobId(submission.getId())
                .status("PENDING")
                .build();
    }

    /** Tra cứu kết quả một lần nộp — chỉ trả bài của chính student. */
    public SpeakingResultResponse getResult(Long jobId, Long studentId) {
        StudentSubmission submission = submissionRepository
                .findByIdAndStudent_Id(jobId, studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Bài nộp", jobId));

        return switch (submission.getStatus()) {
            case REJECTED -> SpeakingResultResponse.builder()
                    .jobId(jobId)
                    .status("FAILED")
                    .error(REJECTED_MESSAGE)
                    .build();
            case GRADED, AI_GRADED -> toGradedResponse(submission);
            case PENDING -> SpeakingResultResponse.builder()
                    .jobId(jobId)
                    .status("PENDING")
                    .build();
        };
    }

    private SpeakingResultResponse toGradedResponse(StudentSubmission submission) {
        // Chỉ tính điểm do giáo viên chấm (manual). Nếu chưa có → vẫn coi là đang chờ chấm.
        BigDecimal score = submission.getManualScore();
        if (score == null) {
            return SpeakingResultResponse.builder()
                    .jobId(submission.getId())
                    .status("PENDING")
                    .build();
        }
        return SpeakingResultResponse.builder()
                .jobId(submission.getId())
                .status("COMPLETED")
                .score(score.setScale(0, RoundingMode.HALF_UP).intValue())
                .feedback(submission.getManualFeedback())
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
            BigDecimal best = (BigDecimal) row[2]; // MAX(manual_score) — null nếu chưa được chấm
            int bestScore =
                    best == null ? -1 : best.setScale(0, RoundingMode.HALF_UP).intValue();
            stats.put(exerciseId, new int[] {attempts, bestScore});
        }
        return stats;
    }

    private List<SpeakingQuestionDto> loadQuestions(Lesson lesson) {
        List<SpeakingQuestionDto> questions =
                speakingQuestionRepository.findByLesson_IdOrderByDisplayOrderAsc(lesson.getId()).stream()
                        .map(this::toQuestionDto)
                        .toList();
        if (!questions.isEmpty()) {
            return questions;
        }
        if (lesson.getContentText() == null || lesson.getContentText().isBlank()) {
            return List.of();
        }
        return List.of(SpeakingQuestionDto.builder()
                .promptText(lesson.getContentText())
                .sampleAudioUrl(lesson.getAudioUrl())
                .displayOrder(0)
                .build());
    }

    private SpeakingQuestionDto toQuestionDto(SpeakingQuestion question) {
        return SpeakingQuestionDto.builder()
                .speakingQuestionId(question.getId())
                .promptText(question.getPromptText())
                .instruction(question.getInstruction())
                .sampleAudioUrl(question.getSampleAudioUrl())
                .displayOrder(question.getDisplayOrder())
                .build();
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
