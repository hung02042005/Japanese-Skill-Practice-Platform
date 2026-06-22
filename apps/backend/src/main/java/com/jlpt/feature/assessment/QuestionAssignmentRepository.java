/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.assessment;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface QuestionAssignmentRepository extends JpaRepository<QuestionAssignment, Long> {

    @Query(
            """
            SELECT qa FROM QuestionAssignment qa
            JOIN FETCH qa.question q
            WHERE qa.parentType = :parentType AND qa.parentId = :parentId
            ORDER BY qa.displayOrder ASC
            """)
    List<QuestionAssignment> findByParentTypeAndParentIdOrderByDisplayOrder(
            @Param("parentType") QuestionAssignment.ParentType parentType, @Param("parentId") Long parentId);

    long countByParentTypeAndParentId(QuestionAssignment.ParentType parentType, Long parentId);
}
