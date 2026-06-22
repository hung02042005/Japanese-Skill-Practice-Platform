package com.jlpt.feature.student.reading.repository;

import com.jlpt.feature.assessment.QuestionAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StudentReadingQuestionAssignmentRepository extends JpaRepository<QuestionAssignment, Long> {

    @Query("SELECT qa FROM QuestionAssignment qa JOIN FETCH qa.question WHERE qa.parentType = :parentType AND qa.parentId = :parentId ORDER BY qa.displayOrder ASC")
    List<QuestionAssignment> findByParentTypeAndParentIdOrderByDisplayOrderAsc(
            @Param("parentType") QuestionAssignment.ParentType parentType,
            @Param("parentId") Long parentId
    );
}
