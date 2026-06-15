/* (c) JLPT E-Learning Platform */
package com.jlpt.repository;

import com.jlpt.entity.StudentUser;
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

    /**
     * Admin-only: paginated filter that bypasses @SQLRestriction to include deleted users.
     * Wildcard pattern (%term%) must be passed from the caller for :q.
     */
    @Query(
            value =
                    """
            SELECT * FROM student_users
            WHERE (:q IS NULL OR full_name LIKE :q OR email LIKE :q)
              AND (:status IS NULL OR LOWER(status) = LOWER(:status))
              AND (:jlptLevel IS NULL OR LOWER(current_jlpt_level) = LOWER(:jlptLevel))
            """,
            countQuery =
                    """
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
}
