/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.staffcontent.question;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * UC-24 — Service interface for Staff question-bank operations.
 */
public interface StaffQuestionService {

    /**
     * Create a new question in draft status (FR-24-01).
     */
    QuestionResponse createQuestion(CreateQuestionRequest request, String staffEmail);

    /**
     * List / search / filter questions with pagination (FR-24-10/11/12/13).
     */
    Page<QuestionResponse> listQuestions(
            String q, String skill, String jlptLevel, String questionType, String status, Pageable pageable);

    /**
     * Get question detail with isLocked flag (FR-24-14/15).
     */
    QuestionResponse getQuestion(Long questionId);

    /**
     * Update an existing question (FR-24-16/17/18/19). Rejected if locked or wrong status.
     */
    QuestionResponse updateQuestion(Long questionId, UpdateQuestionRequest request, String staffEmail);

    /**
     * Submit a question for review (FR-24-20/21/22/23).
     * Transitions draft/rejected → pending_review.
     */
    StaffQuestionSubmitReviewResponse submitForReview(Long questionId, String staffEmail);
}
