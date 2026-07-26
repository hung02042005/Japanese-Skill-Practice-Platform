/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.contentreview.handler;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.jlpt.feature.assessment.Assessment;
import com.jlpt.feature.assessment.Question;
import com.jlpt.feature.assessment.QuestionAssignmentRepository;
import com.jlpt.feature.contentreview.model.ContentSnapshot;
import com.jlpt.feature.contentreview.model.ContentType;
import com.jlpt.feature.contentreview.repository.*;
import com.jlpt.feature.learning.*;
import com.jlpt.feature.speaking.repository.SpeakingQuestionRepository;
import com.jlpt.feature.staff.StaffUser;
import com.jlpt.feature.student.StudentUser.JlptLevel;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ContentReviewHandlersTest {

    @Mock
    private ReviewAssessmentRepository assessmentRepository;

    @Mock
    private QuestionAssignmentRepository assignmentRepository;

    @Mock
    private ReviewGrammarRepository grammarRepository;

    @Mock
    private ReviewKanjiRepository kanjiRepository;

    @Mock
    private ReviewLessonRepository lessonRepository;

    @Mock
    private ReviewQuestionRepository questionRepository;

    @Mock
    private SpeakingQuestionRepository speakingQuestionRepository;

    @Mock
    private ReviewVocabularyRepository vocabularyRepository;

    private StaffUser manager;

    @BeforeEach
    void setUp() {
        manager = StaffUser.builder().id(10L).build();
    }

    @Test
    void assessmentHandler_tests() {
        AssessmentContentHandler handler = new AssessmentContentHandler(assessmentRepository, assignmentRepository);
        assertEquals(ContentType.ASSESSMENT, handler.type());
        assertEquals("assessments", handler.tableName());

        Assessment assessment = Assessment.builder()
                .id(1L)
                .title("Quiz")
                .jlptLevel(JlptLevel.N5)
                .build();
        when(assessmentRepository.findPending(Kanji.ContentStatus.PENDING_REVIEW))
                .thenReturn(List.of(assessment));

        List<ContentSnapshot> pending = handler.findPending(null);
        assertEquals(1, pending.size());
        assertEquals(1L, pending.get(0).getContentId());

        when(assessmentRepository.findActiveById(1L, Kanji.ContentStatus.DELETED))
                .thenReturn(Optional.of(assessment));
        assertTrue(handler.findActiveById(1L).isPresent());
    }

    @Test
    void grammarHandler_tests() {
        GrammarContentHandler handler = new GrammarContentHandler(grammarRepository);
        assertEquals(ContentType.GRAMMAR, handler.type());
        assertEquals("grammar_points", handler.tableName());

        GrammarPoint grammar = GrammarPoint.builder().id(2L).title("Grammar").build();
        when(grammarRepository.findPending(Kanji.ContentStatus.PENDING_REVIEW)).thenReturn(List.of(grammar));

        List<ContentSnapshot> pending = handler.findPending(null);
        assertEquals(1, pending.size());

        when(grammarRepository.findActiveById(2L, Kanji.ContentStatus.DELETED)).thenReturn(Optional.of(grammar));
        assertTrue(handler.findActiveById(2L).isPresent());
    }

    @Test
    void kanjiHandler_tests() {
        KanjiContentHandler handler = new KanjiContentHandler(kanjiRepository);
        assertEquals(ContentType.KANJI, handler.type());
        assertEquals("kanji", handler.tableName());

        Kanji kanji = Kanji.builder().id(3L).characterValue("日").build();
        when(kanjiRepository.findPending(Kanji.ContentStatus.PENDING_REVIEW)).thenReturn(List.of(kanji));

        List<ContentSnapshot> pending = handler.findPending(null);
        assertEquals(1, pending.size());
    }

    @Test
    void lessonHandler_tests() {
        LessonContentHandler handler = new LessonContentHandler(lessonRepository);
        assertEquals(ContentType.LESSON, handler.type());
        assertEquals("lessons", handler.tableName());

        Lesson lesson = Lesson.builder()
                .id(4L)
                .title("Lesson")
                .status(Lesson.LessonStatus.PENDING_REVIEW)
                .build();
        when(lessonRepository.findPendingExcludingType(Lesson.LessonStatus.PENDING_REVIEW, Lesson.LessonType.SPEAKING))
                .thenReturn(List.of(lesson));

        List<ContentSnapshot> pending = handler.findPending(null);
        assertEquals(1, pending.size());
    }

    @Test
    void questionHandler_tests() {
        QuestionContentHandler handler = new QuestionContentHandler(questionRepository);
        assertEquals(ContentType.QUESTION, handler.type());
        assertEquals("questions", handler.tableName());

        Question question = Question.builder().id(5L).questionText("Text").build();
        when(questionRepository.findPending(Question.ContentStatus.PENDING_REVIEW))
                .thenReturn(List.of(question));

        List<ContentSnapshot> pending = handler.findPending(null);
        assertEquals(1, pending.size());
    }

    @Test
    void speakingHandler_tests() {
        SpeakingContentHandler handler = new SpeakingContentHandler(lessonRepository, speakingQuestionRepository);
        assertEquals(ContentType.SPEAKING, handler.type());
        assertEquals("lessons", handler.tableName());

        Lesson speakingLesson = Lesson.builder()
                .id(6L)
                .title("Speaking")
                .lessonType(Lesson.LessonType.SPEAKING)
                .build();
        when(lessonRepository.findPendingByType(Lesson.LessonStatus.PENDING_REVIEW, Lesson.LessonType.SPEAKING))
                .thenReturn(List.of(speakingLesson));

        List<ContentSnapshot> pending = handler.findPending(null);
        assertEquals(1, pending.size());
    }

    @Test
    void vocabularyHandler_tests() {
        VocabularyContentHandler handler = new VocabularyContentHandler(vocabularyRepository);
        assertEquals(ContentType.VOCABULARY, handler.type());
        assertEquals("vocabulary", handler.tableName());

        Vocabulary vocab = Vocabulary.builder().id(7L).word("Word").build();
        when(vocabularyRepository.findPending(Kanji.ContentStatus.PENDING_REVIEW))
                .thenReturn(List.of(vocab));

        List<ContentSnapshot> pending = handler.findPending(null);
        assertEquals(1, pending.size());
    }
}
