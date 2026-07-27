/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.student.lesson;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.jlpt.feature.learning.Lesson;
import com.jlpt.feature.learning.LessonRepository;
import com.jlpt.feature.student.StudentContentProgress;
import com.jlpt.feature.student.StudentContentProgressRepository;
import com.jlpt.feature.student.StudentUser;
import com.jlpt.feature.student.lesson.dto.LessonDetailResponse;
import com.jlpt.shared.exception.ResourceNotFoundException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit tests cho StudentLessonService.
 */
@ExtendWith(MockitoExtension.class)
class StudentLessonServiceTest {

    @Mock
    private LessonRepository lessonRepository;

    @Mock
    private StudentContentProgressRepository progressRepository;

    @InjectMocks
    private StudentLessonService studentLessonService;

    private Lesson lesson;

    @BeforeEach
    void setUp() {
        lesson = Lesson.builder()
                .id(10L)
                .title("Greetings in Japanese")
                .jlptLevel(StudentUser.JlptLevel.N5)
                .lessonType(Lesson.LessonType.LESSON)
                .contentText("<p>Hello</p>")
                .status(Lesson.LessonStatus.PUBLISHED)
                .build();
    }

    @Test
    void getLessonDetail_notFound_throwsResourceNotFoundException() {
        when(lessonRepository.findByIdAndStatus(99L, Lesson.LessonStatus.PUBLISHED))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> studentLessonService.getLessonDetail(99L, 1L));
    }

    @Test
    void getLessonDetail_withProgressAndPrevNext_returnsFullDetail() {
        when(lessonRepository.findByIdAndStatus(10L, Lesson.LessonStatus.PUBLISHED))
                .thenReturn(Optional.of(lesson));

        StudentContentProgress progress = StudentContentProgress.builder()
                .status(StudentContentProgress.ProgressStatus.COMPLETED)
                .progressPercent(BigDecimal.valueOf(100.0))
                .build();
        when(progressRepository.findByStudentIdAndContentTypeAndContentId(eq(1L), any(), eq(10L)))
                .thenReturn(Optional.of(progress));

        Lesson prevLesson = Lesson.builder().id(5L).build();
        Lesson nextLesson = Lesson.builder().id(15L).build();
        when(lessonRepository.findByJlptLevelAndStatusOrderByDisplayOrderAscIdAsc(
                        eq(StudentUser.JlptLevel.N5), eq(Lesson.LessonStatus.PUBLISHED)))
                .thenReturn(List.of(prevLesson, lesson, nextLesson));

        LessonDetailResponse response = studentLessonService.getLessonDetail(10L, 1L);

        assertNotNull(response);
        assertEquals(10L, response.getId());
        assertEquals("Greetings in Japanese", response.getTitle());
        assertEquals("completed", response.getProgressStatus());
        assertEquals(100, response.getProgressPercent());
        assertEquals(5L, response.getPrevLessonId());
        assertEquals(15L, response.getNextLessonId());
    }
}
