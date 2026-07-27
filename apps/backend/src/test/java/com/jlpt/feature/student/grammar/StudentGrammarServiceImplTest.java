/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.student.grammar;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.jlpt.feature.learning.GrammarPoint;
import com.jlpt.feature.learning.Kanji.ContentStatus;
import com.jlpt.feature.student.StudentContentProgressRepository;
import com.jlpt.feature.student.StudentUser;
import com.jlpt.feature.student.StudentUser.JlptLevel;
import com.jlpt.feature.student.StudentUserRepository;
import com.jlpt.feature.student.exception.StudentLearningException;
import com.jlpt.feature.student.grammar.dto.GrammarDetailResponse;
import com.jlpt.feature.student.grammar.dto.GrammarListResponse;
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
 * Unit tests cho StudentGrammarServiceImpl.
 */
@ExtendWith(MockitoExtension.class)
class StudentGrammarServiceImplTest {

    @Mock
    private StudentGrammarRepository grammarRepository;

    @Mock
    private StudentUserRepository studentUserRepository;

    @Mock
    private StudentContentProgressRepository progressRepository;

    @InjectMocks
    private StudentGrammarServiceImpl studentGrammarService;

    private StudentUser student;

    @BeforeEach
    void setUp() {
        student = StudentUser.builder()
                .id(1L)
                .email("student@test.com")
                .currentStreak(0)
                .longestStreak(0)
                .build();
    }

    @Test
    void getGrammarList_invalidLevel_throwsStudentLearningException() {
        when(studentUserRepository.findById(1L)).thenReturn(Optional.of(student));

        assertThrows(StudentLearningException.class, () -> studentGrammarService.getGrammarList("INVALID", 1L, 0, 10));
    }

    @Test
    void getGrammarList_success_returnsGrammarSummaryList() {
        when(studentUserRepository.findById(1L)).thenReturn(Optional.of(student));

        GrammarPoint g1 = GrammarPoint.builder()
                .id(1L)
                .title("Grammar 1")
                .structure("S1")
                .meaning("M1")
                .jlptLevel(JlptLevel.N5)
                .status(ContentStatus.PUBLISHED)
                .build();

        when(grammarRepository.findByJlptLevelAndStatus(
                        eq(JlptLevel.N5), eq(ContentStatus.PUBLISHED), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(g1)));

        GrammarListResponse res = studentGrammarService.getGrammarList("N5", 1L, 0, 10);

        assertNotNull(res);
        assertEquals(1, res.getContent().size());
        assertEquals("Grammar 1", res.getContent().get(0).getTitle());
    }

    @Test
    void getGrammarDetail_vipContentNonVipUser_throwsVipRequired() {
        when(studentUserRepository.findById(1L)).thenReturn(Optional.of(student)); // Non-VIP email

        GrammarPoint vipGrammar = GrammarPoint.builder()
                .id(5L) // id % 5 == 0 -> VIP
                .title("VIP Grammar")
                .jlptLevel(JlptLevel.N5)
                .status(ContentStatus.PUBLISHED)
                .build();
        when(grammarRepository.findByIdAndStatus(5L, ContentStatus.PUBLISHED)).thenReturn(Optional.of(vipGrammar));

        assertThrows(StudentLearningException.class, () -> studentGrammarService.getGrammarDetail(5L, 1L));
    }

    @Test
    void getGrammarDetail_success_updatesStreakAndReturnsDetail() {
        when(studentUserRepository.findById(1L)).thenReturn(Optional.of(student));

        GrammarPoint normalGrammar = GrammarPoint.builder()
                .id(1L)
                .title("Normal Grammar")
                .structure("V-てから")
                .meaning("After doing V")
                .usageExplanation("Usage")
                .exampleSentenceJp("JP")
                .exampleSentenceVi("VI")
                .jlptLevel(JlptLevel.N5)
                .status(ContentStatus.PUBLISHED)
                .build();
        when(grammarRepository.findByIdAndStatus(1L, ContentStatus.PUBLISHED)).thenReturn(Optional.of(normalGrammar));

        GrammarDetailResponse res = studentGrammarService.getGrammarDetail(1L, 1L);

        assertNotNull(res);
        assertEquals("Normal Grammar", res.getTitle());
        assertEquals(1, student.getCurrentStreak());
        verify(studentUserRepository).save(student);
    }
}
