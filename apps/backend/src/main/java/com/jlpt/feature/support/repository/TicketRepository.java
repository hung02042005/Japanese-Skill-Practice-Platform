/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.support.repository;

import com.jlpt.feature.support.Ticket;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {

    @Query("SELECT t FROM Ticket t WHERE t.student.id = :studentId ORDER BY t.createdAt DESC")
    Page<Ticket> findByStudentId(@Param("studentId") Long studentId, Pageable pageable);

    @Query("SELECT t FROM Ticket t WHERE t.student.id = :studentId AND t.status = :status ORDER BY t.createdAt DESC")
    Page<Ticket> findByStudentIdAndStatus(
            @Param("studentId") Long studentId,
            @Param("status") Ticket.TicketStatus status,
            Pageable pageable);

    @Query("""
            SELECT t FROM Ticket t
            WHERE (:status IS NULL OR t.status = :status)
              AND (:category IS NULL OR LOWER(t.category) = LOWER(:category))
              AND (:priority IS NULL OR t.priority = :priority)
              AND (:q IS NULL
                   OR LOWER(t.subject) LIKE LOWER(CONCAT('%',:q,'%'))
                   OR LOWER(t.student.email) LIKE LOWER(CONCAT('%',:q,'%'))
                   OR LOWER(t.student.fullName) LIKE LOWER(CONCAT('%',:q,'%')))
            ORDER BY t.createdAt DESC
            """)
    Page<Ticket> findAllByFilters(
            @Param("status") Ticket.TicketStatus status,
            @Param("category") String category,
            @Param("priority") Ticket.Priority priority,
            @Param("q") String q,
            Pageable pageable);

    long countByStatus(Ticket.TicketStatus status);

    @Query("SELECT COUNT(t) FROM Ticket t WHERE t.assignedTo.id = :staffId AND t.status = :status")
    long countByAssignedToIdAndStatus(
            @Param("staffId") Long staffId,
            @Param("status") Ticket.TicketStatus status);
}

