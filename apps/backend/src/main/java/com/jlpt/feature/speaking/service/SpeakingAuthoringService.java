/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.speaking.service;

import com.jlpt.feature.speaking.repository.SpeakingQuestionRepository;

import com.jlpt.feature.speaking.entity.SpeakingQuestion;

import com.jlpt.feature.speaking.exception.SpeakingBusinessException;

import com.jlpt.feature.learning.Lesson;
import com.jlpt.feature.learning.LessonRepository;
import com.jlpt.feature.speaking.dto.SpeakingLessonCreateRequest;
import com.jlpt.feature.speaking.dto.SpeakingLessonDetailResponse;
import com.jlpt.feature.speaking.dto.SpeakingLessonMutationResponse;
import com.jlpt.feature.speaking.dto.SpeakingQuestionDto;
import com.jlpt.feature.staff.StaffUser;
import com.jlpt.feature.staff.StaffUserRepository;
import com.jlpt.feature.student.StudentUser;
import com.jlpt.shared.exception.ForbiddenException;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
@Slf4j
public class SpeakingAuthoringService {

    private final LessonRepository lessonRepository;
    private final SpeakingQuestionRepository questionRepository;
    private final StaffUserRepository staffUserRepository;

    @Transactional
    public SpeakingLessonMutationResponse create(SpeakingLessonCreateRequest request, String staffEmail) {
        StaffUser staff = resolveActiveStaff(staffEmail);
        validateQuestions(request.getQuestions());
        List<SpeakingQuestionDto> questions = orderedQuestions(request.getQuestions());

        Lesson lesson = Lesson.builder()
                .lessonType(Lesson.LessonType.SPEAKING)
                .title(request.getTitle().trim())
                .jlptLevel(parseLevel(request.getJlptLevel()))
                .contentText(questions.get(0).getPromptText().trim())
                .audioUrl(trimToNull(questions.get(0).getSampleAudioUrl()))
                .status(Lesson.LessonStatus.DRAFT)
                .createdBy(staff)
                .build();
        lesson = lessonRepository.save(lesson);
        saveQuestions(lesson, questions);

        log.info("[Speaking] Staff {} created speaking lesson {}", staff.getId(), lesson.getId());
        return mutationResponse(lesson);
    }

    @Transactional
    public SpeakingLessonDetailResponse update(
            Long lessonId, SpeakingLessonCreateRequest request, String staffEmail) {
        StaffUser staff = resolveActiveStaff(staffEmail);
        Lesson lesson = findOwnedSpeakingLesson(lessonId, staff);
        guardEditable(lesson);
        validateQuestions(request.getQuestions());
        List<SpeakingQuestionDto> questions = orderedQuestions(request.getQuestions());

        lesson.setTitle(request.getTitle().trim());
        lesson.setJlptLevel(parseLevel(request.getJlptLevel()));
        lesson.setContentText(questions.get(0).getPromptText().trim());
        lesson.setAudioUrl(trimToNull(questions.get(0).getSampleAudioUrl()));
        questionRepository.deleteByLesson_Id(lessonId);
        questionRepository.flush();
        saveQuestions(lesson, questions);

        log.info("[Speaking] Staff {} updated speaking lesson {}", staff.getId(), lessonId);
        return toDetail(lesson, questionRepository.findByLesson_IdOrderByDisplayOrderAsc(lessonId));
    }

    @Transactional(readOnly = true)
    public SpeakingLessonDetailResponse getOwnDetail(Long lessonId, String staffEmail) {
        StaffUser staff = resolveActiveStaff(staffEmail);
        Lesson lesson = findOwnedSpeakingLesson(lessonId, staff);
        return toDetail(lesson, questionRepository.findByLesson_IdOrderByDisplayOrderAsc(lessonId));
    }

    @Transactional
    public SpeakingLessonMutationResponse submitForReview(Long lessonId, String staffEmail) {
        StaffUser staff = resolveActiveStaff(staffEmail);
        Lesson lesson = findOwnedSpeakingLesson(lessonId, staff);
        guardEditable(lesson);
        List<SpeakingQuestion> questions = questionRepository.findByLesson_IdOrderByDisplayOrderAsc(lessonId);
        if (questions.isEmpty() && !StringUtils.hasText(lesson.getContentText())) {
            throw SpeakingBusinessException.validationFailed();
        }
        lesson.setStatus(Lesson.LessonStatus.PENDING_REVIEW);
        log.info("[Speaking] Staff {} submitted speaking lesson {} for review", staff.getId(), lessonId);
        return mutationResponse(lesson);
    }

    private Lesson findOwnedSpeakingLesson(Long lessonId, StaffUser staff) {
        return lessonRepository
                .findByIdAndStatusNot(lessonId, Lesson.LessonStatus.DELETED)
                .filter(lesson -> lesson.getLessonType() == Lesson.LessonType.SPEAKING)
                .filter(lesson -> lesson.getCreatedBy() != null && lesson.getCreatedBy().getId().equals(staff.getId()))
                .orElseThrow(SpeakingBusinessException::contentNotFound);
    }

    private StaffUser resolveActiveStaff(String email) {
        StaffUser staff = staffUserRepository
                .findByEmail(email)
                .orElseThrow(() -> new ForbiddenException("Tài khoản nhân viên không tồn tại"));
        if (staff.getStatus() != StaffUser.StaffStatus.ACTIVE) {
            throw new ForbiddenException("Tài khoản nhân viên không hoạt động");
        }
        return staff;
    }

    private StudentUser.JlptLevel parseLevel(String level) {
        try {
            return StudentUser.JlptLevel.valueOf(level.trim().toUpperCase());
        } catch (IllegalArgumentException | NullPointerException exception) {
            throw SpeakingBusinessException.invalidLevel();
        }
    }

    private void guardEditable(Lesson lesson) {
        if (lesson.getStatus() != Lesson.LessonStatus.DRAFT
                && lesson.getStatus() != Lesson.LessonStatus.REJECTED) {
            throw SpeakingBusinessException.invalidStateTransition();
        }
    }

    private void validateQuestions(List<SpeakingQuestionDto> questions) {
        if (questions == null
                || questions.isEmpty()
                || questions.stream().anyMatch(question -> !StringUtils.hasText(question.getPromptText()))) {
            throw SpeakingBusinessException.validationFailed();
        }
    }

    private List<SpeakingQuestionDto> orderedQuestions(List<SpeakingQuestionDto> questions) {
        return questions.stream()
                .sorted(Comparator.comparing(
                        question -> question.getDisplayOrder() == null ? 0 : question.getDisplayOrder()))
                .toList();
    }

    private void saveQuestions(Lesson lesson, List<SpeakingQuestionDto> questions) {
        for (int index = 0; index < questions.size(); index++) {
            SpeakingQuestionDto question = questions.get(index);
            int displayOrder = question.getDisplayOrder() == null ? index : question.getDisplayOrder();
            questionRepository.save(SpeakingQuestion.builder()
                    .lesson(lesson)
                    .promptText(question.getPromptText().trim())
                    .instruction(trimToNull(question.getInstruction()))
                    .sampleAudioUrl(trimToNull(question.getSampleAudioUrl()))
                    .displayOrder(displayOrder)
                    .build());
        }
    }

    private SpeakingLessonDetailResponse toDetail(Lesson lesson, List<SpeakingQuestion> questions) {
        List<SpeakingQuestionDto> responseQuestions = questions.isEmpty() && StringUtils.hasText(lesson.getContentText())
                ? List.of(SpeakingQuestionDto.builder()
                        .promptText(lesson.getContentText())
                        .sampleAudioUrl(lesson.getAudioUrl())
                        .displayOrder(0)
                        .build())
                : questions.stream().map(this::toQuestionDto).toList();
        return SpeakingLessonDetailResponse.builder()
                .lessonId(lesson.getId())
                .title(lesson.getTitle())
                .jlptLevel(lesson.getJlptLevel().name())
                .status(lesson.getStatus().getValue())
                .createdAt(lesson.getCreatedAt())
                .questions(responseQuestions)
                .build();
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

    private SpeakingLessonMutationResponse mutationResponse(Lesson lesson) {
        return SpeakingLessonMutationResponse.builder()
                .lessonId(lesson.getId())
                .status(lesson.getStatus().getValue())
                .build();
    }

    private String trimToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
