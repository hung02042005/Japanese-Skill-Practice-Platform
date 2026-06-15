/* (c) JLPT E-Learning Platform */
package com.jlpt.repository;

import com.jlpt.entity.AdminUser;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AdminUserRepository extends JpaRepository<AdminUser, Long> {

    Optional<AdminUser> findByEmail(String email);

    boolean existsByEmail(String email);

    /**
     * Admin-only: paginated filter that bypasses @SQLRestriction.
     * Wildcard pattern (%term%) must be passed from the caller for :q.
     */
    @Query(
            value =
                    """
            SELECT * FROM admin_users
            WHERE (:q IS NULL OR full_name LIKE :q OR email LIKE :q)
              AND (:status IS NULL OR LOWER(status) = LOWER(:status))
            """,
            countQuery =
                    """
            SELECT COUNT(*) FROM admin_users
            WHERE (:q IS NULL OR full_name LIKE :q OR email LIKE :q)
              AND (:status IS NULL OR LOWER(status) = LOWER(:status))
            """,
            nativeQuery = true)
    Page<AdminUser> findAllAdminFiltered(@Param("q") String q, @Param("status") String status, Pageable pageable);
}
