/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.student.reading.repository;

import com.jlpt.feature.assessment.AttemptAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StudentReadingAttemptAnswerRepository extends JpaRepository<AttemptAnswer, Long> {}
