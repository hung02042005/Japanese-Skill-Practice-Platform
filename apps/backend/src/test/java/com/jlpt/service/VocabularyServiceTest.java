/* (c) JLPT E-Learning Platform */
package com.jlpt.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.jlpt.dto.response.VocabularyPathResponse;
import com.jlpt.entity.Kanji;
import com.jlpt.entity.StudentContentProgress.ContentType;
import com.jlpt.entity.StudentContentProgress.ProgressStatus;
import com.jlpt.entity.StudentUser;
import com.jlpt.entity.VocabularyTopic;
import com.jlpt.flashcard.repository.FlashcardRepository;
import com.jlpt.repository.StudentContentProgressRepository;
import com.jlpt.repository.StudentUserRepository;
import com.jlpt.repository.VocabularyRepository;
import com.jlpt.repository.VocabularyTopicRepository;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class VocabularyServiceTest {

    @Mock
    private VocabularyRepository vocabularyRepository;

    @Mock
    private StudentContentProgressRepository progressRepository;

    @Mock
    private StudentUserRepository studentUserRepository;

    @Mock
    private FlashcardRepository flashcardRepository;

    @Mock
    private VocabularyTopicRepository vocabularyTopicRepository;

    @InjectMocks
    private VocabularyService vocabularyService;

    @Test
    void getVocabularyPath_computesCompletedActiveLockedInOrder() {
        VocabularyTopic family = topic(1L, "family", "家族", "Gia đình", 1);
        VocabularyTopic food = topic(2L, "food", "食べ物", "Ẩm thực", 2);
        VocabularyTopic transport = topic(3L, "transport", "交通", "Giao thông", 3);

        when(vocabularyTopicRepository.findByJlptLevelAndStatusOrderByDisplayOrderAsc(
                        StudentUser.JlptLevel.N5, Kanji.ContentStatus.PUBLISHED))
                .thenReturn(List.of(family, food, transport));

        when(vocabularyRepository.countPublishedByTopic(Kanji.ContentStatus.PUBLISHED, 1L))
                .thenReturn(2L);
        when(vocabularyRepository.countPublishedByTopic(Kanji.ContentStatus.PUBLISHED, 2L))
                .thenReturn(3L);
        when(vocabularyRepository.countPublishedByTopic(Kanji.ContentStatus.PUBLISHED, 3L))
                .thenReturn(4L);

        when(progressRepository.countCompletedVocabularyInTopic(
                        99L, ContentType.VOCABULARY, ProgressStatus.COMPLETED, 1L, Kanji.ContentStatus.PUBLISHED))
                .thenReturn(2L);
        when(progressRepository.countCompletedVocabularyInTopic(
                        99L, ContentType.VOCABULARY, ProgressStatus.COMPLETED, 2L, Kanji.ContentStatus.PUBLISHED))
                .thenReturn(1L);
        when(progressRepository.countCompletedVocabularyInTopic(
                        99L, ContentType.VOCABULARY, ProgressStatus.COMPLETED, 3L, Kanji.ContentStatus.PUBLISHED))
                .thenReturn(0L);

        List<VocabularyPathResponse> path = vocabularyService.getVocabularyPath(99L, "N5");

        assertEquals(3, path.size());
        assertEquals("completed", path.get(0).status());
        assertEquals("active", path.get(1).status());
        assertEquals("locked", path.get(2).status());
        assertEquals("food", path.get(1).slug());
        assertEquals(3L, path.get(1).totalWords());
        assertEquals(1L, path.get(1).completedWords());
    }

    @Test
    void getVocabularyPath_invalidLevelReturnsEmptyPath() {
        List<VocabularyPathResponse> path = vocabularyService.getVocabularyPath(99L, "bad");

        assertTrue(path.isEmpty());
        verifyNoInteractions(vocabularyTopicRepository, vocabularyRepository, progressRepository);
    }

    private static VocabularyTopic topic(Long id, String slug, String titleJa, String titleVi, int displayOrder) {
        return VocabularyTopic.builder()
                .id(id)
                .jlptLevel(StudentUser.JlptLevel.N5)
                .slug(slug)
                .titleJa(titleJa)
                .titleVi(titleVi)
                .displayOrder(displayOrder)
                .status(Kanji.ContentStatus.PUBLISHED)
                .build();
    }
}
