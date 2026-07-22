/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.staffcontent.learning.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.jlpt.feature.learning.Kanji;
import com.jlpt.feature.learning.VocabularyRepository;
import com.jlpt.feature.learning.VocabularyTopic;
import com.jlpt.feature.learning.VocabularyTopicRepository;
import com.jlpt.feature.staff.StaffUser;
import com.jlpt.feature.staff.StaffUserRepository;
import com.jlpt.feature.staffcontent.learning.service.VocabularyTopicService;
import com.jlpt.shared.exception.BusinessException;
import com.jlpt.shared.exception.ForbiddenException;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class VocabularyTopicServiceDeleteRestoreTest {

    @Mock
    private VocabularyTopicRepository topicRepository;

    @Mock
    private StaffUserRepository staffUserRepository;

    @Mock
    private VocabularyRepository vocabularyRepository;

    @InjectMocks
    private VocabularyTopicService topicService;

    private StaffUser activeManager;
    private StaffUser normalStaff;
    private VocabularyTopic activeTopic;
    private VocabularyTopic deletedTopic;

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

        activeTopic = VocabularyTopic.builder()
                .id(10L)
                .titleVi("Gia đình")
                .status(Kanji.ContentStatus.PUBLISHED)
                .build();

        deletedTopic = VocabularyTopic.builder()
                .id(11L)
                .titleVi("Thời tiết")
                .status(Kanji.ContentStatus.DELETED)
                .build();
    }

    @Test
    void deleteTopic_success() {
        when(staffUserRepository.findByEmail("manager@sakuji.com")).thenReturn(Optional.of(activeManager));
        when(topicRepository.findById(10L)).thenReturn(Optional.of(activeTopic));
        when(vocabularyRepository.countByTopicRefIdAndStatusNot(10L, Kanji.ContentStatus.DELETED))
                .thenReturn(0L);

        topicService.deleteTopic(10L, "manager@sakuji.com");

        assertEquals(Kanji.ContentStatus.DELETED, activeTopic.getStatus());
        verify(topicRepository).save(activeTopic);
    }

    @Test
    void deleteTopic_failsIfNotManager() {
        when(staffUserRepository.findByEmail("staff@sakuji.com")).thenReturn(Optional.of(normalStaff));

        assertThrows(ForbiddenException.class, () -> {
            topicService.deleteTopic(10L, "staff@sakuji.com");
        });

        verify(topicRepository, never()).save(any());
    }

    @Test
    void deleteTopic_failsIfVocabulariesInUse() {
        when(staffUserRepository.findByEmail("manager@sakuji.com")).thenReturn(Optional.of(activeManager));
        when(topicRepository.findById(10L)).thenReturn(Optional.of(activeTopic));
        when(vocabularyRepository.countByTopicRefIdAndStatusNot(10L, Kanji.ContentStatus.DELETED))
                .thenReturn(5L);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            topicService.deleteTopic(10L, "manager@sakuji.com");
        });

        assertEquals("RESOURCE_IN_USE", exception.getErrorCode());
        verify(topicRepository, never()).save(any());
    }

    @Test
    void deleteTopic_failsIfAlreadyDeleted() {
        when(staffUserRepository.findByEmail("manager@sakuji.com")).thenReturn(Optional.of(activeManager));
        when(topicRepository.findById(11L)).thenReturn(Optional.of(deletedTopic));

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            topicService.deleteTopic(11L, "manager@sakuji.com");
        });

        assertEquals("ALREADY_DELETED", exception.getErrorCode());
        verify(topicRepository, never()).save(any());
    }

    @Test
    void restoreTopic_success() {
        when(staffUserRepository.findByEmail("manager@sakuji.com")).thenReturn(Optional.of(activeManager));
        when(topicRepository.findById(11L)).thenReturn(Optional.of(deletedTopic));
        when(topicRepository.save(any(VocabularyTopic.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = topicService.restoreTopic(11L, "manager@sakuji.com");

        assertNotNull(response);
        assertEquals(Kanji.ContentStatus.PUBLISHED, deletedTopic.getStatus());
        verify(topicRepository).save(deletedTopic);
    }

    @Test
    void restoreTopic_failsIfNotDeleted() {
        when(staffUserRepository.findByEmail("manager@sakuji.com")).thenReturn(Optional.of(activeManager));
        when(topicRepository.findById(10L)).thenReturn(Optional.of(activeTopic));

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            topicService.restoreTopic(10L, "manager@sakuji.com");
        });

        assertEquals("NOT_DELETED", exception.getErrorCode());
        verify(topicRepository, never()).save(any());
    }

    @Test
    void listDeletedTopics_success() {
        when(staffUserRepository.findByEmail("manager@sakuji.com")).thenReturn(Optional.of(activeManager));
        when(topicRepository.findByStatusOrderByUpdatedAtDesc(Kanji.ContentStatus.DELETED))
                .thenReturn(List.of(deletedTopic));

        var list = topicService.listDeletedTopics("manager@sakuji.com");

        assertNotNull(list);
        assertEquals(1, list.size());
        assertEquals("Thời tiết", list.get(0).titleVi());
    }
}
