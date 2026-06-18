/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.assessment;

import com.jlpt.feature.admin.AdminAuditLog;
import com.jlpt.feature.admin.AdminAuditLogRepository;
import com.jlpt.feature.assessment.dto.request.AnswerSubmissionRequest;
import com.jlpt.feature.assessment.dto.request.MockTestRequest;
import com.jlpt.feature.assessment.dto.response.MockTestResponse;
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
public class MockTestService {

    private final AssessmentRepository assessmentRepository;
    private final QuestionAssignmentRepository questionAssignmentRepository;
    private final TestAttemptRepository testAttemptRepository;
    private final AttemptAnswerRepository attemptAnswerRepository;
    private final AdminAuditLogRepository adminAuditLogRepository;

    @Transactional
    public MockTestResponse generateMockTest(MockTestRequest request, com.jlpt.feature.staff.StaffUser staffUser) {
        Assessment assessment = Assessment.builder()
                .assessmentType(Assessment.AssessmentType.EXAM)
                .title(request.getTitle())
                .jlptLevel(
                        request.getJlptLevel() != null ? StudentUser.JlptLevel.valueOf(request.getJlptLevel()) : null)
                .durationMin(request.getDurationMin())
                .passScore(request.getPassScore())
                .audioUrl(request.getAudioUrl())
                .status(Kanji.ContentStatus.DRAFT)
                .createdBy(staffUser)
                .build();

        assessment = assessmentRepository.save(assessment);
        return mapToResponse(assessment);
    }

    @Transactional
    public ScoreResponse gradeMockTest(
            Long mockTestId, Long studentId, Long attemptId, List<AnswerSubmissionRequest> answers) {
        Assessment assessment = assessmentRepository
                .findById(mockTestId)
                .orElseThrow(() -> new ResourceNotFoundException("MockTest", mockTestId));

        TestAttempt attempt = testAttemptRepository
                .findByIdForUpdate(attemptId)
                .orElseThrow(() -> new ResourceNotFoundException("Attempt", attemptId));

        if (!attempt.getStudent().getId().equals(studentId)) {
            throw new ForbiddenException("Not your attempt");
        }

        if (attempt.getStatus() != TestAttempt.AttemptStatus.IN_PROGRESS) {
            throw new AttemptAlreadySubmittedException("Bài đã nộp");
        }

        List<QuestionAssignment> assignments =
                questionAssignmentRepository.findByParentTypeAndParentIdOrderByDisplayOrder(
                        QuestionAssignment.ParentType.ASSESSMENT, assessment.getId());

        Map<Long, AnswerSubmissionRequest> answerMap =
                answers.stream().collect(Collectors.toMap(AnswerSubmissionRequest::getQuestionId, a -> a));

        BigDecimal totalScore = BigDecimal.ZERO;
        BigDecimal maxScore = BigDecimal.ZERO;
        List<AttemptAnswer> answerEntities = new ArrayList<>();
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
                .action("MOCK_TEST_SUBMITTED")
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
                .build();
    }

    public String mapScoreToJLPT(BigDecimal score) {
        // JLPT Mapping logic implementation
        return "N/A";
    }

    private MockTestResponse mapToResponse(Assessment a) {
        return MockTestResponse.builder()
                .id(a.getId())
                .title(a.getTitle())
                .jlptLevel(a.getJlptLevel() != null ? a.getJlptLevel().name() : null)
                .durationMin(a.getDurationMin())
                .passScore(a.getPassScore())
                .totalScore(a.getTotalScore())
                .audioUrl(a.getAudioUrl())
                .status(a.getStatus().name())
                .createdAt(a.getCreatedAt())
                .updatedAt(a.getUpdatedAt())
                .build();
    }
}
