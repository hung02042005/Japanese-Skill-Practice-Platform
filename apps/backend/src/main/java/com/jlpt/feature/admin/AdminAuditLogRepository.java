/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.admin;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AdminAuditLogRepository extends JpaRepository<AdminAuditLog, Long> {

    /** Audit log viewer (admin) — lọc theo action/targetTable, mới nhất trước. */
    @Query("""
            SELECT l FROM AdminAuditLog l
            WHERE (:action IS NULL OR LOWER(l.action) = LOWER(:action))
              AND (:targetTable IS NULL OR LOWER(l.targetTable) = LOWER(:targetTable))
            ORDER BY l.createdAt DESC
            """)
    Page<AdminAuditLog> findByFilters(
            @Param("action") String action,
            @Param("targetTable") String targetTable,
            Pageable pageable);

    /** Recent activity cho dashboard. */
    List<AdminAuditLog> findTop10ByOrderByCreatedAtDesc();

    /** Lấy phản hồi mới nhất (reject/request_changes) cho một nội dung cụ thể. */
    Optional<AdminAuditLog> findFirstByTargetIdAndTargetTableAndActionInOrderByCreatedAtDesc(
            Long targetId, String targetTable, List<String> actions);
}
