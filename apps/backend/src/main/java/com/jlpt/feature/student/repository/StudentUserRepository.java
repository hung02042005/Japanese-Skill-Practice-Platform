/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.student.repository;

import com.jlpt.feature.student.entity.StudentUser;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface StudentUserRepository extends JpaRepository<StudentUser, Long> {

    Optional<StudentUser> findByEmail(String email);

    boolean existsByEmail(String email);

    /** Đếm học viên theo JLPT level hiện tại (cho Admin report completion rate). */
    long countByCurrentJlptLevel(StudentUser.JlptLevel level);

    /**
     * Admin-only: paginated filter that bypasses @SQLRestriction to include deleted users.
     * Wildcard pattern (%term%) must be passed from the caller for :q.
     */
    @Query(value = """
            SELECT * FROM student_users
            WHERE (:q IS NULL OR full_name LIKE :q OR email LIKE :q)
              AND (:status IS NULL OR LOWER(status) = LOWER(:status))
              AND (:jlptLevel IS NULL OR LOWER(current_jlpt_level) = LOWER(:jlptLevel))
            """,
            countQuery = """
            SELECT COUNT(*) FROM student_users
            WHERE (:q IS NULL OR full_name LIKE :q OR email LIKE :q)
              AND (:status IS NULL OR LOWER(status) = LOWER(:status))
              AND (:jlptLevel IS NULL OR LOWER(current_jlpt_level) = LOWER(:jlptLevel))
            """,
            nativeQuery = true)
    Page<StudentUser> findAllAdminFiltered(
            @Param("q") String q,
            @Param("status") String status,
            @Param("jlptLevel") String jlptLevel,
            Pageable pageable);

    /** Đếm học viên có status chỉ định (cho Admin dashboard). */
    @Query("SELECT COUNT(s) FROM StudentUser s WHERE LOWER(s.status) = LOWER(:status)")
    long countByStatusValue(@Param("status") String status);

    /** Đếm học viên đăng ký trong khoảng thời gian (cho Admin report). */
    @Query("SELECT COUNT(s) FROM StudentUser s WHERE s.createdAt >= :from AND s.createdAt <= :to")
    long countByCreatedAtBetween(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    /** Đếm học viên đăng ký trong tháng hiện tại (cho Admin dashboard). */
    @Query(value = "SELECT COUNT(*) FROM student_users WHERE YEAR(created_at) = YEAR(GETUTCDATE()) AND MONTH(created_at) = MONTH(GETUTCDATE())", nativeQuery = true)
    long countCreatedThisMonth();

    /** Đếm học viên có hoạt động trong ngày hôm nay (cho Admin dashboard summary). */
    @Query(value = "SELECT COUNT(*) FROM student_users WHERE CAST(last_activity_date AS DATE) = CAST(GETUTCDATE() AS DATE)", nativeQuery = true)
    long countActiveToday();
}
