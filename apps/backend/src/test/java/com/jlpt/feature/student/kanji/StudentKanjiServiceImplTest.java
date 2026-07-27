/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.student.kanji;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.jlpt.feature.learning.Kanji;
import com.jlpt.feature.learning.Kanji.ContentStatus;
import com.jlpt.feature.student.StudentContentProgressRepository;
import com.jlpt.feature.student.StudentUser;
import com.jlpt.feature.student.StudentUser.JlptLevel;
import com.jlpt.feature.student.StudentUserRepository;
import com.jlpt.feature.student.kanji.dto.KanjiDetailResponse;
import com.jlpt.feature.student.kanji.dto.KanjiListResponse;
import com.jlpt.shared.exception.ResourceNotFoundException;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

/**
 * Unit tests cho StudentKanjiServiceImpl.
 */
@ExtendWith(MockitoExtension.class)
class StudentKanjiServiceImplTest {

    @Mock
    private StudentKanjiRepository kanjiRepository;

    @Mock
    private StudentContentProgressRepository progressRepository;

    @Mock
    private StudentUserRepository studentUserRepository;

    @InjectMocks
    private StudentKanjiServiceImpl studentKanjiService;

    private Kanji kanji;

    @BeforeEach
    void setUp() {
        kanji = Kanji.builder()
                .id(1L)
                .characterValue("日")
                .meaning("Sun, Day")
                .onyomi("NICHI")
                .kunyomi("hi")
                .strokeCount(4)
                .jlptLevel(JlptLevel.N5)
                .status(ContentStatus.PUBLISHED)
                .build();
    }

    @Test
    void getKanjiList_invalidLevel_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> studentKanjiService.getKanjiList("INVALID", 1L, 0, 10));
    }

    @Test
    void getKanjiList_success_returnsMappedKanjiList() {
        when(kanjiRepository.findByLevelAndStatus(
                        eq(JlptLevel.N5), eq(ContentStatus.PUBLISHED), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(kanji)));
        when(progressRepository.findByStudentIdAndContentTypeAndContentIdIn(eq(1L), any(), any()))
                .thenReturn(List.of());

        KanjiListResponse res = studentKanjiService.getKanjiList("N5", 1L, 0, 10);

        assertNotNull(res);
        assertEquals(1, res.getContent().size());
        assertEquals("日", res.getContent().get(0).getCharacterValue());
    }

    @Test
    void getKanjiDetail_notFound_throwsResourceNotFoundException() {
        when(kanjiRepository.findByIdAndStatus(99L, ContentStatus.PUBLISHED)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> studentKanjiService.getKanjiDetail(99L, 1L));
    }

    @Test
    void getKanjiDetail_success_updatesActivityDateAndReturnsDetail() {
        StudentUser student =
                StudentUser.builder().id(1L).email("student@test.com").build();
        when(kanjiRepository.findByIdAndStatus(1L, ContentStatus.PUBLISHED)).thenReturn(Optional.of(kanji));
        when(studentUserRepository.findById(1L)).thenReturn(Optional.of(student));

        KanjiDetailResponse res = studentKanjiService.getKanjiDetail(1L, 1L);

        assertNotNull(res);
        assertEquals("日", res.getCharacterValue());
        assertNotNull(student.getLastActivityDate());
        verify(studentUserRepository).save(student);
    }
}
