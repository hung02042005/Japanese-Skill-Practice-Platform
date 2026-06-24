/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.assessment.repository;

import com.jlpt.feature.assessment.entity.AttemptAnswer;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Read-only for analytics: per-question accuracy computation.
 * Owned by feat-assessment; feat-learning-analytics reads only.
 */
@Repository
public interface AttemptAnswerRepository extends JpaRepository<AttemptAnswer, Long> {

    /**
     * Lấy tất cả đáp án của một attempt — cho quiz stats per-question.
     * Dùng để tính correctCount / totalAttempts cho mỗi câu hỏi.
     */
    @Query("""
            SELECT a FROM AttemptAnswer a
            WHERE a.attempt.id IN :attemptIds
            """)
    List<AttemptAnswer> findByAttemptIdIn(@Param("attemptIds") List<Long> attemptIds);

    /**
     * Tính trực tiếp số câu trả lời đúng cho 1 question trên nhiều attempts.
     * Dùng server-side aggregation — không để client tính.
     */
    @Query("""
            SELECT COUNT(a) FROM AttemptAnswer a
            WHERE a.question.id = :questionId
              AND a.attempt.id IN :attemptIds
              AND a.isCorrect = true
            """)
    long countCorrectByQuestionIdAndAttemptIdIn(
            @Param("questionId") Long questionId,
            @Param("attemptIds") List<Long> attemptIds);
}
