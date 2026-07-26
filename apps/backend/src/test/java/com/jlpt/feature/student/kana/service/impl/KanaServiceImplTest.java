/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.student.kana.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.jlpt.feature.learning.KanaCharacter;
import com.jlpt.feature.learning.repository.KanaCharacterRepository;
import com.jlpt.feature.student.StudentContentProgressRepository;
import com.jlpt.feature.student.kana.dto.response.KanaListResponse;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit tests cho KanaServiceImpl.
 */
@ExtendWith(MockitoExtension.class)
class KanaServiceImplTest {

    @Mock
    private KanaCharacterRepository kanaRepository;

    @Mock
    private StudentContentProgressRepository progressRepository;

    @InjectMocks
    private KanaServiceImpl kanaService;

    @Test
    void getKanaChart_invalidScript_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> kanaService.getKanaChart("INVALID", 1L));
    }

    @Test
    void getKanaChart_validScript_returnsMappedKanaChart() {
        KanaCharacter k1 = KanaCharacter.builder()
                .id(1)
                .characterValue("あ")
                .romaji("a")
                .kanaType(KanaCharacter.KanaType.HIRAGANA)
                .displayOrder(1)
                .build();

        when(kanaRepository.findByKanaTypeOrderByDisplayOrderAsc(KanaCharacter.KanaType.HIRAGANA))
                .thenReturn(List.of(k1));
        when(progressRepository.findByStudentIdAndContentTypeAndContentIdIn(eq(1L), any(), any()))
                .thenReturn(List.of());

        KanaListResponse res = kanaService.getKanaChart("HIRAGANA", 1L);

        assertNotNull(res);
        assertEquals(1, res.getTotalCount());
        assertEquals(0, res.getCompletedCount());
        assertEquals("あ", res.getCharacters().get(0).getCharacter());
        assertEquals("a-row", res.getCharacters().get(0).getRow());
    }
}
