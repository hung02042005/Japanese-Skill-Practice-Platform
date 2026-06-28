package com.jlpt.feature.student.kanji;

import com.jlpt.feature.learning.Kanji;
import com.jlpt.feature.learning.Kanji.ContentStatus;
import com.jlpt.feature.student.StudentUser.JlptLevel;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface StudentKanjiRepository extends JpaRepository<Kanji, Long> {

    @Query("SELECT k FROM Kanji k WHERE k.jlptLevel = :level AND k.status = :status")
    Page<Kanji> findByLevelAndStatus(@Param("level") JlptLevel level, @Param("status") ContentStatus status, Pageable pageable);

    @Query("SELECT k FROM Kanji k WHERE k.id = :id AND k.status = :status")
    Optional<Kanji> findByIdAndStatus(@Param("id") Long id, @Param("status") ContentStatus status);

    @Query("SELECT COUNT(k) FROM Kanji k WHERE k.jlptLevel = :level AND k.status = :status")
    long countByLevelAndStatus(@Param("level") JlptLevel level, @Param("status") ContentStatus status);

    Optional<Kanji> findFirstByJlptLevelAndStatusAndIdLessThanOrderByIdDesc(JlptLevel level, ContentStatus status, Long id);
    Optional<Kanji> findFirstByJlptLevelAndStatusAndIdGreaterThanOrderByIdAsc(JlptLevel level, ContentStatus status, Long id);
}
