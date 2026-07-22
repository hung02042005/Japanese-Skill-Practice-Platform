/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.staffcontent.exam.repository;

import com.jlpt.feature.staffcontent.exam.entity.ExamAssignmentEntity;

import java.math.BigDecimal;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ExamAssignmentRepository extends JpaRepository<ExamAssignmentEntity, Long> {

    /** Assignments of an exam, ordered by display order (FR-28-13). */
    List<ExamAssignmentEntity> findByParentTypeAndParentIdOrderByDisplayOrderAsc(String parentType, Long parentId);

    long countByParentTypeAndParentId(String parentType, Long parentId);

    /** Σ score for the score invariant (FR-28-14/30). Returns 0 when there are no assignments. */
    @Query("SELECT COALESCE(SUM(a.score), 0) FROM ExamAssignmentEntity a "
            + "WHERE a.parentType = :parentType AND a.parentId = :parentId")
    BigDecimal sumScoreByParent(@Param("parentType") String parentType, @Param("parentId") Long parentId);

    /** Replace semantics: clear current assignments before inserting the new set (FR-28-23). */
    @Modifying
    @Query("DELETE FROM ExamAssignmentEntity a WHERE a.parentType = :parentType AND a.parentId = :parentId")
    void deleteByParent(@Param("parentType") String parentType, @Param("parentId") Long parentId);

    /** Group assignments by section_name with their aggregate score (FR-28-13). */
    @Query("SELECT a.sectionName, SUM(a.score), COUNT(a) FROM ExamAssignmentEntity a "
            + "WHERE a.parentType = :parentType AND a.parentId = :parentId "
            + "GROUP BY a.sectionName ORDER BY a.sectionName")
    List<Object[]> sumBySectionGrouped(@Param("parentType") String parentType, @Param("parentId") Long parentId);
}
