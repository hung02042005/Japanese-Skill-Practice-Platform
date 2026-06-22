/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.assessment;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jlpt.feature.auth.dto.request.LoginRequest;
import com.jlpt.feature.learning.Kanji;
import com.jlpt.feature.student.StudentUser;
import com.jlpt.feature.student.StudentUserRepository;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Kiểm thử adapter layer — đảm bảo contract cũ của studentService.js
 * (getQuizList/getQuizQuestions/submitPracticeQuiz) hoạt động đúng, dùng chung
 * business logic với {@link AssessmentControllerIntegrationTest} qua AssessmentService.
 */
@SpringBootTest
@AutoConfigureMockMvc
class LegacyQuizControllerIntegrationTest {

    private static final String EMAIL = "legacy-quiz-it@example.com";
    private static final String PASSWORD = "password123";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private StudentUserRepository studentUserRepository;

    @Autowired
    private AssessmentRepository assessmentRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private QuestionAssignmentRepository questionAssignmentRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Long quizId;
    private Long questionId;
    private String accessToken;

    @BeforeEach
    void setUp() throws Exception {
        studentUserRepository
                .findByEmail(EMAIL)
                .orElseGet(() -> studentUserRepository.save(StudentUser.builder()
                        .email(EMAIL)
                        .fullName("Legacy Quiz Tester")
                        .passwordHash(passwordEncoder.encode(PASSWORD))
                        .status(StudentUser.StudentStatus.ACTIVE)
                        .build()));

        Assessment assessment = assessmentRepository.save(Assessment.builder()
                .title("N5 Legacy Quiz")
                .assessmentType(Assessment.AssessmentType.QUIZ)
                .jlptLevel(StudentUser.JlptLevel.N5)
                .topic("grammar")
                .status(Kanji.ContentStatus.PUBLISHED)
                .durationMin(15)
                .passScore(10)
                .build());
        quizId = assessment.getId();

        Question question = questionRepository.save(Question.builder()
                .questionText("１＋１＝？")
                .questionType(Question.QuestionType.MULTIPLE_CHOICE)
                .skill(Question.Skill.GRAMMAR)
                .jlptLevel(StudentUser.JlptLevel.N5)
                .optionA("1")
                .optionB("2")
                .optionC("3")
                .optionD("4")
                .correctOption("B")
                .status(Question.ContentStatus.PUBLISHED)
                .build());
        questionId = question.getId();

        questionAssignmentRepository.save(QuestionAssignment.builder()
                .parentType(QuestionAssignment.ParentType.ASSESSMENT)
                .parentId(quizId)
                .question(question)
                .sectionName("default")
                .score(BigDecimal.TEN)
                .displayOrder(1)
                .build());

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail(EMAIL);
        loginRequest.setPassword(PASSWORD);

        String body = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        accessToken = objectMapper.readTree(body).get("data").get("accessToken").asText();
    }

    @Test
    void listQuizzes_returnsContentArray() throws Exception {
        mockMvc.perform(get("/api/quizzes")
                        .header("Authorization", "Bearer " + accessToken)
                        .param("level", "N5")
                        .param("skill", "grammar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.totalElements", org.hamcrest.Matchers.greaterThanOrEqualTo(1)));
    }

    @Test
    void getQuizQuestions_doesNotLeakCorrectOption() throws Exception {
        mockMvc.perform(get("/api/quizzes/" + quizId + "/questions").header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].questionId").value(questionId))
                .andExpect(jsonPath("$.data[0].options").isArray())
                .andExpect(jsonPath("$.data[0].correctOptionId").doesNotExist());
    }

    @Test
    void submitQuizAttempt_correctOption_returnsFullScoreAndPct() throws Exception {
        String payload = String.format(
                "{\"quizId\":%d,\"answers\":[{\"questionId\":%d,\"selectedOptionId\":2}]}", quizId, questionId);

        mockMvc.perform(post("/api/quiz-attempts")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.score").value(1))
                .andExpect(jsonPath("$.data.totalQuestions").value(1))
                .andExpect(jsonPath("$.data.scorePct").value(100))
                .andExpect(jsonPath("$.data.results[0].isCorrect").value(true))
                .andExpect(jsonPath("$.data.results[0].selectedOptionId").value(2))
                .andExpect(jsonPath("$.data.results[0].correctOptionId").value(2));
    }

    @Test
    void submitQuizAttempt_invalidOptionId_returnsBadRequest() throws Exception {
        String payload = String.format(
                "{\"quizId\":%d,\"answers\":[{\"questionId\":%d,\"selectedOptionId\":9}]}", quizId, questionId);

        mockMvc.perform(post("/api/quiz-attempts")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest());
    }
}
