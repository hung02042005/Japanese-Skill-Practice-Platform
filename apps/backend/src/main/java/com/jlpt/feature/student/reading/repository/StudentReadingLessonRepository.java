/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.student.reading.repository;

import com.jlpt.feature.learning.Lesson;
import com.jlpt.feature.student.StudentUser.JlptLevel;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface StudentReadingLessonRepository extends JpaRepository<Lesson, Long> {

    @Query("SELECT l FROM Lesson l WHERE l.lessonType = :type AND l.jlptLevel = :level AND l.status = :status")
    Page<Lesson> findByTypeAndLevelAndStatus(
            @Param("type") Lesson.LessonType type,
            @Param("level") JlptLevel level,
            @Param("status") Lesson.LessonStatus status,
            Pageable pageable);

    @Query("SELECT l FROM Lesson l WHERE l.id = :id AND l.lessonType = :type AND l.status = :status")
    Optional<Lesson> findByIdAndTypeAndStatus(
            @Param("id") Long id, @Param("type") Lesson.LessonType type, @Param("status") Lesson.LessonStatus status);

    @Query("SELECT l FROM Lesson l WHERE l.id = :id AND l.status = :status")
    Optional<Lesson> findByIdAndStatus(@Param("id") Long id, @Param("status") Lesson.LessonStatus status);
}
