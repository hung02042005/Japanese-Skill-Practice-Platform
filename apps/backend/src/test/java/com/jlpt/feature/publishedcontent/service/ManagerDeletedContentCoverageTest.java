/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.publishedcontent.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.jlpt.feature.assessment.Assessment;
import com.jlpt.feature.assessment.Question;
import com.jlpt.feature.learning.GrammarPoint;
import com.jlpt.feature.learning.Kanji;
import com.jlpt.feature.learning.Lesson;
import com.jlpt.feature.learning.VocabularyTopic;
import com.jlpt.feature.learning.VocabularyTopicRepository;
import com.jlpt.feature.publishedcontent.dto.DeletedContentResponse;
import com.jlpt.feature.publishedcontent.repository.ManagedAssessmentRepository;
import com.jlpt.feature.publishedcontent.repository.ManagedGrammarRepository;
import com.jlpt.feature.publishedcontent.repository.ManagedKanjiRepository;
import com.jlpt.feature.publishedcontent.repository.ManagedLessonRepository;
import com.jlpt.feature.publishedcontent.repository.ManagedQuestionRepository;
import com.jlpt.feature.staff.StaffUser;
import com.jlpt.feature.staff.StaffUserRepository;
import com.jlpt.feature.student.StudentUser.JlptLevel;
import com.jlpt.shared.exception.BusinessException;
import com.jlpt.shared.exception.ForbiddenException;
import com.jlpt.shared.exception.ResourceNotFoundException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

/**
 * Thùng rác nội dung của Manager — phủ NHÁNH: lọc theo từng loại (6 loại + "all" + null/blank),
 * ternary null-safe cho jlptLevel/updatedAt, và restore cho từng loại kèm các trường hợp thất bại.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ManagerDeletedContentCoverageTest {

    private static final String MANAGER_EMAIL = "manager@sakuji.com";
    private static final String STAFF_EMAIL = "staff@sakuji.com";

    @Mock
    private ManagedLessonRepository lessonRepository;

    @Mock
    private ManagedQuestionRepository questionRepository;

    @Mock
    private VocabularyTopicRepository topicRepository;

    @Mock
    private ManagedGrammarRepository grammarRepository;

    @Mock
    private ManagedKanjiRepository kanjiRepository;

    @Mock
    private ManagedAssessmentRepository assessmentRepository;

    @Mock
    private StaffUserRepository staffUserRepository;

    @InjectMocks
    private ManagerDeletedContentService service;

    private StaffUser manager;

    @BeforeEach
    void setUp() {
        manager = StaffUser.builder()
                .id(1L)
                .email(MANAGER_EMAIL)
                .staffRole(StaffUser.StaffRole.STAFF_MANAGER)
                .status(StaffUser.StaffStatus.ACTIVE)
                .build();
        when(staffUserRepository.findByEmail(MANAGER_EMAIL)).thenReturn(Optional.of(manager));
    }

    private void stubAllEmpty() {
        when(lessonRepository.findByStatusOrderByUpdatedAtDesc(any())).thenReturn(List.of());
        when(questionRepository.findByStatusOrderByUpdatedAtDesc(any())).thenReturn(List.of());
        when(topicRepository.findByStatusOrderByUpdatedAtDesc(any())).thenReturn(List.of());
        when(grammarRepository.findByStatusOrderByUpdatedAtDesc(any())).thenReturn(List.of());
        when(kanjiRepository.findByStatusOrderByUpdatedAtDesc(any())).thenReturn(List.of());
        when(assessmentRepository.findByStatusOrderByUpdatedAtDesc(any())).thenReturn(List.of());
    }

    // ── requireManager ───────────────────────────────────────────────────────

    @Test
    void listDeleted_byPlainStaff_throwsForbidden() {
        StaffUser staff = StaffUser.builder()
                .id(2L)
                .email(STAFF_EMAIL)
                .staffRole(StaffUser.StaffRole.STAFF)
                .status(StaffUser.StaffStatus.ACTIVE)
                .build();
        when(staffUserRepository.findByEmail(STAFF_EMAIL)).thenReturn(Optional.of(staff));

        assertThrows(ForbiddenException.class, () -> service.listDeleted(STAFF_EMAIL, "all"));
    }

    @Test
    void listDeleted_bySuspendedManager_throwsForbidden() {
        manager.setStatus(StaffUser.StaffStatus.SUSPENDED);

        assertThrows(ForbiddenException.class, () -> service.listDeleted(MANAGER_EMAIL, "all"));
    }

    @Test
    void listDeleted_unknownAccount_throwsForbidden() {
        when(staffUserRepository.findByEmail("ghost@sakuji.com")).thenReturn(Optional.empty());

        assertThrows(ForbiddenException.class, () -> service.listDeleted("ghost@sakuji.com", "all"));
    }

    @Test
    void restore_byPlainStaff_throwsForbidden() {
        StaffUser staff = StaffUser.builder()
                .id(2L)
                .email(STAFF_EMAIL)
                .staffRole(StaffUser.StaffRole.STAFF)
                .status(StaffUser.StaffStatus.ACTIVE)
                .build();
        when(staffUserRepository.findByEmail(STAFF_EMAIL)).thenReturn(Optional.of(staff));

        assertThrows(ForbiddenException.class, () -> service.restore(STAFF_EMAIL, "lesson", 1L));
    }

    // ── listDeleted: "all" và các giá trị tương đương ────────────────────────

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"all", "ALL", "   "})
    void listDeleted_allOrBlankType_queriesEverySource(String type) {
        stubAllEmpty();

        service.listDeleted(MANAGER_EMAIL, type);

        verify(lessonRepository).findByStatusOrderByUpdatedAtDesc(Lesson.LessonStatus.DELETED);
        verify(questionRepository).findByStatusOrderByUpdatedAtDesc(Question.ContentStatus.DELETED);
        verify(topicRepository).findByStatusOrderByUpdatedAtDesc(Kanji.ContentStatus.DELETED);
        verify(grammarRepository).findByStatusOrderByUpdatedAtDesc(Kanji.ContentStatus.DELETED);
        verify(kanjiRepository).findByStatusOrderByUpdatedAtDesc(Kanji.ContentStatus.DELETED);
        verify(assessmentRepository).findByStatusOrderByUpdatedAtDesc(Kanji.ContentStatus.DELETED);
    }

    @Test
    void listDeleted_unknownType_returnsEmptyWithoutQueryingAnything() {
        List<DeletedContentResponse> result = service.listDeleted(MANAGER_EMAIL, "podcast");

        assertTrue(result.isEmpty());
        verifyNoInteractions(
                lessonRepository,
                questionRepository,
                topicRepository,
                grammarRepository,
                kanjiRepository,
                assessmentRepository);
    }

    // ── listDeleted: lọc từng loại + map đầy đủ ──────────────────────────────

    @Test
    void listDeleted_lessonOnly_mapsTitleAndSkipsOtherSources() {
        Lesson lesson = Lesson.builder()
                .id(100L)
                .title("Bài 1")
                .jlptLevel(JlptLevel.N5)
                .status(Lesson.LessonStatus.DELETED)
                .updatedAt(LocalDateTime.of(2026, 7, 26, 10, 30, 0))
                .build();
        when(lessonRepository.findByStatusOrderByUpdatedAtDesc(Lesson.LessonStatus.DELETED))
                .thenReturn(List.of(lesson));

        List<DeletedContentResponse> result = service.listDeleted(MANAGER_EMAIL, "lesson");

        assertEquals(1, result.size());
        DeletedContentResponse row = result.get(0);
        assertEquals(100L, row.id());
        assertEquals("lesson", row.contentType());
        assertEquals("Bài 1", row.titleOrText());
        assertEquals("N5", row.jlptLevel());
        assertEquals("2026-07-26 10:30:00", row.updatedAt());
        verifyNoInteractions(questionRepository, topicRepository, grammarRepository, assessmentRepository);
    }

    @Test
    void listDeleted_questionOnly_mapsQuestionText() {
        Question question = Question.builder()
                .id(101L)
                .questionText("Câu hỏi 1")
                .jlptLevel(JlptLevel.N4)
                .status(Question.ContentStatus.DELETED)
                .updatedAt(LocalDateTime.of(2026, 7, 26, 11, 0, 0))
                .build();
        when(questionRepository.findByStatusOrderByUpdatedAtDesc(Question.ContentStatus.DELETED))
                .thenReturn(List.of(question));

        DeletedContentResponse row =
                service.listDeleted(MANAGER_EMAIL, "question").get(0);

        assertEquals("question", row.contentType());
        assertEquals("Câu hỏi 1", row.titleOrText());
        assertEquals("N4", row.jlptLevel());
    }

    @Test
    void listDeleted_vocabularyOnly_combinesViAndJaTitle() {
        VocabularyTopic topic = VocabularyTopic.builder()
                .id(102L)
                .titleVi("Gia đình")
                .titleJa("家族")
                .jlptLevel(JlptLevel.N5)
                .status(Kanji.ContentStatus.DELETED)
                .updatedAt(LocalDateTime.now())
                .build();
        when(topicRepository.findByStatusOrderByUpdatedAtDesc(Kanji.ContentStatus.DELETED))
                .thenReturn(List.of(topic));

        DeletedContentResponse row =
                service.listDeleted(MANAGER_EMAIL, "vocabulary").get(0);

        assertEquals("vocabulary", row.contentType());
        assertEquals("Gia đình (家族)", row.titleOrText());
    }

    @Test
    void listDeleted_grammarOnly_mapsTitle() {
        GrammarPoint grammar = GrammarPoint.builder()
                .id(103L)
                .title("V-てから")
                .jlptLevel(JlptLevel.N5)
                .status(Kanji.ContentStatus.DELETED)
                .updatedAt(LocalDateTime.now())
                .build();
        when(grammarRepository.findByStatusOrderByUpdatedAtDesc(Kanji.ContentStatus.DELETED))
                .thenReturn(List.of(grammar));

        DeletedContentResponse row =
                service.listDeleted(MANAGER_EMAIL, "grammar").get(0);

        assertEquals("grammar", row.contentType());
        assertEquals("V-てから", row.titleOrText());
    }

    @Test
    void listDeleted_kanjiOnly_combinesCharacterAndMeaning() {
        Kanji kanji = Kanji.builder()
                .id(104L)
                .characterValue("日")
                .meaning("Mặt trời")
                .jlptLevel(JlptLevel.N5)
                .status(Kanji.ContentStatus.DELETED)
                .updatedAt(LocalDateTime.now())
                .build();
        when(kanjiRepository.findByStatusOrderByUpdatedAtDesc(Kanji.ContentStatus.DELETED))
                .thenReturn(List.of(kanji));

        DeletedContentResponse row = service.listDeleted(MANAGER_EMAIL, "kanji").get(0);

        assertEquals("kanji", row.contentType());
        assertEquals("日 - Mặt trời", row.titleOrText());
    }

    @Test
    void listDeleted_assessmentOnly_mapsTitle() {
        Assessment assessment = Assessment.builder()
                .id(105L)
                .title("Đề thi thử N5")
                .jlptLevel(JlptLevel.N5)
                .status(Kanji.ContentStatus.DELETED)
                .updatedAt(LocalDateTime.now())
                .build();
        when(assessmentRepository.findByStatusOrderByUpdatedAtDesc(Kanji.ContentStatus.DELETED))
                .thenReturn(List.of(assessment));

        DeletedContentResponse row =
                service.listDeleted(MANAGER_EMAIL, "assessment").get(0);

        assertEquals("assessment", row.contentType());
        assertEquals("Đề thi thử N5", row.titleOrText());
    }

    // ── listDeleted: nhánh null của jlptLevel / updatedAt ────────────────────

    @Test
    void listDeleted_entitiesWithoutLevelOrTimestamp_mapToNulls() {
        Lesson lesson = Lesson.builder()
                .id(100L)
                .title("Bài 1")
                .status(Lesson.LessonStatus.DELETED)
                .build();
        lesson.setJlptLevel(null);
        lesson.setUpdatedAt(null);
        Question question = Question.builder()
                .id(101L)
                .questionText("Câu hỏi")
                .status(Question.ContentStatus.DELETED)
                .build();
        question.setJlptLevel(null);
        question.setUpdatedAt(null);
        VocabularyTopic topic = VocabularyTopic.builder()
                .id(102L)
                .titleVi("A")
                .titleJa("B")
                .status(Kanji.ContentStatus.DELETED)
                .build();
        topic.setJlptLevel(null);
        topic.setUpdatedAt(null);
        GrammarPoint grammar = GrammarPoint.builder()
                .id(103L)
                .title("G")
                .status(Kanji.ContentStatus.DELETED)
                .build();
        grammar.setJlptLevel(null);
        grammar.setUpdatedAt(null);
        Kanji kanji = Kanji.builder()
                .id(104L)
                .characterValue("日")
                .meaning("M")
                .status(Kanji.ContentStatus.DELETED)
                .build();
        kanji.setJlptLevel(null);
        kanji.setUpdatedAt(null);
        Assessment assessment = Assessment.builder()
                .id(105L)
                .title("A")
                .status(Kanji.ContentStatus.DELETED)
                .build();
        assessment.setJlptLevel(null);
        assessment.setUpdatedAt(null);

        when(lessonRepository.findByStatusOrderByUpdatedAtDesc(any())).thenReturn(List.of(lesson));
        when(questionRepository.findByStatusOrderByUpdatedAtDesc(any())).thenReturn(List.of(question));
        when(topicRepository.findByStatusOrderByUpdatedAtDesc(any())).thenReturn(List.of(topic));
        when(grammarRepository.findByStatusOrderByUpdatedAtDesc(any())).thenReturn(List.of(grammar));
        when(kanjiRepository.findByStatusOrderByUpdatedAtDesc(any())).thenReturn(List.of(kanji));
        when(assessmentRepository.findByStatusOrderByUpdatedAtDesc(any())).thenReturn(List.of(assessment));

        List<DeletedContentResponse> result = service.listDeleted(MANAGER_EMAIL, "all");

        assertEquals(6, result.size());
        assertTrue(result.stream().allMatch(r -> r.jlptLevel() == null));
        assertTrue(result.stream().allMatch(r -> r.updatedAt() == null));
    }

    // ── restore: từng loại ───────────────────────────────────────────────────

    @Test
    void restore_lesson_transitionsDeletedToPublished() {
        when(lessonRepository.transition(
                        eq(1L),
                        eq(Lesson.LessonStatus.DELETED),
                        eq(Lesson.LessonStatus.PUBLISHED),
                        any(LocalDateTime.class)))
                .thenReturn(1);

        assertDoesNotThrow(() -> service.restore(MANAGER_EMAIL, "LESSON", 1L));
    }

    @Test
    void restore_question_transitionsDeletedToPublished() {
        when(questionRepository.transition(
                        eq(1L),
                        eq(Question.ContentStatus.DELETED),
                        eq(Question.ContentStatus.PUBLISHED),
                        any(LocalDateTime.class)))
                .thenReturn(1);

        assertDoesNotThrow(() -> service.restore(MANAGER_EMAIL, "question", 1L));
    }

    @Test
    void restore_grammar_transitionsDeletedToPublished() {
        when(grammarRepository.transition(
                        eq(1L),
                        eq(Kanji.ContentStatus.DELETED),
                        eq(Kanji.ContentStatus.PUBLISHED),
                        any(LocalDateTime.class)))
                .thenReturn(1);

        assertDoesNotThrow(() -> service.restore(MANAGER_EMAIL, "grammar", 1L));
    }

    @Test
    void restore_kanji_transitionsDeletedToPublished() {
        when(kanjiRepository.transition(
                        eq(1L),
                        eq(Kanji.ContentStatus.DELETED),
                        eq(Kanji.ContentStatus.PUBLISHED),
                        any(LocalDateTime.class)))
                .thenReturn(1);

        assertDoesNotThrow(() -> service.restore(MANAGER_EMAIL, "kanji", 1L));
    }

    @Test
    void restore_assessment_transitionsDeletedToPublished() {
        when(assessmentRepository.transition(
                        eq(1L),
                        eq(Kanji.ContentStatus.DELETED),
                        eq(Kanji.ContentStatus.PUBLISHED),
                        any(LocalDateTime.class)))
                .thenReturn(1);

        assertDoesNotThrow(() -> service.restore(MANAGER_EMAIL, "assessment", 1L));
    }

    // ── restore: vocabulary đi đường riêng (đọc entity, không dùng transition) ─

    @Test
    void restore_vocabulary_setsStatusPublished() {
        VocabularyTopic topic = VocabularyTopic.builder()
                .id(1L)
                .status(Kanji.ContentStatus.DELETED)
                .build();
        when(topicRepository.findById(1L)).thenReturn(Optional.of(topic));

        service.restore(MANAGER_EMAIL, "vocabulary", 1L);

        assertEquals(Kanji.ContentStatus.PUBLISHED, topic.getStatus());
        verify(topicRepository).save(topic);
    }

    @Test
    void restore_vocabularyNotDeleted_throwsRestoreFailed() {
        VocabularyTopic topic = VocabularyTopic.builder()
                .id(1L)
                .status(Kanji.ContentStatus.PUBLISHED)
                .build();
        when(topicRepository.findById(1L)).thenReturn(Optional.of(topic));

        BusinessException ex =
                assertThrows(BusinessException.class, () -> service.restore(MANAGER_EMAIL, "vocabulary", 1L));
        assertEquals("RESTORE_FAILED", ex.getErrorCode());
        verify(topicRepository, never()).save(any());
    }

    @Test
    void restore_vocabularyMissing_throwsNotFound() {
        when(topicRepository.findById(404L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.restore(MANAGER_EMAIL, "vocabulary", 404L));
    }

    // ── restore: thất bại chung ──────────────────────────────────────────────

    @Test
    void restore_transitionAffectedNoRow_throwsRestoreFailed() {
        when(lessonRepository.transition(any(), any(), any(), any())).thenReturn(0);

        BusinessException ex =
                assertThrows(BusinessException.class, () -> service.restore(MANAGER_EMAIL, "lesson", 1L));
        assertEquals(400, ex.getStatus());
        assertEquals("RESTORE_FAILED", ex.getErrorCode());
    }

    @Test
    void restore_unknownType_throwsInvalidType() {
        BusinessException ex =
                assertThrows(BusinessException.class, () -> service.restore(MANAGER_EMAIL, "podcast", 1L));

        assertEquals(400, ex.getStatus());
        assertEquals("INVALID_TYPE", ex.getErrorCode());
    }
}
