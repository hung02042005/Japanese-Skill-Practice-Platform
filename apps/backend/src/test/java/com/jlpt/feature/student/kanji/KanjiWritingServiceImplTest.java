/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.student.kanji;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.jlpt.feature.student.StudentUser;
import com.jlpt.feature.student.StudentUserRepository;
import com.jlpt.feature.student.kanji.dto.KanjiWritingAttemptRequest;
import com.jlpt.feature.student.kanji.dto.KanjiWritingAttemptResponse;
import com.jlpt.feature.student.kanji.dto.KanjiWritingEvaluateRequest;
import com.jlpt.feature.student.kanji.dto.KanjiWritingEvaluateResponse;
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
 * Unit tests cho KanjiWritingServiceImpl — DTW evaluation và attempt recording.
 */
@ExtendWith(MockitoExtension.class)
class KanjiWritingServiceImplTest {

    @Mock
    private KanjiWritingAttemptRepository attemptRepository;

    @Mock
    private StudentUserRepository studentUserRepository;

    @InjectMocks
    private KanjiWritingServiceImpl kanjiWritingService;

    private StudentUser student;

    @BeforeEach
    void setUp() {
        student = StudentUser.builder().id(1L).email("student@test.com").build();
    }

    // ── evaluateStroke ────────────────────────────────────────────────────────

    @Test
    void evaluateStroke_shortPath_returnsSkippedEvaluation() {
        KanjiWritingEvaluateRequest req = new KanjiWritingEvaluateRequest();
        req.setUserPath(List.of(List.of(0.0, 0.0))); // Only 1 point (< 2)
        req.setReferencePath(List.of(List.of(0.0, 0.0), List.of(10.0, 10.0)));
        req.setStrokeIndex(0);

        KanjiWritingEvaluateResponse res = kanjiWritingService.evaluateStroke(req);

        assertNotNull(res);
        assertEquals(0.0, res.getDtwScore());
        assertEquals("ok", res.getQuality());
    }

    @Test
    void evaluateStroke_validPaths_computesDtwAndQuality() {
        KanjiWritingEvaluateRequest req = new KanjiWritingEvaluateRequest();
        req.setUserPath(List.of(List.of(0.0, 0.0), List.of(10.0, 0.0)));
        req.setReferencePath(List.of(List.of(0.0, 0.0), List.of(10.0, 0.0)));
        req.setStrokeIndex(0);

        KanjiWritingEvaluateResponse res = kanjiWritingService.evaluateStroke(req);

        assertNotNull(res);
        assertEquals("perfect", res.getQuality());
        assertEquals("Hoàn hảo!", res.getFeedbackMsg());
    }

    // ── saveAttempt ───────────────────────────────────────────────────────────

    @Test
    void saveAttempt_studentNotFound_throwsResourceNotFoundException() {
        when(studentUserRepository.findById(99L)).thenReturn(Optional.empty());
        KanjiWritingAttemptRequest req = new KanjiWritingAttemptRequest();

        assertThrows(ResourceNotFoundException.class, () -> kanjiWritingService.saveAttempt(req, 99L));
    }

    @Test
    void saveAttempt_success_calculatesAvgDtwAndSavesAttempt() {
        when(studentUserRepository.findById(1L)).thenReturn(Optional.of(student));
        when(attemptRepository.save(any())).thenAnswer(i -> {
            KanjiWritingAttempt a = i.getArgument(0);
            a.setId(500L);
            return a;
        });

        KanjiWritingAttemptRequest.StrokeResult sr = new KanjiWritingAttemptRequest.StrokeResult();
        sr.setStrokeIndex(0);
        sr.setDtwScore(100.0);
        sr.setQuality("perfect");

        KanjiWritingAttemptRequest req = new KanjiWritingAttemptRequest();
        req.setKanjiId(10L);
        req.setCharacterValue("日");
        req.setTotalStrokes(4);
        req.setStrokes(List.of(sr));

        KanjiWritingAttemptResponse res = kanjiWritingService.saveAttempt(req, 1L);

        assertNotNull(res);
        assertEquals(500L, res.getAttemptId());
        assertEquals(100.0, res.getAvgDtwScore());
        assertEquals("perfect", res.getFinalQuality());
        verify(attemptRepository).save(any());
    }
}
