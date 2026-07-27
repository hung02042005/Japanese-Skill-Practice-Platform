/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.student;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.jlpt.feature.learning.Lesson;
import com.jlpt.feature.learning.LessonRepository;
import com.jlpt.feature.learning.VocabularyRepository;
import com.jlpt.feature.student.dto.response.DashboardResponse;
import com.jlpt.feature.student.dto.response.NextLessonResponse;
import com.jlpt.feature.student.dto.response.StudentStatsResponse;
import com.jlpt.feature.student.kanji.StudentKanjiRepository;
import com.jlpt.shared.exception.ResourceNotFoundException;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit tests cho StudentDashboardService.
 */
@ExtendWith(MockitoExtension.class)
class StudentDashboardServiceTest {

    @Mock
    private StudentUserRepository studentUserRepository;

    @Mock
    private StudentContentProgressRepository progressRepository;

    @Mock
    private StudentKanjiRepository kanjiRepository;

    @Mock
    private VocabularyRepository vocabularyRepository;

    @Mock
    private LessonRepository lessonRepository;

    @InjectMocks
    private StudentDashboardService studentDashboardService;

    private StudentUser student;

    @BeforeEach
    void setUp() {
        student = StudentUser.builder()
                .id(1L)
                .email("student@test.com")
                .currentJlptLevel(StudentUser.JlptLevel.N5)
                .currentStreak(3)
                .build();
    }

    // ── getDashboard ──────────────────────────────────────────────────────────

    @Test
    void getDashboard_studentNotFound_throwsResourceNotFoundException() {
        when(studentUserRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> studentDashboardService.getDashboard(99L));
    }

    @Test
    void getDashboard_success_returnsDashboardData() {
        when(studentUserRepository.findById(1L)).thenReturn(Optional.of(student));
        when(progressRepository.countCompletedVocab(1L)).thenReturn(25L);

        DashboardResponse res = studentDashboardService.getDashboard(1L);

        assertNotNull(res);
        assertEquals(3, res.getStreak());
        assertEquals("N5", res.getSelectedLevel());
        assertEquals(25L, res.getWordCount());
    }

    // ── getStats ──────────────────────────────────────────────────────────────

    @Test
    void getStats_success_aggregatesStatsData() {
        when(studentUserRepository.findById(1L)).thenReturn(Optional.of(student));
        when(progressRepository.countCompletedVocab(1L)).thenReturn(50L);

        StudentStatsResponse stats = studentDashboardService.getStats(1L);

        assertNotNull(stats);
        assertEquals(3, stats.getCurrentStreak());
        assertEquals(50L, stats.getWordCount());
    }

    // ── getNextLesson ─────────────────────────────────────────────────────────

    @Test
    void getNextLesson_returnsUncompletedLesson() {
        when(studentUserRepository.findById(1L)).thenReturn(Optional.of(student));
        Lesson l1 = Lesson.builder()
                .id(10L)
                .title("Lesson 1")
                .jlptLevel(StudentUser.JlptLevel.N5)
                .build();
        Lesson l2 = Lesson.builder()
                .id(20L)
                .title("Lesson 2")
                .jlptLevel(StudentUser.JlptLevel.N5)
                .build();
        when(lessonRepository.findByJlptLevelAndStatusOrderByDisplayOrderAscIdAsc(eq(StudentUser.JlptLevel.N5), any()))
                .thenReturn(List.of(l1, l2));

        NextLessonResponse res = studentDashboardService.getNextLesson(1L);

        assertNotNull(res);
        assertNotNull(res.getNextLesson());
        assertEquals(10L, res.getNextLesson().getLessonId());
    }
}
