/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.assessment;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AttemptAnswerRepository extends JpaRepository<AttemptAnswer, Long> {

    @Query(
            """
            SELECT a FROM AttemptAnswer a
            JOIN FETCH a.question
            WHERE a.attempt.id = :attemptId
            ORDER BY a.id ASC
            """)
    List<AttemptAnswer> findByAttemptIdWithQuestion(@Param("attemptId") Long attemptId);
}
