package com.jlpt.feature.student.reading.service;

import com.jlpt.feature.assessment.AttemptAnswer;
import com.jlpt.feature.assessment.Question;
import com.jlpt.feature.assessment.QuestionAssignment;
import com.jlpt.feature.assessment.TestAttempt;
import com.jlpt.feature.learning.Lesson;
import com.jlpt.feature.student.StudentUser;
import com.jlpt.feature.student.reading.dto.*;
import com.jlpt.feature.student.reading.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class StudentReadingServiceImpl implements StudentReadingService {

    private final StudentReadingLessonRepository lessonRepository;
    private final StudentReadingQuestionAssignmentRepository questionAssignmentRepository;
    private final StudentReadingAttemptRepository attemptRepository;
    private final StudentReadingAttemptAnswerRepository attemptAnswerRepository;
    private final StudentReadingUserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<ReadingLessonSummaryResponse> getLessonList(
            Lesson.LessonType type,
            StudentUser.JlptLevel level,
            Long studentId,
            int page,
            int size
    ) {
        if (type != Lesson.LessonType.READING && type != Lesson.LessonType.LISTENING) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid type");
        }

        updateLastActivityDate(studentId);

        Page<Lesson> lessonPage = lessonRepository.findByTypeAndLevelAndStatus(
                type,
                level,
                Lesson.LessonStatus.PUBLISHED,
                PageRequest.of(page, size)
        );

        List<Long> lessonIds = lessonPage.getContent().stream()
                .map(Lesson::getId)
                .collect(Collectors.toList());

        Set<Long> attemptedIds = attemptRepository.findAttemptedParentIds(
                studentId,
                TestAttempt.ParentType.LESSON,
                lessonIds,
                TestAttempt.AttemptType.valueOf(type.name())
        );

        return lessonPage.map(lesson -> {
            List<QuestionAssignment> assignments = questionAssignmentRepository
                    .findByParentTypeAndParentIdOrderByDisplayOrderAsc(QuestionAssignment.ParentType.LESSON, lesson.getId());
            
            return ReadingLessonSummaryResponse.builder()
                    .id(lesson.getId())
                    .title(lesson.getTitle())
                    .jlptLevel(lesson.getJlptLevel().name())
                    .questionCount(assignments.size())
                    .hasAttempted(attemptedIds.contains(lesson.getId()))
                    .build();
        });
    }

    @Override
    @Transactional(readOnly = true)
    public ReadingDetailResponse getReadingDetail(Long lessonId, Long studentId) {
        updateLastActivityDate(studentId);

        Lesson lesson = lessonRepository.findByIdAndTypeAndStatus(lessonId, Lesson.LessonType.READING, Lesson.LessonStatus.PUBLISHED)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "LESSON_NOT_FOUND"));

        List<QuestionAssignment> assignments = questionAssignmentRepository
                .findByParentTypeAndParentIdOrderByDisplayOrderAsc(QuestionAssignment.ParentType.LESSON, lessonId);

        List<ReadingQuestionResponse> questions = assignments.stream()
                .map(qa -> {
                    Question q = qa.getQuestion();
                    return ReadingQuestionResponse.builder()
                            .questionId(q.getId())
                            .content(q.getQuestionText())
                            .optionA(q.getOptionA())
                            .optionB(q.getOptionB())
                            .optionC(q.getOptionC())
                            .optionD(q.getOptionD())
                            .displayOrder(qa.getDisplayOrder())
                            .build();
                })
                .collect(Collectors.toList());

        return ReadingDetailResponse.builder()
                .id(lesson.getId())
                .title(lesson.getTitle())
                .jlptLevel(lesson.getJlptLevel().name())
                .passageText(lesson.getContentText())
                .questions(questions)
                .build();
    }

    @Override
    @Transactional
    public ReadingSubmitResponse submitReading(Long lessonId, Long studentId, ReadingSubmitRequest request) {
        updateLastActivityDate(studentId);

        if (!"reading".equalsIgnoreCase(request.getAttemptType())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "VALIDATION_FAILED: attemptType must be reading");
        }

        Lesson lesson = lessonRepository.findByIdAndStatus(lessonId, Lesson.LessonStatus.PUBLISHED)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "LESSON_NOT_FOUND"));

        List<QuestionAssignment> assignments = questionAssignmentRepository
                .findByParentTypeAndParentIdOrderByDisplayOrderAsc(QuestionAssignment.ParentType.LESSON, lessonId);

        Map<Long, QuestionAssignment> assignmentMap = assignments.stream()
                .collect(Collectors.toMap(qa -> qa.getQuestion().getId(), qa -> qa));

        if (assignments.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Lesson has no questions");
        }

        BigDecimal totalScore = BigDecimal.ZERO;
        BigDecimal maxScore = BigDecimal.valueOf(assignments.size()); // As per spec, maxScore is total_questions or sum of scores. Assuming 1 per question or sum of qa.getScore()
        int correctCount = 0;

        List<ReadingResultItemResponse> results = new ArrayList<>();
        List<AttemptAnswer> attemptAnswers = new ArrayList<>();

        StudentUser student = userRepository.findById(studentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));

        TestAttempt attempt = TestAttempt.builder()
                .student(student)
                .attemptType(TestAttempt.AttemptType.READING)
                .parentType(TestAttempt.ParentType.LESSON)
                .parentId(lessonId)
                .status(TestAttempt.AttemptStatus.SUBMITTED)
                .submittedAt(LocalDateTime.now())
                .startedAt(LocalDateTime.now())
                .build();
        
        attempt = attemptRepository.save(attempt);

        for (ReadingAnswerRequest ans : request.getAnswers()) {
            QuestionAssignment qa = assignmentMap.get(ans.getQuestionId());
            if (qa == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "VALIDATION_FAILED: Invalid questionId");
            }
            Question q = qa.getQuestion();
            boolean isCorrect = ans.getSelectedOption().equalsIgnoreCase(q.getCorrectOption());
            
            BigDecimal score = isCorrect ? qa.getScore() != null ? qa.getScore() : BigDecimal.ONE : BigDecimal.ZERO;
            if (isCorrect) {
                totalScore = totalScore.add(score);
                correctCount++;
            }

            AttemptAnswer attemptAnswer = AttemptAnswer.builder()
                    .attempt(attempt)
                    .question(q)
                    .selectedOption(ans.getSelectedOption())
                    .answerText(ans.getAnswerText())
                    .isCorrect(isCorrect)
                    .score(score)
                    .answeredAt(LocalDateTime.now())
                    .build();
            attemptAnswers.add(attemptAnswer);

            results.add(ReadingResultItemResponse.builder()
                    .questionId(q.getId())
                    .isCorrect(isCorrect)
                    .correctOption(q.getCorrectOption())
                    .explanation(q.getExplanation())
                    .build());
        }

        if (totalScore.compareTo(BigDecimal.ZERO) < 0 || totalScore.compareTo(maxScore) > 0) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "SCORE_INVARIANT: Invalid score");
        }

        attemptAnswerRepository.saveAll(attemptAnswers);

        attempt.setTotalScore(totalScore);
        attempt.setMaxScore(maxScore);
        attemptRepository.save(attempt);

        log.info("[ReadingService] {studentId: {}, lessonId: {}, attemptId: {}, score: {}}", studentId, lessonId, attempt.getId(), totalScore);

        return ReadingSubmitResponse.builder()
                .attemptId(attempt.getId())
                .score(totalScore)
                .maxScore(maxScore)
                .results(results)
                .build();
    }

    private void updateLastActivityDate(Long studentId) {
        userRepository.updateLastActivityDate(studentId, LocalDate.now());
    }
}
