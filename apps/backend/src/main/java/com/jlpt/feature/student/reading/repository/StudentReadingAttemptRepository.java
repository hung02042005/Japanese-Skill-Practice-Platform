package com.jlpt.feature.student.reading.repository;

import com.jlpt.feature.assessment.TestAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface StudentReadingAttemptRepository extends JpaRepository<TestAttempt, Long> {

    @Query("SELECT DISTINCT ta.parentId FROM TestAttempt ta WHERE ta.student.id = :studentId AND ta.parentType = :parentType AND ta.parentId IN :parentIds AND ta.attemptType = :attemptType")
    Set<Long> findAttemptedParentIds(
            @Param("studentId") Long studentId,
            @Param("parentType") TestAttempt.ParentType parentType,
            @Param("parentIds") List<Long> parentIds,
            @Param("attemptType") TestAttempt.AttemptType attemptType
    );
}
