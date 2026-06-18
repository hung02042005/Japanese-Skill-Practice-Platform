/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.assessment;

import com.jlpt.feature.admin.AdminAuditLog;
import com.jlpt.feature.admin.AdminAuditLogRepository;
import com.jlpt.feature.assessment.dto.request.AnswerSubmissionRequest;
import com.jlpt.feature.assessment.dto.request.QuestionRequest;
import com.jlpt.feature.assessment.dto.request.QuizRequest;
import com.jlpt.feature.assessment.dto.response.QuestionResultResponse;
import com.jlpt.feature.assessment.dto.response.QuizResponse;
import com.jlpt.feature.assessment.dto.response.ScoreResponse;
import com.jlpt.feature.learning.Kanji;
import com.jlpt.feature.student.StudentUser;
import com.jlpt.shared.exception.AttemptAlreadySubmittedException;
import com.jlpt.shared.exception.BusinessRuleException;
import com.jlpt.shared.exception.ForbiddenException;
import com.jlpt.shared.exception.ResourceNotFoundException;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

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

    @Transactional
    public QuizResponse createQuiz(QuizRequest request, com.jlpt.feature.staff.StaffUser staffUser) {
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
        return mapToResponse(assessment);
    }

    @Transactional
    public void addQuestions(Long quizId, List<QuestionRequest> questions) {
        assessmentRepository.findById(quizId).orElseThrow(() -> new ResourceNotFoundException("Quiz", quizId));

        int order = 1;
        for (QuestionRequest req : questions) {
            Question question = Question.builder()
                    .questionText(req.getQuestionText())
                    .optionA(req.getOptionA())
                    .optionB(req.getOptionB())
                    .optionC(req.getOptionC())
                    .optionD(req.getOptionD())
                    .correctOption(req.getCorrectOption())
                    .explanation(req.getExplanation())
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
    }

    @Transactional
    public ScoreResponse submitQuiz(
            Long quizId, Long studentId, Long attemptId, List<AnswerSubmissionRequest> answers) {
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

    private ScoreResponse calculateScore(
            TestAttempt attempt, Assessment assessment, List<AnswerSubmissionRequest> answers) {
        List<QuestionAssignment> assignments =
                questionAssignmentRepository.findByParentTypeAndParentIdOrderByDisplayOrder(
                        QuestionAssignment.ParentType.ASSESSMENT, assessment.getId());

        if (assignments.isEmpty()) {
            throw new ResourceNotFoundException("Không tìm thấy câu hỏi");
        }

        Map<Long, AnswerSubmissionRequest> answerMap =
                answers.stream().collect(Collectors.toMap(AnswerSubmissionRequest::getQuestionId, a -> a));

        BigDecimal totalScore = BigDecimal.ZERO;
        BigDecimal maxScore = BigDecimal.ZERO;
        List<AttemptAnswer> answerEntities = new ArrayList<>();
        List<QuestionResultResponse> results = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        for (QuestionAssignment qa : assignments) {
            Question question = qa.getQuestion();
            maxScore = maxScore.add(qa.getScore());

            AnswerSubmissionRequest input = answerMap.get(question.getId());
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

        AdminAuditLog log = AdminAuditLog.builder()
                .studentActor(attempt.getStudent())
                .action("QUIZ_SUBMITTED")
                .targetTable("test_attempts")
                .targetId(attempt.getId())
                .description("score=" + totalScore + "/" + maxScore)
                .build();
        adminAuditLogRepository.save(log);

        return ScoreResponse.builder()
                .attemptId(attempt.getId())
                .score(totalScore)
                .maxScore(maxScore)
                .isPassed(attempt.getIsPassed())
                .results(results)
                .build();
    }

    private QuizResponse mapToResponse(Assessment a) {
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
