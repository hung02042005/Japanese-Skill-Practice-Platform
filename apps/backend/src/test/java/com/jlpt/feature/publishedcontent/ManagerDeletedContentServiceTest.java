/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.publishedcontent;

import static org.junit.jupiter.api.Assertions.*;
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
import com.jlpt.shared.exception.BusinessException;
import com.jlpt.shared.exception.ForbiddenException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ManagerDeletedContentServiceTest {

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
    private ManagerDeletedContentService deletedContentService;

    private StaffUser activeManager;
    private StaffUser normalStaff;

    @BeforeEach
    void setUp() {
        activeManager = StaffUser.builder()
                .id(1L)
                .email("manager@sakuji.com")
                .staffRole(StaffUser.StaffRole.STAFF_MANAGER)
                .status(StaffUser.StaffStatus.ACTIVE)
                .build();

        normalStaff = StaffUser.builder()
                .id(2L)
                .email("staff@sakuji.com")
                .staffRole(StaffUser.StaffRole.STAFF)
                .status(StaffUser.StaffStatus.ACTIVE)
                .build();
    }

    @Test
    void listDeleted_all_success() {
        when(staffUserRepository.findByEmail("manager@sakuji.com")).thenReturn(Optional.of(activeManager));

        Lesson lesson = Lesson.builder()
                .id(100L)
                .title("Bài 1")
                .status(Lesson.LessonStatus.DELETED)
                .updatedAt(LocalDateTime.now())
                .build();
        when(lessonRepository.findByStatusOrderByUpdatedAtDesc(Lesson.LessonStatus.DELETED))
                .thenReturn(List.of(lesson));

        Question question = Question.builder()
                .id(101L)
                .questionText("Câu hỏi 1")
                .status(Question.ContentStatus.DELETED)
                .updatedAt(LocalDateTime.now())
                .build();
        when(questionRepository.findByStatusOrderByUpdatedAtDesc(Question.ContentStatus.DELETED))
                .thenReturn(List.of(question));

        VocabularyTopic topic = VocabularyTopic.builder()
                .id(102L)
                .titleVi("Chủ đề 1")
                .titleJa("トピック1")
                .status(Kanji.ContentStatus.DELETED)
                .updatedAt(LocalDateTime.now())
                .build();
        when(topicRepository.findByStatusOrderByUpdatedAtDesc(Kanji.ContentStatus.DELETED))
                .thenReturn(List.of(topic));

        GrammarPoint grammar = GrammarPoint.builder()
                .id(103L)
                .title("Ngữ pháp 1")
                .status(Kanji.ContentStatus.DELETED)
                .updatedAt(LocalDateTime.now())
                .build();
        when(grammarRepository.findByStatusOrderByUpdatedAtDesc(Kanji.ContentStatus.DELETED))
                .thenReturn(List.of(grammar));

        Kanji kanji = Kanji.builder()
                .id(104L)
                .characterValue("日")
                .meaning("Ngày")
                .status(Kanji.ContentStatus.DELETED)
                .updatedAt(LocalDateTime.now())
                .build();
        when(kanjiRepository.findByStatusOrderByUpdatedAtDesc(Kanji.ContentStatus.DELETED))
                .thenReturn(List.of(kanji));

        Assessment assessment = Assessment.builder()
                .id(105L)
                .title("Đề thi 1")
                .status(Kanji.ContentStatus.DELETED)
                .updatedAt(LocalDateTime.now())
                .build();
        when(assessmentRepository.findByStatusOrderByUpdatedAtDesc(Kanji.ContentStatus.DELETED))
                .thenReturn(List.of(assessment));

        List<DeletedContentResponse> result = deletedContentService.listDeleted("manager@sakuji.com", "all");

        assertEquals(6, result.size());
        assertEquals("lesson", result.get(0).contentType());
        assertEquals("Bài 1", result.get(0).titleOrText());
        assertEquals("question", result.get(1).contentType());
        assertEquals("vocabulary", result.get(2).contentType());
        assertEquals("Chủ đề 1 (トピック1)", result.get(2).titleOrText());
        assertEquals("grammar", result.get(3).contentType());
        assertEquals("kanji", result.get(4).contentType());
        assertEquals("日 - Ngày", result.get(4).titleOrText());
        assertEquals("assessment", result.get(5).contentType());
    }

    @Test
    void listDeleted_failsIfNotManager() {
        when(staffUserRepository.findByEmail("staff@sakuji.com")).thenReturn(Optional.of(normalStaff));

        assertThrows(ForbiddenException.class, () -> {
            deletedContentService.listDeleted("staff@sakuji.com", "all");
        });
    }

    @Test
    void restore_lesson_success() {
        when(staffUserRepository.findByEmail("manager@sakuji.com")).thenReturn(Optional.of(activeManager));
        when(lessonRepository.transition(
                        eq(100L), eq(Lesson.LessonStatus.DELETED), eq(Lesson.LessonStatus.PUBLISHED), any()))
                .thenReturn(1);

        deletedContentService.restore("manager@sakuji.com", "lesson", 100L);

        verify(lessonRepository)
                .transition(eq(100L), eq(Lesson.LessonStatus.DELETED), eq(Lesson.LessonStatus.PUBLISHED), any());
    }

    @Test
    void restore_vocabulary_success() {
        when(staffUserRepository.findByEmail("manager@sakuji.com")).thenReturn(Optional.of(activeManager));
        VocabularyTopic topic = VocabularyTopic.builder()
                .id(102L)
                .status(Kanji.ContentStatus.DELETED)
                .build();
        when(topicRepository.findById(102L)).thenReturn(Optional.of(topic));
        when(topicRepository.save(any(VocabularyTopic.class))).thenAnswer(invocation -> invocation.getArgument(0));

        deletedContentService.restore("manager@sakuji.com", "vocabulary", 102L);

        assertEquals(Kanji.ContentStatus.PUBLISHED, topic.getStatus());
        verify(topicRepository).save(topic);
    }

    @Test
    void restore_failsIfTransitionRowsIsZero() {
        when(staffUserRepository.findByEmail("manager@sakuji.com")).thenReturn(Optional.of(activeManager));
        when(lessonRepository.transition(
                        eq(100L), eq(Lesson.LessonStatus.DELETED), eq(Lesson.LessonStatus.PUBLISHED), any()))
                .thenReturn(0);

        assertThrows(BusinessException.class, () -> {
            deletedContentService.restore("manager@sakuji.com", "lesson", 100L);
        });
    }
}
