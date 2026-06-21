/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.student;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.jlpt.feature.learning.Lesson;
import com.jlpt.feature.learning.LessonRepository;
import com.jlpt.feature.student.StudentUser.JlptLevel;
import com.jlpt.feature.student.dto.response.CourseListResponse;
import com.jlpt.shared.exception.ResourceNotFoundException;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit test cho {@link CourseService} — SPEC-course-list (VIP đã bỏ khỏi phạm vi).
 * Mock repository, kiểm tra thứ tự N5→N1, currentLevel và đếm tiến độ.
 */
@ExtendWith(MockitoExtension.class)
class CourseServiceTest {

    @Mock
    private StudentUserRepository studentUserRepository;

    @Mock
    private LessonRepository lessonRepository;

    @Mock
    private StudentContentProgressRepository progressRepository;

    @InjectMocks
    private CourseService courseService;

    private static StudentUser studentWithLevel(JlptLevel level) {
        StudentUser s = new StudentUser();
        s.setCurrentJlptLevel(level);
        return s;
    }

    @Test
    void getCourses_returnsFiveLevelsInOrderN5ToN1() {
        when(studentUserRepository.findById(1L)).thenReturn(Optional.of(studentWithLevel(JlptLevel.N4)));
        when(lessonRepository.countByJlptLevelAndStatus(any(), eq(Lesson.LessonStatus.PUBLISHED)))
                .thenReturn(0L);
        when(progressRepository.countCompletedLessonsInLevel(anyLong(), any(), any(), any(), any()))
                .thenReturn(0L);

        CourseListResponse res = courseService.getCourses(1L);

        List<CourseListResponse.CourseItem> courses = res.getCourses();
        assertEquals(5, courses.size());
        assertEquals(
                List.of("N5", "N4", "N3", "N2", "N1"),
                courses.stream()
                        .map(CourseListResponse.CourseItem::getJlptLevel)
                        .toList());
        assertEquals("N4", res.getCurrentLevel());
    }

    @Test
    void getCourses_mapsProgressCountsPerLevel() {
        when(studentUserRepository.findById(1L)).thenReturn(Optional.of(studentWithLevel(JlptLevel.N5)));
        when(lessonRepository.countByJlptLevelAndStatus(eq(JlptLevel.N5), eq(Lesson.LessonStatus.PUBLISHED)))
                .thenReturn(40L);
        when(lessonRepository.countByJlptLevelAndStatus(eq(JlptLevel.N4), eq(Lesson.LessonStatus.PUBLISHED)))
                .thenReturn(52L);
        when(lessonRepository.countByJlptLevelAndStatus(
                        argThat(l -> l == JlptLevel.N3 || l == JlptLevel.N2 || l == JlptLevel.N1),
                        eq(Lesson.LessonStatus.PUBLISHED)))
                .thenReturn(0L);
        when(progressRepository.countCompletedLessonsInLevel(eq(1L), any(), any(), eq(JlptLevel.N5), any()))
                .thenReturn(5L);
        when(progressRepository.countCompletedLessonsInLevel(
                        eq(1L), any(), any(), argThat(l -> l != JlptLevel.N5), any()))
                .thenReturn(0L);

        CourseListResponse res = courseService.getCourses(1L);

        CourseListResponse.CourseItem n5 = res.getCourses().get(0);
        assertEquals("Tiếng Nhật N5", n5.getTitle());
        assertEquals(40L, n5.getTotalLessons());
        assertEquals(5L, n5.getCompletedLessons());
        assertEquals(52L, res.getCourses().get(1).getTotalLessons());
    }

    @Test
    void getCourses_defaultsToN5WhenStudentLevelNull() {
        when(studentUserRepository.findById(1L)).thenReturn(Optional.of(studentWithLevel(null)));
        when(lessonRepository.countByJlptLevelAndStatus(any(), any())).thenReturn(0L);
        when(progressRepository.countCompletedLessonsInLevel(anyLong(), any(), any(), any(), any()))
                .thenReturn(0L);

        assertEquals("N5", courseService.getCourses(1L).getCurrentLevel());
    }

    @Test
    void getCourses_throwsWhenStudentNotFound() {
        when(studentUserRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> courseService.getCourses(99L));
    }
}
