/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.notification;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    /** Thong bao da den han hien thi (scheduledAt rong hoac <= now) cua 1 student. */
    @Query("""
            SELECT n FROM Notification n
            WHERE n.student.id = :studentId
              AND (n.scheduledAt IS NULL OR n.scheduledAt <= :now)
            ORDER BY n.createdAt DESC
            """)
    Page<Notification> findVisibleByStudentId(
            @Param("studentId") Long studentId,
            @Param("now") LocalDateTime now,
            Pageable pageable);

    @Query("""
            SELECT COUNT(n) FROM Notification n
            WHERE n.student.id = :studentId
              AND n.isRead = false
              AND (n.scheduledAt IS NULL OR n.scheduledAt <= :now)
            """)
    long countUnreadVisibleByStudentId(
            @Param("studentId") Long studentId, @Param("now") LocalDateTime now);

    @Modifying
    @Query("""
            UPDATE Notification n
            SET n.isRead = true, n.readAt = :readAt
            WHERE n.student.id = :studentId AND n.isRead = false
            """)
    int markAllReadByStudentId(@Param("studentId") Long studentId, @Param("readAt") LocalDateTime readAt);

    /** Thong bao kenh email/both da den han ma chua gui email — phuc vu scheduler. */
    @Query("""
            SELECT n FROM Notification n
            WHERE n.channel IN :channels
              AND n.sentAt IS NULL
              AND (n.scheduledAt IS NULL OR n.scheduledAt <= :now)
            ORDER BY n.createdAt ASC
            """)
    List<Notification> findDuePendingEmails(
            @Param("channels") Collection<Notification.Channel> channels,
            @Param("now") LocalDateTime now,
            Pageable pageable);
}
