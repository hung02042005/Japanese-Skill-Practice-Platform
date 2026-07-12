/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.staffcontent.quiz;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import com.jlpt.feature.learning.LessonRepository;
import com.jlpt.feature.staff.StaffUser;
import com.jlpt.feature.staff.StaffUserRepository;
import com.jlpt.feature.staffcontent.quiz.exception.QuizBusinessException;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** L3-R1 (PA-3) — submitForReview chặn cứng quiz rỗng (FR-26-28). */
@ExtendWith(MockitoExtension.class)
class StaffQuizServiceSubmitReviewTest {

    @Mock
    private QuizAssessmentRepository assessmentRepository;

    @Mock
    private QuizAssignmentRepository assignmentRepository;

    @Mock
    private QuizQuestionRefRepository questionRefRepository;

    @Mock
    private LessonRepository lessonRepository;

    @Mock
    private StaffUserRepository staffUserRepository;

    @InjectMocks
    private StaffQuizService service;

    @Test
    void submitForReview_rejectsEmptyQuiz() {
        StaffUser staff =
                StaffUser.builder().id(1L).staffRole(StaffUser.StaffRole.STAFF).build();
        when(staffUserRepository.findByEmail("s@x.com")).thenReturn(Optional.of(staff));

        QuizAssessmentEntity quiz = QuizAssessmentEntity.builder()
                .id(5L)
                .createdBy(1L)
                .status("draft")
                .totalScore(10)
                .build();
        when(assessmentRepository.findByIdAndAssessmentTypeAndStatusNot(5L, "quiz", "deleted"))
                .thenReturn(Optional.of(quiz));
        when(assignmentRepository.countByParentTypeAndParentId("assessment", 5L))
                .thenReturn(0L);

        QuizBusinessException ex =
                assertThrows(QuizBusinessException.class, () -> service.submitForReview(5L, "s@x.com"));
        assertEquals("EMPTY_QUIZ", ex.getErrorCode());
    }
}
