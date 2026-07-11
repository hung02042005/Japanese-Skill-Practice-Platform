/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.assessment;

import com.jlpt.feature.admin.AdminAuditLog;
import com.jlpt.feature.admin.AdminAuditLogRepository;
import com.jlpt.feature.assessment.dto.request.AnswerRequest;
import com.jlpt.feature.assessment.dto.request.SubmitExamRequest;
import com.jlpt.feature.assessment.dto.response.ExamHistoryResponse;
import com.jlpt.feature.assessment.dto.response.ExamResultItem;
import com.jlpt.feature.assessment.dto.response.ExamReviewItem;
import com.jlpt.feature.assessment.dto.response.ExamReviewResponse;
import com.jlpt.feature.assessment.dto.response.ExamStartResponse;
import com.jlpt.feature.assessment.dto.response.ExamSubmitResponse;
import com.jlpt.feature.assessment.dto.response.SectionScoresResponse;
import com.jlpt.feature.learning.Kanji;
import com.jlpt.feature.student.StudentUser;
import com.jlpt.shared.exception.AttemptAlreadySubmittedException;
import com.jlpt.shared.exception.BadRequestException;
import com.jlpt.shared.exception.BusinessRuleException;
import com.jlpt.shared.exception.ForbiddenException;
import com.jlpt.shared.exception.ResourceNotFoundException;
import com.jlpt.shared.exception.TimeExceededException;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Business logic cho JLPT Mock Exam (UC-10) — xem {@code .sdd/specs/backend/feat-mock-test}. */
@Slf4j
@Service
@RequiredArgsConstructor
public class MockExamService {

    private static final String SECTION_LANGUAGE = "language_knowledge";
    private static final String SECTION_READING = "reading";
    private static final String SECTION_LISTENING = "listening";

    /**
     * Section của câu hỏi (bộ staff gán: vocabulary/grammar/kanji/reading/listening) được gom về 3 nhóm
     * điểm JLPT thật: 言語知識(language_knowledge) = từ vựng + ngữ pháp + kanji, 読解(reading), 聴解(listening).
     * Bảo toàn bất biến NFR-MOCK-05: languageKnowledge + reading + listening == totalScore.
     */
    private static final java.util.Set<String> LANGUAGE_KNOWLEDGE_SECTIONS =
            java.util.Set.of("language_knowledge", "vocabulary", "grammar", "kanji");

    private static String canonicalSection(String raw) {
        if (raw == null) {
            return SECTION_LANGUAGE;
        }
        String s = raw.trim().toLowerCase();
        if (SECTION_READING.equals(s)) {
            return SECTION_READING;
        }
        if (SECTION_LISTENING.equals(s)) {
            return SECTION_LISTENING;
        }
        // language_knowledge + vocabulary/grammar/kanji + mọi giá trị khác → gom vào ngôn ngữ
        return SECTION_LANGUAGE;
    }

    private final AssessmentRepository assessmentRepository;
    private final QuestionAssignmentRepository questionAssignmentRepository;
    private final TestAttemptRepository testAttemptRepository;
    private final AttemptAnswerRepository attemptAnswerRepository;
    private final AdminAuditLogRepository adminAuditLogRepository;

    @Transactional
    public ExamStartResponse startExam(Long assessmentId, StudentUser student) {
        Assessment assessment = assessmentRepository
                .findByIdAndAssessmentTypeAndStatus(
                        assessmentId, Assessment.AssessmentType.EXAM, Kanji.ContentStatus.PUBLISHED)
                .orElseThrow(() -> new ResourceNotFoundException("Assessment", assessmentId));

        List<QuestionAssignment> assignments =
                questionAssignmentRepository.findByParentTypeAndParentIdOrderByDisplayOrder(
                        QuestionAssignment.ParentType.ASSESSMENT, assessmentId);
        if (assignments.isEmpty()) {
            throw new ResourceNotFoundException("Không tìm thấy câu hỏi cho đề thi này");
        }

        LocalDateTime startedAt = LocalDateTime.now();
        TestAttempt attempt = testAttemptRepository.save(TestAttempt.builder()
                .student(student)
                .attemptType(TestAttempt.AttemptType.EXAM)
                .parentType(TestAttempt.ParentType.ASSESSMENT)
                .parentId(assessmentId)
                .startedAt(startedAt)
                .maxScore(assessment.getTotalScore() != null ? BigDecimal.valueOf(assessment.getTotalScore()) : null)
                .status(TestAttempt.AttemptStatus.IN_PROGRESS)
                .build());

        log.info(
                "[MockExamService] EXAM_STARTED studentId={} assessmentId={} attemptId={}",
                student.getId(),
                assessmentId,
                attempt.getId());

        return ExamStartResponse.builder()
                .attemptId(attempt.getId())
                .startedAt(startedAt)
                .expiresAt(computeExpiresAt(assessment, startedAt))
                .sections(QuestionAssignmentSupport.groupBySection(assignments))
                .listeningAudioUrl(assessment.getAudioUrl())
                .build();
    }

    @Transactional
    public ExamSubmitResponse submitExam(Long assessmentId, Long studentId, SubmitExamRequest request) {
        Assessment assessment = assessmentRepository
                .findById(assessmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Assessment", assessmentId));

        TestAttempt attempt = testAttemptRepository
                .findByIdForUpdate(request.getAttemptId())
                .orElseThrow(() -> new ResourceNotFoundException("Attempt", request.getAttemptId()));

        if (!attempt.getStudent().getId().equals(studentId)) {
            throw new ForbiddenException("Bạn không có quyền thao tác trên bài làm này");
        }
        if (!attempt.getParentId().equals(assessmentId)) {
            throw new BadRequestException("Attempt không thuộc đề thi này");
        }
        if (attempt.getStatus() != TestAttempt.AttemptStatus.IN_PROGRESS) {
            throw new AttemptAlreadySubmittedException("Bài đã được nộp");
        }

        boolean isAutoSubmit = Boolean.TRUE.equals(request.getIsAutoSubmit());
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiresAt = computeExpiresAt(assessment, attempt.getStartedAt());
        if (!isAutoSubmit && expiresAt != null && now.isAfter(expiresAt)) {
            throw new TimeExceededException("Đã hết thời gian làm bài");
        }

        List<QuestionAssignment> assignments =
                questionAssignmentRepository.findByParentTypeAndParentIdOrderByDisplayOrder(
                        QuestionAssignment.ParentType.ASSESSMENT, assessmentId);
        if (assignments.isEmpty()) {
            throw new ResourceNotFoundException("Không tìm thấy câu hỏi cho đề thi này");
        }

        return gradeAndPersist(attempt, assessment, assignments, request.getAnswers(), isAutoSubmit, now);
    }

    @Transactional(readOnly = true)
    public Page<ExamHistoryResponse> getExamHistory(Long studentId, Pageable pageable) {
        Page<TestAttempt> attempts =
                testAttemptRepository.findByStudent_IdAndAttemptTypeAndStatusInOrderBySubmittedAtDesc(
                        studentId,
                        TestAttempt.AttemptType.EXAM,
                        List.of(TestAttempt.AttemptStatus.SUBMITTED, TestAttempt.AttemptStatus.AUTO_SUBMITTED),
                        pageable);

        List<Long> assessmentIds = attempts.getContent().stream()
                .map(TestAttempt::getParentId)
                .distinct()
                .toList();
        Map<Long, Assessment> assessmentsById = assessmentRepository.findAllById(assessmentIds).stream()
                .collect(Collectors.toMap(Assessment::getId, a -> a));

        return attempts.map(attempt -> toHistoryResponse(attempt, assessmentsById.get(attempt.getParentId())));
    }

    @Transactional(readOnly = true)
    public ExamReviewResponse getExamReview(Long attemptId, Long studentId) {
        TestAttempt attempt = findOwnedAttempt(attemptId, studentId);
        if (attempt.getStatus() == TestAttempt.AttemptStatus.IN_PROGRESS) {
            throw new BadRequestException("Bài thi chưa kết thúc");
        }

        List<AttemptAnswer> answers = attemptAnswerRepository.findByAttemptIdWithQuestion(attemptId);
        List<ExamReviewItem> items = answers.stream().map(this::toReviewItem).toList();

        return ExamReviewResponse.builder()
                .attemptId(attempt.getId())
                .totalScore(attempt.getTotalScore())
                .maxScore(attempt.getMaxScore())
                .isPassed(attempt.getIsPassed())
                .sectionScores(toSectionScores(attempt))
                .results(items)
                .build();
    }

    private ExamSubmitResponse gradeAndPersist(
            TestAttempt attempt,
            Assessment assessment,
            List<QuestionAssignment> assignments,
            List<AnswerRequest> answers,
            boolean isAutoSubmit,
            LocalDateTime now) {
        Map<Long, AnswerRequest> answerMap =
                answers.stream().collect(Collectors.toMap(AnswerRequest::getQuestionId, a -> a));

        Map<String, BigDecimal> sectionScores = new LinkedHashMap<>();
        BigDecimal totalScore = BigDecimal.ZERO;
        BigDecimal maxScore = BigDecimal.ZERO;
        List<AttemptAnswer> answerEntities = new ArrayList<>();
        List<ExamResultItem> results = new ArrayList<>();

        for (QuestionAssignment qa : assignments) {
            Question question = qa.getQuestion();
            String section = qa.getSectionName() != null ? qa.getSectionName() : "default";
            maxScore = maxScore.add(qa.getScore());

            AnswerRequest input = answerMap.get(question.getId());
            String selectedOption = input != null ? input.getSelectedOption() : null;
            String answerText = input != null ? input.getAnswerText() : null;
            boolean correct = QuestionAssignmentSupport.isCorrect(question, selectedOption, answerText);
            BigDecimal score = correct ? qa.getScore() : BigDecimal.ZERO;
            totalScore = totalScore.add(score);
            sectionScores.merge(canonicalSection(section), score, BigDecimal::add);

            answerEntities.add(AttemptAnswer.builder()
                    .attempt(attempt)
                    .question(question)
                    .selectedOption(selectedOption)
                    .answerText(answerText)
                    .isCorrect(correct)
                    .score(score)
                    .answeredAt(now)
                    .build());

            results.add(ExamResultItem.builder()
                    .questionId(question.getId())
                    .sectionName(section)
                    .isCorrect(correct)
                    .selectedOption(selectedOption)
                    .correctOption(question.getCorrectOption())
                    .score(score)
                    .explanation(question.getExplanation())
                    .build());
        }

        if (totalScore.compareTo(BigDecimal.ZERO) < 0 || totalScore.compareTo(maxScore) > 0) {
            log.error(
                    "[MockExamService] Score invariant violated attemptId={} totalScore={} maxScore={}",
                    attempt.getId(),
                    totalScore,
                    maxScore);
            throw new BusinessRuleException("Điểm số không hợp lệ");
        }

        attemptAnswerRepository.saveAll(answerEntities);

        boolean isPassed = assessment.getPassScore() != null
                && totalScore.compareTo(BigDecimal.valueOf(assessment.getPassScore())) >= 0;
        int durationSeconds =
                (int) Duration.between(attempt.getStartedAt(), now).getSeconds();

        attempt.setStatus(
                isAutoSubmit ? TestAttempt.AttemptStatus.AUTO_SUBMITTED : TestAttempt.AttemptStatus.SUBMITTED);
        attempt.setSubmittedAt(now);
        attempt.setTotalScore(totalScore);
        attempt.setMaxScore(maxScore);
        attempt.setDurationSeconds(durationSeconds);
        attempt.setIsPassed(isPassed);
        attempt.setLanguageKnowledgeScore(sectionScores.getOrDefault(SECTION_LANGUAGE, BigDecimal.ZERO));
        attempt.setReadingScore(sectionScores.getOrDefault(SECTION_READING, BigDecimal.ZERO));
        attempt.setListeningScore(sectionScores.getOrDefault(SECTION_LISTENING, BigDecimal.ZERO));
        testAttemptRepository.save(attempt);

        AdminAuditLog auditLog = AdminAuditLog.builder()
                .studentActor(attempt.getStudent())
                .action("EXAM_SUBMITTED")
                .targetTable("test_attempts")
                .targetId(attempt.getId())
                .description("score=" + totalScore + "/" + maxScore + " isPassed=" + isPassed)
                .build();
        adminAuditLogRepository.save(auditLog);

        log.info(
                "[MockExamService] EXAM_SUBMITTED studentId={} attemptId={} totalScore={} maxScore={} isPassed={} durationSeconds={}",
                attempt.getStudent().getId(),
                attempt.getId(),
                totalScore,
                maxScore,
                isPassed,
                durationSeconds);

        return ExamSubmitResponse.builder()
                .attemptId(attempt.getId())
                .totalScore(totalScore)
                .maxScore(maxScore)
                .isPassed(isPassed)
                .durationSeconds(durationSeconds)
                .submittedAt(now)
                .sectionScores(SectionScoresResponse.builder()
                        .languageKnowledge(sectionScores.getOrDefault(SECTION_LANGUAGE, BigDecimal.ZERO))
                        .reading(sectionScores.getOrDefault(SECTION_READING, BigDecimal.ZERO))
                        .listening(sectionScores.getOrDefault(SECTION_LISTENING, BigDecimal.ZERO))
                        .build())
                .results(results)
                .build();
    }

    private TestAttempt findOwnedAttempt(Long attemptId, Long studentId) {
        TestAttempt attempt = testAttemptRepository
                .findById(attemptId)
                .orElseThrow(() -> new ResourceNotFoundException("Attempt", attemptId));
        if (!attempt.getStudent().getId().equals(studentId)) {
            throw new ForbiddenException("Bạn không có quyền thao tác trên bài làm này");
        }
        return attempt;
    }

    private LocalDateTime computeExpiresAt(Assessment assessment, LocalDateTime startedAt) {
        return assessment.getDurationMin() != null ? startedAt.plusMinutes(assessment.getDurationMin()) : null;
    }

    private SectionScoresResponse toSectionScores(TestAttempt attempt) {
        return SectionScoresResponse.builder()
                .languageKnowledge(attempt.getLanguageKnowledgeScore())
                .reading(attempt.getReadingScore())
                .listening(attempt.getListeningScore())
                .build();
    }

    private ExamHistoryResponse toHistoryResponse(TestAttempt attempt, Assessment assessment) {
        return ExamHistoryResponse.builder()
                .attemptId(attempt.getId())
                .assessmentTitle(assessment != null ? assessment.getTitle() : null)
                .jlptLevel(
                        assessment != null && assessment.getJlptLevel() != null
                                ? assessment.getJlptLevel().name()
                                : null)
                .totalScore(attempt.getTotalScore())
                .maxScore(attempt.getMaxScore())
                .isPassed(attempt.getIsPassed())
                .status(attempt.getStatus().getValue())
                .sectionScores(toSectionScores(attempt))
                .submittedAt(attempt.getSubmittedAt())
                .durationSeconds(attempt.getDurationSeconds())
                .build();
    }

    private ExamReviewItem toReviewItem(AttemptAnswer answer) {
        Question question = answer.getQuestion();
        return ExamReviewItem.builder()
                .questionId(question.getId())
                .questionText(question.getQuestionText())
                .optionA(question.getOptionA())
                .optionB(question.getOptionB())
                .optionC(question.getOptionC())
                .optionD(question.getOptionD())
                .selectedOption(answer.getSelectedOption())
                .correctOption(question.getCorrectOption())
                .isCorrect(answer.getIsCorrect())
                .score(answer.getScore())
                .explanation(question.getExplanation())
                .build();
    }
}
