/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.staff.repository;

import com.jlpt.feature.staff.entity.StaffUser;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface StaffUserRepository extends JpaRepository<StaffUser, Long> {

    Optional<StaffUser> findByEmail(String email);

    boolean existsByEmail(String email);

    /**
     * Admin-only: paginated filter that bypasses @SQLRestriction to include deleted users.
     * Wildcard pattern (%term%) must be passed from the caller for :q.
     */
    @Query(value = """
            SELECT * FROM staff_users
            WHERE (:q IS NULL OR full_name LIKE :q OR email LIKE :q)
              AND (:status IS NULL OR LOWER(status) = LOWER(:status))
              AND (:staffRole IS NULL OR LOWER(staff_role) = LOWER(:staffRole))
            """,
            countQuery = """
            SELECT COUNT(*) FROM staff_users
            WHERE (:q IS NULL OR full_name LIKE :q OR email LIKE :q)
              AND (:status IS NULL OR LOWER(status) = LOWER(:status))
              AND (:staffRole IS NULL OR LOWER(staff_role) = LOWER(:staffRole))
            """,
            nativeQuery = true)
    Page<StaffUser> findAllAdminFiltered(
            @Param("q") String q,
            @Param("status") String status,
            @Param("staffRole") String staffRole,
            Pageable pageable);
}
