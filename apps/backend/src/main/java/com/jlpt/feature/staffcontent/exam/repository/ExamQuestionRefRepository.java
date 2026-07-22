/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.staffcontent.exam.repository;

import com.jlpt.feature.staffcontent.exam.entity.ExamQuestionRefEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** Read-only access to questions for validating assignable questions (FR-28-21/25). */
@Repository
public interface ExamQuestionRefRepository extends JpaRepository<ExamQuestionRefEntity, Long> {}
