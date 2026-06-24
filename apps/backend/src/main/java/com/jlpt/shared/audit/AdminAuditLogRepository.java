/* (c) JLPT E-Learning Platform */
package com.jlpt.shared.audit;

import com.jlpt.shared.audit.AdminAuditLog;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AdminAuditLogRepository extends JpaRepository<AdminAuditLog, Long> {

    /** Lấy 5 hoạt động gần nhất cho Admin dashboard. */
    List<AdminAuditLog> findTop5ByOrderByCreatedAtDesc();

    /** Paginated audit log with optional action filter, sorted via Pageable. */
    @Query("""
            SELECT l FROM AdminAuditLog l
            WHERE :action IS NULL OR l.action = :action
            """)
    Page<AdminAuditLog> findAllByActionFilter(
            @Param("action") String action,
            Pageable pageable);
}
