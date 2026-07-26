/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.speaking;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.jlpt.feature.assessment.StudentSubmission;
import com.jlpt.feature.assessment.StudentSubmissionRepository;
import com.jlpt.feature.learning.Lesson;
import com.jlpt.feature.learning.LessonRepository;
import com.jlpt.feature.speaking.dto.SpeakingExerciseResponse;
import com.jlpt.feature.speaking.dto.SpeakingResultResponse;
import com.jlpt.feature.speaking.dto.SpeakingSubmitResponse;
import com.jlpt.feature.speaking.repository.SpeakingQuestionRepository;
import com.jlpt.feature.speaking.service.SpeakingAudioStorageService;
import com.jlpt.feature.speaking.service.SpeakingService;
import com.jlpt.feature.student.StudentUser;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

@ExtendWith(MockitoExtension.class)
class SpeakingServiceTest {

    @Mock
    private LessonRepository lessonRepository;

    @Mock
    private SpeakingQuestionRepository speakingQuestionRepository;

    @Mock
    private StudentSubmissionRepository submissionRepository;

    @Mock
    private SpeakingAudioStorageService audioStorage;

    @InjectMocks
    private SpeakingService service;

    private StudentUser student;
    private Lesson exercise;

    @BeforeEach
    void setUp() {
        student = StudentUser.builder().id(1L).build();
        exercise = Lesson.builder()
                .id(10L)
                .title("Speaking Ex 1")
                .jlptLevel(StudentUser.JlptLevel.N5)
                .lessonType(Lesson.LessonType.SPEAKING)
                .status(Lesson.LessonStatus.PUBLISHED)
                .contentText("Sample Text")
                .build();
    }

    @Test
    void getExercises_success() {
        when(lessonRepository.findByJlptLevelAndLessonTypeAndStatusOrderByDisplayOrderAscIdAsc(
                        StudentUser.JlptLevel.N5, Lesson.LessonType.SPEAKING, Lesson.LessonStatus.PUBLISHED))
                .thenReturn(List.of(exercise));

        ArrayList<Object[]> rows = new ArrayList<>();
        rows.add(new Object[] {10L, 2, BigDecimal.valueOf(85)});
        when(submissionRepository.findSpeakingStats(1L, StudentSubmission.SubmissionType.SPEAKING, List.of(10L)))
                .thenReturn(rows);

        List<SpeakingExerciseResponse> res = service.getExercises("N5", 1L);
        assertEquals(1, res.size());
        assertEquals("Speaking Ex 1", res.get(0).getTitle());
        assertEquals(2, res.get(0).getAttemptCount());
        assertEquals(85, res.get(0).getBestScore());
    }

    @Test
    void submit_success() {
        MultipartFile file = mock(MultipartFile.class);
        when(lessonRepository.findByIdAndStatus(10L, Lesson.LessonStatus.PUBLISHED))
                .thenReturn(Optional.of(exercise));
        SpeakingAudioStorageService.StoredAudio stored =
                new SpeakingAudioStorageService.StoredAudio("http://audio.mp3", Path.of("file.mp3"));
        when(audioStorage.store(file, 1L)).thenReturn(stored);

        when(submissionRepository.save(any())).thenAnswer(i -> {
            StudentSubmission s = i.getArgument(0);
            s.setId(100L);
            return s;
        });

        SpeakingSubmitResponse res = service.submit(10L, file, student);
        assertEquals(100L, res.getJobId());
        assertEquals("PENDING", res.getStatus());
    }

    @Test
    void getResult_gradedSuccess() {
        StudentSubmission submission = StudentSubmission.builder()
                .id(100L)
                .status(StudentSubmission.SubmissionStatus.GRADED)
                .manualScore(BigDecimal.valueOf(90))
                .manualFeedback("Good job")
                .build();
        when(submissionRepository.findByIdAndStudent_Id(100L, 1L)).thenReturn(Optional.of(submission));

        SpeakingResultResponse res = service.getResult(100L, 1L);
        assertEquals("COMPLETED", res.getStatus());
        assertEquals(90, res.getScore());
        assertEquals("Good job", res.getFeedback());
    }
}
