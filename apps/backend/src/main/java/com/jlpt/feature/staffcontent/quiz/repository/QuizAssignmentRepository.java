/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.staffcontent.quiz.repository;

import com.jlpt.feature.staffcontent.quiz.entity.QuizAssignmentEntity;

import java.math.BigDecimal;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface QuizAssignmentRepository extends JpaRepository<QuizAssignmentEntity, Long> {

    /** Assignments of a quiz, ordered by display order (FR-26-13). */
    List<QuizAssignmentEntity> findByParentTypeAndParentIdOrderByDisplayOrderAsc(String parentType, Long parentId);

    long countByParentTypeAndParentId(String parentType, Long parentId);

    /** Σ score for the score invariant (FR-26-14/26). Returns 0 when there are no assignments. */
    @Query("SELECT COALESCE(SUM(a.score), 0) FROM QuizAssignmentEntity a "
            + "WHERE a.parentType = :parentType AND a.parentId = :parentId")
    BigDecimal sumScoreByParent(@Param("parentType") String parentType, @Param("parentId") Long parentId);

    /** Replace semantics: clear current assignments before inserting the new set (FR-26-23). */
    @Modifying
    @Query("DELETE FROM QuizAssignmentEntity a WHERE a.parentType = :parentType AND a.parentId = :parentId")
    void deleteByParent(@Param("parentType") String parentType, @Param("parentId") Long parentId);
}
