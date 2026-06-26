/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.notification;

import java.time.LocalDateTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    @Query("SELECT n FROM Notification n WHERE n.student.id = :studentId ORDER BY n.createdAt DESC")
    Page<Notification> findByStudentId(@Param("studentId") Long studentId, Pageable pageable);

    @Query("SELECT COUNT(n) FROM Notification n WHERE n.student.id = :studentId AND n.isRead = false")
    long countUnreadByStudentId(@Param("studentId") Long studentId);

    @Modifying
    @Query("""
            UPDATE Notification n
            SET n.isRead = true, n.readAt = :readAt
            WHERE n.student.id = :studentId AND n.isRead = false
            """)
    int markAllReadByStudentId(@Param("studentId") Long studentId, @Param("readAt") LocalDateTime readAt);

    boolean existsByStudentIdAndRuleKeyAndCreatedAtAfter(
            Long studentId, String ruleKey, LocalDateTime createdAt);
}
