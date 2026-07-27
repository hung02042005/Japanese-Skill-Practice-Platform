/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.student;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.jlpt.feature.flashcard.repository.FlashcardRepository;
import com.jlpt.feature.learning.Kanji;
import com.jlpt.feature.learning.VocabularyRepository;
import com.jlpt.feature.learning.VocabularyTopic;
import com.jlpt.feature.learning.VocabularyTopicRepository;
import com.jlpt.feature.student.dto.response.VocabHomeResponse;
import com.jlpt.shared.exception.ResourceNotFoundException;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class VocabHomeServiceTest {

    @Mock
    private StudentUserRepository studentUserRepository;

    @Mock
    private VocabularyTopicRepository topicRepository;

    @Mock
    private VocabularyRepository vocabularyRepository;

    @Mock
    private StudentContentProgressRepository progressRepository;

    @Mock
    private FlashcardRepository flashcardRepository;

    @InjectMocks
    private VocabHomeService service;

    private StudentUser student;

    @BeforeEach
    void setUp() {
        student = StudentUser.builder()
                .id(1L)
                .currentJlptLevel(StudentUser.JlptLevel.N5)
                .currentStreak(5)
                .build();
    }

    @Test
    void getVocabHome_studentNotFound() {
        when(studentUserRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> service.getVocabHome(1L));
    }

    @Test
    void getVocabHome_success() {
        when(studentUserRepository.findById(1L)).thenReturn(Optional.of(student));
        VocabularyTopic topic1 =
                VocabularyTopic.builder().id(10L).titleVi("Topic 1").slug("t1").build();
        VocabularyTopic topic2 =
                VocabularyTopic.builder().id(11L).titleVi("Topic 2").slug("t2").build();
        when(topicRepository.findPublishedByLevel(StudentUser.JlptLevel.N5, Kanji.ContentStatus.PUBLISHED))
                .thenReturn(List.of(topic1, topic2));

        when(vocabularyRepository.countPublishedByTopic(Kanji.ContentStatus.PUBLISHED, 10L))
                .thenReturn(5L);
        when(vocabularyRepository.countPublishedByTopic(Kanji.ContentStatus.PUBLISHED, 11L))
                .thenReturn(5L);

        when(progressRepository.countCompletedVocabularyInTopic(eq(1L), any(), any(), eq(10L), any()))
                .thenReturn(5L);
        when(progressRepository.countCompletedVocabularyInTopic(eq(1L), any(), any(), eq(11L), any()))
                .thenReturn(2L);

        VocabHomeResponse res = service.getVocabHome(1L, "N5");
        assertNotNull(res);
        assertEquals("N5", res.getLevel());
        assertEquals(5, res.getStreak());
        assertEquals(2, res.getLessons().size());
        assertEquals("available", res.getLessons().get(0).getStatus());
        assertEquals("active", res.getLessons().get(1).getStatus());
    }
}
