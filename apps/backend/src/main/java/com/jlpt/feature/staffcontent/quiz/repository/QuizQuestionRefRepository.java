/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.staffcontent.quiz.repository;

import com.jlpt.feature.staffcontent.quiz.entity.QuizQuestionRefEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** Read-only access to `questions` for validating assignable questions (FR-26-21). */
@Repository
public interface QuizQuestionRefRepository extends JpaRepository<QuizQuestionRefEntity, Long> {}
