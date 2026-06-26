/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.support.repository;

import com.jlpt.feature.support.TicketReply;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TicketReplyRepository extends JpaRepository<TicketReply, Long> {

    @Query("SELECT r FROM TicketReply r WHERE r.ticket.id = :ticketId ORDER BY r.createdAt ASC")
    List<TicketReply> findByTicketIdOrderByCreatedAtAsc(@Param("ticketId") Long ticketId);

    @Query("SELECT COUNT(r) FROM TicketReply r WHERE r.ticket.id = :ticketId")
    long countByTicketId(@Param("ticketId") Long ticketId);
}

