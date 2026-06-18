/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.assessment;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.JsonNode;
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

@SpringBootTest
@AutoConfigureMockMvc
class AssessmentControllerIntegrationTest {

    private static final String EMAIL = "assessment-it@example.com";
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

    private Long assessmentId;
    private Long questionId;
    private String accessToken;

    @BeforeEach
    void setUp() throws Exception {
        studentUserRepository
                .findByEmail(EMAIL)
                .orElseGet(() -> studentUserRepository.save(StudentUser.builder()
                        .email(EMAIL)
                        .fullName("Assessment Tester")
                        .passwordHash(passwordEncoder.encode(PASSWORD))
                        .status(StudentUser.StudentStatus.ACTIVE)
                        .build()));

        Assessment assessment = assessmentRepository.save(Assessment.builder()
                .title("N5 Quiz IT")
                .assessmentType(Assessment.AssessmentType.QUIZ)
                .jlptLevel(StudentUser.JlptLevel.N5)
                .status(Kanji.ContentStatus.PUBLISHED)
                .durationMin(15)
                .passScore(10)
                .build());
        assessmentId = assessment.getId();

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
                .parentId(assessmentId)
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
        JsonNode json = objectMapper.readTree(body);
        accessToken = json.get("data").get("accessToken").asText();
    }

    @Test
    void listAssessments_returnsPublishedQuiz() throws Exception {
        mockMvc.perform(get("/api/assessments")
                        .header("Authorization", "Bearer " + accessToken)
                        .param("type", "quiz")
                        .param("level", "N5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.totalElements", org.hamcrest.Matchers.greaterThanOrEqualTo(1)));
    }

    @Test
    void startThenSubmitAssessment_correctAnswer_returnsFullScore() throws Exception {
        String startBody = mockMvc.perform(post("/api/assessments/" + assessmentId + "/start")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.attemptId").exists())
                .andExpect(
                        jsonPath("$.data.sections[0].questions[0].questionId").value(questionId))
                .andReturn()
                .getResponse()
                .getContentAsString();
        Long attemptId =
                objectMapper.readTree(startBody).get("data").get("attemptId").asLong();

        String submitPayload = String.format(
                "{\"attemptId\":%d,\"answers\":[{\"questionId\":%d,\"selectedOption\":\"B\"}]}", attemptId, questionId);

        mockMvc.perform(post("/api/assessments/" + assessmentId + "/submit")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(submitPayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.attemptId").value(attemptId))
                .andExpect(jsonPath("$.data.score").value(10))
                .andExpect(jsonPath("$.data.isPassed").value(true))
                .andExpect(jsonPath("$.data.results[0].isCorrect").value(true));
    }

    @Test
    void submitAssessment_withoutStarting_attemptNotFound_returns404() throws Exception {
        String submitPayload = String.format(
                "{\"attemptId\":999999,\"answers\":[{\"questionId\":%d,\"selectedOption\":\"B\"}]}", questionId);

        mockMvc.perform(post("/api/assessments/" + assessmentId + "/submit")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(submitPayload))
                .andExpect(status().isNotFound());
    }

    @Test
    void listAssessments_withoutToken_returnsForbidden() throws Exception {
        mockMvc.perform(get("/api/assessments").param("type", "quiz")).andExpect(status().isForbidden());
    }
}
