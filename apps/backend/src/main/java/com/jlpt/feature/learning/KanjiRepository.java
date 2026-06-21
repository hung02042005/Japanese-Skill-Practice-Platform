/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.learning;

import com.jlpt.feature.student.StudentUser;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface KanjiRepository extends JpaRepository<Kanji, Long> {

    @Query(
            """
            SELECT k FROM Kanji k
            WHERE k.status = :status
              AND (:jlptLevel IS NULL OR k.jlptLevel = :jlptLevel)
              AND (k.characterValue LIKE CONCAT('%', :q, '%')
                OR k.meaning LIKE CONCAT('%', :q, '%')
                OR k.onyomi LIKE CONCAT('%', :q, '%')
                OR k.kunyomi LIKE CONCAT('%', :q, '%'))
            ORDER BY k.characterValue ASC
            """)
    List<Kanji> searchPublished(
            @Param("q") String q,
            @Param("jlptLevel") StudentUser.JlptLevel jlptLevel,
            @Param("status") Kanji.ContentStatus status,
            Pageable pageable);
}
