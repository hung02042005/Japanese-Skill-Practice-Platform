/* (c) JLPT E-Learning Platform */
package com.jlpt.repository;

import com.jlpt.entity.Lesson;
import com.jlpt.entity.StudentUser;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface LessonRepository extends JpaRepository<Lesson, Long> {

    @Query(
            """
            SELECT l FROM Lesson l
            WHERE l.status = :status
              AND (:jlptLevel IS NULL OR l.jlptLevel = :jlptLevel)
              AND l.title LIKE CONCAT('%', :q, '%')
            ORDER BY l.title ASC
            """)
    List<Lesson> searchPublished(
            @Param("q") String q,
            @Param("jlptLevel") StudentUser.JlptLevel jlptLevel,
            @Param("status") Lesson.LessonStatus status,
            Pageable pageable);
}
