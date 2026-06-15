/* (c) JLPT E-Learning Platform */
package com.jlpt.repository;

import com.jlpt.entity.GrammarPoint;
import com.jlpt.entity.Kanji;
import com.jlpt.entity.StudentUser;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface GrammarPointRepository extends JpaRepository<GrammarPoint, Long> {

    @Query(
            """
            SELECT g FROM GrammarPoint g
            WHERE g.status = :status
              AND (:jlptLevel IS NULL OR g.jlptLevel = :jlptLevel)
              AND (g.structure LIKE CONCAT('%', :q, '%')
                OR g.meaning LIKE CONCAT('%', :q, '%'))
            ORDER BY g.structure ASC
            """)
    List<GrammarPoint> searchPublished(
            @Param("q") String q,
            @Param("jlptLevel") StudentUser.JlptLevel jlptLevel,
            @Param("status") Kanji.ContentStatus status,
            Pageable pageable);
}
