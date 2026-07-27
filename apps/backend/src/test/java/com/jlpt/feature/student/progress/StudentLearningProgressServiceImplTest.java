/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.student.progress;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.jlpt.feature.student.StudentContentProgress;
import com.jlpt.feature.student.StudentContentProgress.ContentType;
import com.jlpt.feature.student.StudentContentProgress.ProgressStatus;
import com.jlpt.feature.student.StudentContentProgressRepository;
import com.jlpt.feature.student.StudentUser;
import com.jlpt.feature.student.StudentUserRepository;
import com.jlpt.feature.student.progress.dto.LearningProgressRequest;
import com.jlpt.feature.student.progress.dto.LearningProgressResponse;
import com.jlpt.shared.exception.ResourceNotFoundException;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit tests cho StudentLearningProgressServiceImpl — đảm bảo quy tắc BR-07-04 tiến độ chỉ tăng không giảm.
 */
@ExtendWith(MockitoExtension.class)
class StudentLearningProgressServiceImplTest {

    @Mock
    private StudentContentProgressRepository progressRepository;

    @Mock
    private StudentUserRepository studentUserRepository;

    @InjectMocks
    private StudentLearningProgressServiceImpl progressService;

    private StudentUser student;

    @BeforeEach
    void setUp() {
        student = StudentUser.builder().id(1L).email("student@test.com").build();
    }

    @Test
    void markProgress_studentNotFound_throwsResourceNotFoundException() {
        when(studentUserRepository.findById(99L)).thenReturn(Optional.empty());
        LearningProgressRequest req = new LearningProgressRequest();

        assertThrows(ResourceNotFoundException.class, () -> progressService.markProgress(req, 99L));
    }

    @Test
    void markProgress_invalidContentType_throwsIllegalArgumentException() {
        when(studentUserRepository.findById(1L)).thenReturn(Optional.of(student));
        LearningProgressRequest req = new LearningProgressRequest();
        req.setContentType("INVALID");

        assertThrows(IllegalArgumentException.class, () -> progressService.markProgress(req, 1L));
    }

    @Test
    void markProgress_newProgress_createsAndSaves() {
        when(studentUserRepository.findById(1L)).thenReturn(Optional.of(student));
        when(progressRepository.findByStudentIdAndContentTypeAndContentId(1L, ContentType.LESSON, 10L))
                .thenReturn(Optional.empty());
        when(progressRepository.save(any())).thenAnswer(i -> {
            StudentContentProgress p = i.getArgument(0);
            p.setId(100L);
            return p;
        });

        LearningProgressRequest req = new LearningProgressRequest();
        req.setContentType("LESSON");
        req.setContentId(10L);
        req.setStatus("LEARNING");
        req.setProgressPercent(BigDecimal.valueOf(50.0));

        LearningProgressResponse res = progressService.markProgress(req, 1L);

        assertNotNull(res);
        assertEquals(100L, res.getId());
        assertEquals(BigDecimal.valueOf(50.0), res.getProgressPercent());
        verify(progressRepository).save(any(StudentContentProgress.class));
    }

    @Test
    void markProgress_existingProgress_doesNotLowerPercentOrRevertCompleted() {
        StudentContentProgress existing = StudentContentProgress.builder()
                .id(100L)
                .student(student)
                .contentType(ContentType.LESSON)
                .contentId(10L)
                .status(ProgressStatus.COMPLETED)
                .progressPercent(BigDecimal.valueOf(100.0))
                .build();

        when(studentUserRepository.findById(1L)).thenReturn(Optional.of(student));
        when(progressRepository.findByStudentIdAndContentTypeAndContentId(1L, ContentType.LESSON, 10L))
                .thenReturn(Optional.of(existing));
        when(progressRepository.save(any())).thenReturn(existing);

        // Lower percent request
        LearningProgressRequest req = new LearningProgressRequest();
        req.setContentType("LESSON");
        req.setContentId(10L);
        req.setStatus("LEARNING");
        req.setProgressPercent(BigDecimal.valueOf(30.0));

        LearningProgressResponse res = progressService.markProgress(req, 1L);

        // Should maintain 100% and COMPLETED status
        assertEquals(BigDecimal.valueOf(100.0), existing.getProgressPercent());
        assertEquals(ProgressStatus.COMPLETED, existing.getStatus());
        verify(progressRepository).save(existing);
    }

    @Test
    void resetProgress_invalidType_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> progressService.resetProgress("INVALID", 1L));
    }

    @Test
    void resetProgress_validType_deletesProgressRecords() {
        progressService.resetProgress("KANJI", 1L);

        verify(progressRepository).deleteByStudentIdAndContentType(1L, ContentType.KANJI);
    }
}
