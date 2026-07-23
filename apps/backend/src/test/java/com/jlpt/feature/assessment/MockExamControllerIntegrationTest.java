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
class MockExamControllerIntegrationTest {

    private static final String EMAIL = "mockexam-it@example.com";
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

    private Long examId;
    private Long languageQuestionId;
    private Long vocabQuestionId;
    private String accessToken;

    @BeforeEach
    void setUp() throws Exception {
        studentUserRepository
                .findByEmail(EMAIL)
                .orElseGet(() -> studentUserRepository.save(StudentUser.builder()
                        .email(EMAIL)
                        .fullName("Mock Exam Tester")
                        .passwordHash(passwordEncoder.encode(PASSWORD))
                        .status(StudentUser.StudentStatus.ACTIVE)
                        .build()));

        Assessment exam = assessmentRepository.save(Assessment.builder()
                .title("N3 Mock Exam IT " + System.nanoTime())
                .assessmentType(Assessment.AssessmentType.EXAM)
                .jlptLevel(StudentUser.JlptLevel.N3)
                .status(Kanji.ContentStatus.PUBLISHED)
                .durationMin(60)
                .passScore(20)
                .totalScore(40)
                .build());
        examId = exam.getId();

        Question languageQuestion = questionRepository.save(Question.builder()
                .questionText("言語知識の問題")
                .questionType(Question.QuestionType.MULTIPLE_CHOICE)
                .skill(Question.Skill.GRAMMAR)
                .jlptLevel(StudentUser.JlptLevel.N3)
                .optionA("1")
                .optionB("2")
                .optionC("3")
                .optionD("4")
                .correctOption("B")
                .explanation("Vì B đúng")
                .status(Question.ContentStatus.PUBLISHED)
                .build());
        languageQuestionId = languageQuestion.getId();

        Question vocabQuestion = questionRepository.save(Question.builder()
                .questionText("語彙の問題")
                .questionType(Question.QuestionType.MULTIPLE_CHOICE)
                .skill(Question.Skill.VOCABULARY)
                .jlptLevel(StudentUser.JlptLevel.N3)
                .optionA("1")
                .optionB("2")
                .optionC("3")
                .optionD("4")
                .correctOption("C")
                .status(Question.ContentStatus.PUBLISHED)
                .build());
        vocabQuestionId = vocabQuestion.getId();

        questionAssignmentRepository.save(QuestionAssignment.builder()
                .parentType(QuestionAssignment.ParentType.ASSESSMENT)
                .parentId(examId)
                .question(languageQuestion)
                .sectionName("language_knowledge")
                .score(BigDecimal.valueOf(20))
                .displayOrder(1)
                .build());
        questionAssignmentRepository.save(QuestionAssignment.builder()
                .parentType(QuestionAssignment.ParentType.ASSESSMENT)
                .parentId(examId)
                .question(vocabQuestion)
                .sectionName("vocabulary")
                .score(BigDecimal.valueOf(20))
                .displayOrder(2)
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
    void startExam_returnsSectionsWithoutCorrectOption() throws Exception {
        mockMvc.perform(post("/api/assessments/" + examId + "/start").header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.attemptId").exists())
                .andExpect(jsonPath("$.data.expiresAt").exists())
                .andExpect(jsonPath("$.data.sections").isArray())
                .andExpect(jsonPath("$.data.sections[*].questions[*].correctOption")
                        .doesNotExist());
    }

    @Test
    void startThenSubmitExam_manualSubmit_returnsSectionScores() throws Exception {
        String startBody = mockMvc.perform(
                        post("/api/assessments/" + examId + "/start").header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        Long attemptId =
                objectMapper.readTree(startBody).get("data").get("attemptId").asLong();

        String submitPayload = String.format(
                "{\"attemptId\":%d,\"isAutoSubmit\":false,\"answers\":["
                        + "{\"questionId\":%d,\"selectedOption\":\"B\"},"
                        + "{\"questionId\":%d,\"selectedOption\":\"A\"}]}",
                attemptId, languageQuestionId, vocabQuestionId);

        mockMvc.perform(post("/api/assessments/" + examId + "/submit")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(submitPayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalScore").value(20))
                .andExpect(jsonPath("$.data.maxScore").value(40))
                .andExpect(jsonPath("$.data.isPassed").value(true))
                .andExpect(jsonPath("$.data.sectionScores.languageKnowledge").value(20))
                .andExpect(jsonPath("$.data.sectionScores.reading").value(0));

        mockMvc.perform(get("/api/test-attempts/" + attemptId + "/review")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.results[0].correctOption").exists());

        mockMvc.perform(get("/api/test-attempts")
                        .header("Authorization", "Bearer " + accessToken)
                        .param("type", "exam"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].attemptId").value(attemptId));
    }

    @Test
    void submitExam_alreadySubmitted_returns422() throws Exception {
        String startBody = mockMvc.perform(
                        post("/api/assessments/" + examId + "/start").header("Authorization", "Bearer " + accessToken))
                .andReturn()
                .getResponse()
                .getContentAsString();
        Long attemptId =
                objectMapper.readTree(startBody).get("data").get("attemptId").asLong();
        String submitPayload = String.format(
                "{\"attemptId\":%d,\"isAutoSubmit\":false,\"answers\":[{\"questionId\":%d,\"selectedOption\":\"B\"}]}",
                attemptId, languageQuestionId);

        mockMvc.perform(post("/api/assessments/" + examId + "/submit")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(submitPayload))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/assessments/" + examId + "/submit")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(submitPayload))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void getReview_whileInProgress_returns400() throws Exception {
        String startBody = mockMvc.perform(
                        post("/api/assessments/" + examId + "/start").header("Authorization", "Bearer " + accessToken))
                .andReturn()
                .getResponse()
                .getContentAsString();
        Long attemptId =
                objectMapper.readTree(startBody).get("data").get("attemptId").asLong();

        mockMvc.perform(get("/api/test-attempts/" + attemptId + "/review")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isBadRequest());
    }
}
