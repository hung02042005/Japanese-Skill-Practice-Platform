/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.learning.kanji;

import com.jlpt.feature.learning.Kanji;
import com.jlpt.feature.student.StudentUser;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

/** UC-07 — Student-facing read access to published kanji. */
@Repository
public interface KanjiRepository extends JpaRepository<Kanji, Long> {

    Page<Kanji> findByJlptLevelAndStatus(StudentUser.JlptLevel jlptLevel, Kanji.ContentStatus status, Pageable pageable);

    Optional<Kanji> findByIdAndStatus(Long id, Kanji.ContentStatus status);

    @Query("SELECT k.id FROM Kanji k WHERE k.jlptLevel = :jlptLevel AND k.status = :status")
    List<Long> findIdsByJlptLevelAndStatus(StudentUser.JlptLevel jlptLevel, Kanji.ContentStatus status);

    Optional<Kanji> findFirstByJlptLevelAndStatusAndIdGreaterThanOrderByIdAsc(
            StudentUser.JlptLevel jlptLevel, Kanji.ContentStatus status, Long id);

    Optional<Kanji> findFirstByJlptLevelAndStatusAndIdLessThanOrderByIdDesc(
            StudentUser.JlptLevel jlptLevel, Kanji.ContentStatus status, Long id);
}
