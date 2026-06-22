/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.student.grammar;

import com.jlpt.feature.learning.GrammarPoint;
import com.jlpt.feature.learning.Kanji;
import com.jlpt.feature.student.StudentUser;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * UC-06 — Repository for Student grammar learning operations.
 */
@Repository
public interface StudentGrammarRepository extends JpaRepository<GrammarPoint, Long> {

    Page<GrammarPoint> findByJlptLevelAndStatus(
            StudentUser.JlptLevel jlptLevel, Kanji.ContentStatus status, Pageable pageable);

    Optional<GrammarPoint> findByIdAndStatus(Long id, Kanji.ContentStatus status);
}
