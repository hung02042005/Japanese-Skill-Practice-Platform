/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.learning;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.jlpt.feature.learning.dto.VocabTopicResponse;
import com.jlpt.feature.learning.dto.VocabularyListResponse;
import com.jlpt.feature.student.StudentContentProgress;
import com.jlpt.feature.student.StudentContentProgressRepository;
import com.jlpt.feature.student.StudentUser.JlptLevel;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class StudentVocabularyServiceTest {

    @Mock
    private VocabularyTopicRepository topicRepository;

    @Mock
    private VocabularyRepository vocabularyRepository;

    @Mock
    private StudentContentProgressRepository progressRepository;

    @InjectMocks
    private StudentVocabularyService service;

    @Test
    void getTopics_success() {
        VocabularyTopic topic =
                VocabularyTopic.builder().id(10L).titleVi("Title").build();
        when(topicRepository.findPublishedByLevel(JlptLevel.N5, Kanji.ContentStatus.PUBLISHED))
                .thenReturn(List.of(topic));

        List<VocabTopicResponse> res = service.getTopics("N5");
        assertEquals(1, res.size());
        assertEquals("Title", res.get(0).titleVi());
    }

    @Test
    void getVocabularyList_success() {
        Vocabulary vocab =
                Vocabulary.builder().id(100L).word("Word").meaning("Meaning").build();
        Page<Vocabulary> page = new PageImpl<>(List.of(vocab));
        when(vocabularyRepository.findPublished(
                        eq(Kanji.ContentStatus.PUBLISHED), eq(JlptLevel.N5), eq(5L), eq("q"), any(Pageable.class)))
                .thenReturn(page);

        StudentContentProgress progress = StudentContentProgress.builder()
                .contentId(100L)
                .status(StudentContentProgress.ProgressStatus.COMPLETED)
                .build();
        when(progressRepository.findByStudentIdAndContentTypeAndContentIdIn(
                        eq(1L), eq(StudentContentProgress.ContentType.VOCABULARY), eq(List.of(100L))))
                .thenReturn(List.of(progress));

        when(progressRepository.countCompletedVocabularyByLevel(
                        1L,
                        JlptLevel.N5,
                        StudentContentProgress.ContentType.VOCABULARY,
                        StudentContentProgress.ProgressStatus.COMPLETED))
                .thenReturn(1L);

        VocabularyListResponse res = service.getVocabularyList("N5", 5L, "q", 0, 10, 1L);
        assertEquals(1, res.getContent().size());
        assertTrue(res.getContent().get(0).isCompleted());
        assertEquals(1L, res.getCompletedCount());
    }
}
