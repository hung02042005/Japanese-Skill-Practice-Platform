/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.assessment;

import com.jlpt.feature.assessment.dto.response.QuestionResponse;
import com.jlpt.feature.assessment.dto.response.SectionResponse;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Mapping/grading helpers dùng chung giữa {@link QuizService} (quiz) và {@link MockExamService} (exam). */
final class QuestionAssignmentSupport {

    private QuestionAssignmentSupport() {}

    static List<SectionResponse> groupBySection(List<QuestionAssignment> assignments) {
        Map<String, List<QuestionResponse>> bySection = new LinkedHashMap<>();
        for (QuestionAssignment qa : assignments) {
            String section = qa.getSectionName() != null ? qa.getSectionName() : "default";
            bySection.computeIfAbsent(section, k -> new ArrayList<>()).add(toQuestionResponse(qa));
        }
        return bySection.entrySet().stream()
                .map(e -> SectionResponse.builder()
                        .sectionName(e.getKey())
                        .questions(e.getValue())
                        .build())
                .toList();
    }

    static QuestionResponse toQuestionResponse(QuestionAssignment qa) {
        Question q = qa.getQuestion();
        return QuestionResponse.builder()
                .questionId(q.getId())
                .questionText(q.getQuestionText())
                .questionType(q.getQuestionType().getValue())
                .skill(q.getSkill() != null ? q.getSkill().getValue() : null)
                .optionA(q.getOptionA())
                .optionB(q.getOptionB())
                .optionC(q.getOptionC())
                .optionD(q.getOptionD())
                .audioUrl(q.getAudioUrl())
                .imageUrl(q.getImageUrl())
                .displayOrder(qa.getDisplayOrder())
                .build();
    }

    static boolean isCorrect(Question question, String selectedOption, String answerText) {
        if (question.getQuestionType() == Question.QuestionType.FILL_BLANK) {
            if (answerText == null || question.getCorrectAnswerText() == null) {
                return false;
            }
            return answerText
                    .trim()
                    .equalsIgnoreCase(question.getCorrectAnswerText().trim());
        }
        if (selectedOption == null || question.getCorrectOption() == null) {
            return false;
        }
        return selectedOption.equalsIgnoreCase(question.getCorrectOption());
    }
}
