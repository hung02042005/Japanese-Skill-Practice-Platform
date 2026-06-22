package com.jlpt.feature.student.reading.repository;

import com.jlpt.feature.student.StudentUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

@Repository
public interface StudentReadingUserRepository extends JpaRepository<StudentUser, Long> {

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE StudentUser u SET u.lastActivityDate = :date, u.updatedAt = CURRENT_TIMESTAMP WHERE u.id = :id")
    void updateLastActivityDate(@Param("id") Long id, @Param("date") LocalDate date);
}
