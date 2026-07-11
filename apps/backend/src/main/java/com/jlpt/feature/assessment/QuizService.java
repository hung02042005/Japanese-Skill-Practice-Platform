/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.assessment;

import com.jlpt.feature.admin.AdminAuditLog;
import com.jlpt.feature.admin.AdminAuditLogRepository;
import com.jlpt.feature.assessment.dto.request.AnswerRequest;
import com.jlpt.feature.assessment.dto.request.QuestionRequest;
import com.jlpt.feature.assessment.dto.request.QuizRequest;
import com.jlpt.feature.assessment.dto.response.AssessmentSummaryResponse;
import com.jlpt.feature.assessment.dto.response.ExamStartResponse;
import com.jlpt.feature.assessment.dto.response.QuestionResponse;
import com.jlpt.feature.assessment.dto.response.QuestionResultResponse;
import com.jlpt.feature.assessment.dto.response.QuizResponse;
import com.jlpt.feature.assessment.dto.response.ScoreResponse;
import com.jlpt.feature.learning.Kanji;
import com.jlpt.feature.staff.StaffUser;
import com.jlpt.feature.student.StudentUser;
import com.jlpt.shared.exception.AttemptAlreadySubmittedException;
import com.jlpt.shared.exception.BusinessRuleException;
import com.jlpt.shared.exception.ForbiddenException;
import com.jlpt.shared.exception.ResourceNotFoundException;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class QuizService {

    private final AssessmentRepository assessmentRepository;
    private final QuestionRepository questionRepository;
    private final QuestionAssignmentRepository questionAssignmentRepository;
    private final TestAttemptRepository testAttemptRepository;
    private final AttemptAnswerRepository attemptAnswerRepository;
    private final AdminAuditLogRepository adminAuditLogRepository;

    // =========================================================
    // STAFF — Quản lý đề thi (Quiz)
    // =========================================================

    @Transactional
    public QuizResponse createQuiz(QuizRequest request, StaffUser staffUser) {
        Assessment assessment = Assessment.builder()
                .assessmentType(Assessment.AssessmentType.QUIZ)
                .title(request.getTitle())
                .topic(request.getTopic())
                .jlptLevel(
                        request.getJlptLevel() != null ? StudentUser.JlptLevel.valueOf(request.getJlptLevel()) : null)
                .durationMin(request.getDurationMin())
                .passScore(request.getPassScore())
                .status(Kanji.ContentStatus.DRAFT)
                .createdBy(staffUser)
                .build();

        assessment = assessmentRepository.save(assessment);
        log.info(
                "[QuizService] QUIZ_CREATED assessmentId={} by staffId={}",
                assessment.getId(),
                staffUser != null ? staffUser.getId() : null);
        return mapToQuizResponse(assessment);
    }

    @Transactional
    public QuizResponse updateAssessment(Long assessmentId, QuizRequest request) {
        Assessment assessment = assessmentRepository
                .findByIdAndIsDeletedFalse(assessmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Assessment", assessmentId));

        assessment.setTitle(request.getTitle());
        assessment.setTopic(request.getTopic());
        if (request.getJlptLevel() != null) {
            assessment.setJlptLevel(StudentUser.JlptLevel.valueOf(request.getJlptLevel()));
        }
        assessment.setDurationMin(request.getDurationMin());
        assessment.setPassScore(request.getPassScore());

        assessment = assessmentRepository.save(assessment);
        log.info("[QuizService] ASSESSMENT_UPDATED assessmentId={}", assessmentId);
        return mapToQuizResponse(assessment);
    }

    @Transactional
    public void softDeleteAssessment(Long assessmentId) {
        Assessment assessment = assessmentRepository
                .findByIdAndIsDeletedFalse(assessmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Assessment", assessmentId));
        assessment.setIsDeleted(Boolean.TRUE);
        assessmentRepository.save(assessment);

        adminAuditLogRepository.save(AdminAuditLog.builder()
                .action("ASSESSMENT_SOFT_DELETED")
                .targetTable("assessments")
                .targetId(assessmentId)
                .description("soft-deleted assessmentId=" + assessmentId)
                .build());
        log.info("[QuizService] ASSESSMENT_SOFT_DELETED assessmentId={}", assessmentId);
    }

    @Transactional
    public void addQuestions(Long quizId, List<QuestionRequest> questions, StaffUser staffUser) {
        assessmentRepository
                .findByIdAndIsDeletedFalse(quizId)
                .orElseThrow(() -> new ResourceNotFoundException("Quiz", quizId));

        int order = 1;
        for (QuestionRequest req : questions) {
            Question.QuestionType questionType;
            Question.Skill skill;
            StudentUser.JlptLevel jlptLevel;

            try {
                questionType = Question.QuestionType.valueOf(
                        req.getQuestionType().toUpperCase().replace("-", "_").replace(" ", "_"));
            } catch (IllegalArgumentException e) {
                throw new BusinessRuleException("questionType không hợp lệ: " + req.getQuestionType());
            }
            try {
                skill = Question.Skill.valueOf(req.getSkill().toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new BusinessRuleException("skill không hợp lệ: " + req.getSkill());
            }
            try {
                jlptLevel = StudentUser.JlptLevel.valueOf(req.getJlptLevel().toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new BusinessRuleException("jlptLevel không hợp lệ: " + req.getJlptLevel());
            }

            Question question = Question.builder()
                    .questionText(req.getQuestionText())
                    .questionType(questionType)
                    .skill(skill)
                    .jlptLevel(jlptLevel)
                    .optionA(req.getOptionA())
                    .optionB(req.getOptionB())
                    .optionC(req.getOptionC())
                    .optionD(req.getOptionD())
                    .correctOption(req.getCorrectOption())
                    .correctAnswerText(req.getCorrectAnswerText())
                    .explanation(req.getExplanation())
                    .audioUrl(req.getAudioUrl())
                    .imageUrl(req.getImageUrl())
                    .status(Question.ContentStatus.DRAFT)
                    .createdBy(staffUser)
                    .build();
            question = questionRepository.save(question);

            QuestionAssignment qa = QuestionAssignment.builder()
                    .parentType(QuestionAssignment.ParentType.ASSESSMENT)
                    .parentId(quizId)
                    .question(question)
                    .score(req.getScore())
                    .sectionName(req.getSection())
                    .displayOrder(req.getDisplayOrder() != null ? req.getDisplayOrder() : order++)
                    .build();
            questionAssignmentRepository.save(qa);
        }
        log.info("[QuizService] QUESTIONS_ADDED quizId={} count={}", quizId, questions.size());
    }

    @Transactional(readOnly = true)
    public Page<AssessmentSummaryResponse> listAssessmentsForStaff(
            Assessment.AssessmentType type,
            Kanji.ContentStatus status,
            StudentUser.JlptLevel jlptLevel,
            Pageable pageable) {
        Page<Assessment> assessments =
                assessmentRepository.findAllByAssessmentTypeForStaff(type, status, jlptLevel, pageable);
        return assessments.map(this::toSummaryResponse);
    }

    @Transactional(readOnly = true)
    public List<QuestionResponse> getQuestionsOfAssessment(Long assessmentId) {
        assessmentRepository
                .findByIdAndIsDeletedFalse(assessmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Assessment", assessmentId));
        List<QuestionAssignment> assignments =
                questionAssignmentRepository.findByParentTypeAndParentIdOrderByDisplayOrder(
                        QuestionAssignment.ParentType.ASSESSMENT, assessmentId);
        return assignments.stream()
                .map(QuestionAssignmentSupport::toQuestionResponse)
                .collect(Collectors.toList());
    }

    // =========================================================
    // STUDENT — Làm bài Quiz
    // =========================================================

    @Transactional
    public ExamStartResponse startQuiz(Long quizId, StudentUser student) {
        Assessment assessment = assessmentRepository
                .findByIdAndAssessmentTypeAndStatus(
                        quizId, Assessment.AssessmentType.QUIZ, Kanji.ContentStatus.PUBLISHED)
                .orElseThrow(() -> new ResourceNotFoundException("Quiz", quizId));

        List<QuestionAssignment> assignments =
                questionAssignmentRepository.findByParentTypeAndParentIdOrderByDisplayOrder(
                        QuestionAssignment.ParentType.ASSESSMENT, quizId);
        if (assignments.isEmpty()) {
            throw new ResourceNotFoundException("Không tìm thấy câu hỏi cho bài quiz này");
        }

        LocalDateTime startedAt = LocalDateTime.now();
        TestAttempt attempt = testAttemptRepository.save(TestAttempt.builder()
                .student(student)
                .attemptType(TestAttempt.AttemptType.QUIZ)
                .parentType(TestAttempt.ParentType.ASSESSMENT)
                .parentId(quizId)
                .startedAt(startedAt)
                .maxScore(assessment.getTotalScore() != null ? BigDecimal.valueOf(assessment.getTotalScore()) : null)
                .status(TestAttempt.AttemptStatus.IN_PROGRESS)
                .build());

        return ExamStartResponse.builder()
                .attemptId(attempt.getId())
                .startedAt(startedAt)
                .expiresAt(
                        assessment.getDurationMin() != null ? startedAt.plusMinutes(assessment.getDurationMin()) : null)
                .sections(QuestionAssignmentSupport.groupBySection(assignments))
                .build();
    }

    @Transactional
    public ScoreResponse submitQuiz(Long quizId, Long studentId, Long attemptId, List<AnswerRequest> answers) {
        Assessment assessment =
                assessmentRepository.findById(quizId).orElseThrow(() -> new ResourceNotFoundException("Quiz", quizId));

        TestAttempt attempt = testAttemptRepository
                .findByIdForUpdate(attemptId)
                .orElseThrow(() -> new ResourceNotFoundException("Attempt", attemptId));

        if (!attempt.getStudent().getId().equals(studentId)) {
            throw new ForbiddenException("Not your attempt");
        }

        if (attempt.getStatus() != TestAttempt.AttemptStatus.IN_PROGRESS) {
            throw new AttemptAlreadySubmittedException("Bài đã nộp");
        }

        return calculateScore(attempt, assessment, answers);
    }

    // =========================================================
    // Helpers dùng chung trong Controller
    // =========================================================

    public AssessmentSummaryResponse toSummaryResponse(Assessment assessment) {
        long questionCount = questionAssignmentRepository.countByParentTypeAndParentId(
                QuestionAssignment.ParentType.ASSESSMENT, assessment.getId());
        return AssessmentSummaryResponse.builder()
                .assessmentId(assessment.getId())
                .title(assessment.getTitle())
                .assessmentType(assessment.getAssessmentType().getValue())
                .jlptLevel(
                        assessment.getJlptLevel() != null
                                ? assessment.getJlptLevel().name()
                                : null)
                .durationMin(assessment.getDurationMin())
                .passScore(assessment.getPassScore())
                .totalScore(assessment.getTotalScore())
                .questionCount(questionCount)
                .build();
    }

    // =========================================================
    // Private helpers
    // =========================================================

    private ScoreResponse calculateScore(TestAttempt attempt, Assessment assessment, List<AnswerRequest> answers) {
        List<QuestionAssignment> assignments =
                questionAssignmentRepository.findByParentTypeAndParentIdOrderByDisplayOrder(
                        QuestionAssignment.ParentType.ASSESSMENT, assessment.getId());

        if (assignments.isEmpty()) {
            throw new ResourceNotFoundException("Không tìm thấy câu hỏi");
        }

        Map<Long, AnswerRequest> answerMap =
                answers.stream().collect(Collectors.toMap(AnswerRequest::getQuestionId, a -> a));

        BigDecimal totalScore = BigDecimal.ZERO;
        BigDecimal maxScore = BigDecimal.ZERO;
        List<AttemptAnswer> answerEntities = new ArrayList<>();
        List<QuestionResultResponse> results = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        for (QuestionAssignment qa : assignments) {
            Question question = qa.getQuestion();
            maxScore = maxScore.add(qa.getScore());

            AnswerRequest input = answerMap.get(question.getId());
            String selectedOption = input != null ? input.getSelectedOption() : null;
            String answerText = input != null ? input.getAnswerText() : null;
            boolean correct = QuestionAssignmentSupport.isCorrect(question, selectedOption, answerText);
            BigDecimal score = correct ? qa.getScore() : BigDecimal.ZERO;
            totalScore = totalScore.add(score);

            answerEntities.add(AttemptAnswer.builder()
                    .attempt(attempt)
                    .question(question)
                    .selectedOption(selectedOption)
                    .answerText(answerText)
                    .isCorrect(correct)
                    .score(score)
                    .answeredAt(now)
                    .build());

            results.add(QuestionResultResponse.builder()
                    .questionId(question.getId())
                    .isCorrect(correct)
                    .selectedOption(selectedOption)
                    .correctOption(question.getCorrectOption())
                    .explanation(question.getExplanation())
                    .build());
        }

        if (totalScore.compareTo(BigDecimal.ZERO) < 0 || totalScore.compareTo(maxScore) > 0) {
            throw new BusinessRuleException("Điểm số không hợp lệ.");
        }

        attemptAnswerRepository.saveAll(answerEntities);

        attempt.setStatus(TestAttempt.AttemptStatus.SUBMITTED);
        attempt.setSubmittedAt(now);
        attempt.setTotalScore(totalScore);
        attempt.setMaxScore(maxScore);
        attempt.setDurationSeconds(
                (int) Duration.between(attempt.getStartedAt(), now).getSeconds());
        if (assessment.getPassScore() != null) {
            attempt.setIsPassed(totalScore.compareTo(BigDecimal.valueOf(assessment.getPassScore())) >= 0);
        }
        testAttemptRepository.save(attempt);

        adminAuditLogRepository.save(AdminAuditLog.builder()
                .studentActor(attempt.getStudent())
                .action("QUIZ_SUBMITTED")
                .targetTable("test_attempts")
                .targetId(attempt.getId())
                .description("score=" + totalScore + "/" + maxScore)
                .build());

        log.info(
                "[QuizService] QUIZ_SUBMITTED studentId={} attemptId={} score={}/{}",
                attempt.getStudent().getId(),
                attempt.getId(),
                totalScore,
                maxScore);

        return ScoreResponse.builder()
                .attemptId(attempt.getId())
                .score(totalScore)
                .maxScore(maxScore)
                .isPassed(attempt.getIsPassed())
                .results(results)
                .build();
    }

    private QuizResponse mapToQuizResponse(Assessment a) {
        return QuizResponse.builder()
                .id(a.getId())
                .title(a.getTitle())
                .lessonId(a.getLesson() != null ? a.getLesson().getId() : null)
                .topic(a.getTopic())
                .jlptLevel(a.getJlptLevel() != null ? a.getJlptLevel().name() : null)
                .durationMin(a.getDurationMin())
                .passScore(a.getPassScore())
                .totalScore(a.getTotalScore())
                .status(a.getStatus().name())
                .createdAt(a.getCreatedAt())
                .updatedAt(a.getUpdatedAt())
                .build();
    }
}
