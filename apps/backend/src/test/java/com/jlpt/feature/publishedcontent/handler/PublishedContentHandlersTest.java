/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.publishedcontent.handler;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.jlpt.feature.assessment.Assessment;
import com.jlpt.feature.assessment.Question;
import com.jlpt.feature.contentreview.model.ContentType;
import com.jlpt.feature.learning.*;
import com.jlpt.feature.publishedcontent.model.ManagedContentSnapshot;
import com.jlpt.feature.publishedcontent.repository.*;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PublishedContentHandlersTest {

    @Mock
    private ManagedVocabularyRepository vocabularyRepository;

    @Mock
    private ManagedGrammarRepository grammarRepository;

    @Mock
    private ManagedLessonRepository lessonRepository;

    @Mock
    private ManagedAssessmentRepository assessmentRepository;

    @Mock
    private ManagedKanjiRepository kanjiRepository;

    @Mock
    private ManagedQuestionRepository questionRepository;

    @Mock
    private QuestionReferenceRepository referenceRepository;

    @Test
    void vocabularyManagedHandler_tests() {
        VocabularyManagedHandler handler = new VocabularyManagedHandler(vocabularyRepository);
        assertEquals(ContentType.VOCABULARY, handler.type());
        assertEquals("vocabulary", handler.tableName());

        Vocabulary vocab = Vocabulary.builder()
                .id(1L)
                .word("Word")
                .status(Kanji.ContentStatus.PUBLISHED)
                .build();
        when(vocabularyRepository.findPublished(Kanji.ContentStatus.PUBLISHED, null))
                .thenReturn(List.of(vocab));

        List<ManagedContentSnapshot> list = handler.findPublished(null);
        assertEquals(1, list.size());
        assertEquals(1L, list.get(0).getContentId());

        when(vocabularyRepository.findById(1L)).thenReturn(Optional.of(vocab));
        assertTrue(handler.findById(1L).isPresent());
    }

    @Test
    void grammarManagedHandler_tests() {
        GrammarManagedHandler handler = new GrammarManagedHandler(grammarRepository);
        assertEquals(ContentType.GRAMMAR, handler.type());
        assertEquals("grammar_points", handler.tableName());

        GrammarPoint grammar = GrammarPoint.builder()
                .id(2L)
                .title("Grammar")
                .status(Kanji.ContentStatus.PUBLISHED)
                .build();
        when(grammarRepository.findPublished(Kanji.ContentStatus.PUBLISHED, null))
                .thenReturn(List.of(grammar));

        List<ManagedContentSnapshot> list = handler.findPublished(null);
        assertEquals(1, list.size());
    }

    @Test
    void lessonManagedHandler_tests() {
        LessonManagedHandler handler = new LessonManagedHandler(lessonRepository, assessmentRepository);
        assertEquals(ContentType.LESSON, handler.type());
        assertEquals("lessons", handler.tableName());

        Lesson lesson = Lesson.builder()
                .id(3L)
                .title("Lesson")
                .status(Lesson.LessonStatus.PUBLISHED)
                .build();
        when(lessonRepository.findPublished(Lesson.LessonStatus.PUBLISHED, null))
                .thenReturn(List.of(lesson));

        List<ManagedContentSnapshot> list = handler.findPublished(null);
        assertEquals(1, list.size());
    }

    @Test
    void kanjiManagedHandler_tests() {
        KanjiManagedHandler handler = new KanjiManagedHandler(kanjiRepository);
        assertEquals(ContentType.KANJI, handler.type());
        assertEquals("kanji", handler.tableName());

        Kanji kanji = Kanji.builder()
                .id(4L)
                .characterValue("日")
                .status(Kanji.ContentStatus.PUBLISHED)
                .build();
        when(kanjiRepository.findPublished(Kanji.ContentStatus.PUBLISHED, null)).thenReturn(List.of(kanji));

        List<ManagedContentSnapshot> list = handler.findPublished(null);
        assertEquals(1, list.size());
    }

    @Test
    void questionManagedHandler_tests() {
        QuestionManagedHandler handler = new QuestionManagedHandler(questionRepository, referenceRepository);
        assertEquals(ContentType.QUESTION, handler.type());
        assertEquals("questions", handler.tableName());

        Question question = Question.builder()
                .id(5L)
                .questionText("Text")
                .status(Question.ContentStatus.PUBLISHED)
                .build();
        when(questionRepository.findPublished(Question.ContentStatus.PUBLISHED, null))
                .thenReturn(List.of(question));

        List<ManagedContentSnapshot> list = handler.findPublished(null);
        assertEquals(1, list.size());
    }

    @Test
    void assessmentManagedHandler_tests() {
        AssessmentManagedHandler handler = new AssessmentManagedHandler(assessmentRepository);
        assertEquals(ContentType.ASSESSMENT, handler.type());
        assertEquals("assessments", handler.tableName());

        Assessment assessment = Assessment.builder()
                .id(6L)
                .title("Quiz")
                .status(Kanji.ContentStatus.PUBLISHED)
                .build();
        when(assessmentRepository.findPublished(Kanji.ContentStatus.PUBLISHED, null))
                .thenReturn(List.of(assessment));

        List<ManagedContentSnapshot> list = handler.findPublished(null);
        assertEquals(1, list.size());
    }
}
