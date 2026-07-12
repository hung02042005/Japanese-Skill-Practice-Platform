/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.contentreview.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.jlpt.feature.assessment.Assessment;
import com.jlpt.feature.assessment.QuestionAssignment;
import com.jlpt.feature.assessment.QuestionAssignmentRepository;
import com.jlpt.feature.contentreview.repository.ReviewAssessmentRepository;
import com.jlpt.feature.learning.Kanji.ContentStatus;
import com.jlpt.shared.exception.BusinessException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * L3-R1 (PA-3) — chốt xuất bản ở bước manager approve: quiz/exam phải có câu hỏi
 * và tổng điểm khớp {@code totalScore} trước khi lên {@code published}.
 */
@ExtendWith(MockitoExtension.class)
class AssessmentContentHandlerTest {

    @Mock
    private ReviewAssessmentRepository repository;

    @Mock
    private QuestionAssignmentRepository assignmentRepository;

    @InjectMocks
    private AssessmentContentHandler handler;

    @Test
    void approve_rejectsEmptyAssessment() {
        Assessment a = mock(Assessment.class);
        when(repository.findActiveById(1L, ContentStatus.DELETED)).thenReturn(Optional.of(a));
        when(assignmentRepository.countByParentTypeAndParentId(QuestionAssignment.ParentType.ASSESSMENT, 1L))
                .thenReturn(0L);

        BusinessException ex =
                assertThrows(BusinessException.class, () -> handler.approve(1L, null, LocalDateTime.now()));
        assertEquals("ASSESSMENT_EMPTY", ex.getErrorCode());
        verify(repository, never()).approve(anyLong(), any(), any(), any(), any());
    }

    @Test
    void approve_rejectsScoreMismatch() {
        Assessment a = mock(Assessment.class);
        when(a.getTotalScore()).thenReturn(10);
        when(repository.findActiveById(1L, ContentStatus.DELETED)).thenReturn(Optional.of(a));
        when(assignmentRepository.countByParentTypeAndParentId(QuestionAssignment.ParentType.ASSESSMENT, 1L))
                .thenReturn(3L);
        when(assignmentRepository.sumScoreByParent(QuestionAssignment.ParentType.ASSESSMENT, 1L))
                .thenReturn(BigDecimal.valueOf(5));

        BusinessException ex =
                assertThrows(BusinessException.class, () -> handler.approve(1L, null, LocalDateTime.now()));
        assertEquals("ASSESSMENT_SCORE_MISMATCH", ex.getErrorCode());
        verify(repository, never()).approve(anyLong(), any(), any(), any(), any());
    }

    @Test
    void approve_publishesWhenNonEmptyAndScoreMatches() {
        Assessment a = mock(Assessment.class);
        when(a.getTotalScore()).thenReturn(10);
        LocalDateTime now = LocalDateTime.now();
        when(repository.findActiveById(1L, ContentStatus.DELETED)).thenReturn(Optional.of(a));
        when(assignmentRepository.countByParentTypeAndParentId(QuestionAssignment.ParentType.ASSESSMENT, 1L))
                .thenReturn(2L);
        when(assignmentRepository.sumScoreByParent(QuestionAssignment.ParentType.ASSESSMENT, 1L))
                .thenReturn(BigDecimal.TEN);
        when(repository.approve(1L, null, now, ContentStatus.PENDING_REVIEW, ContentStatus.PUBLISHED))
                .thenReturn(1);

        assertEquals(1, handler.approve(1L, null, now));
    }
}
