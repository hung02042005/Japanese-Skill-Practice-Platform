/* (c) JLPT E-Learning Platform */
package com.jlpt.shared.email;

import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmailOutboxRepository extends JpaRepository<EmailOutbox, Long> {

    List<EmailOutbox> findByStatusOrderByCreatedAtAsc(EmailOutbox.Status status, Pageable pageable);
}
