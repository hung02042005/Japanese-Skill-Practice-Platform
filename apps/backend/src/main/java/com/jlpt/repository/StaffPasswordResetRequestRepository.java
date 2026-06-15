/* (c) JLPT E-Learning Platform */
package com.jlpt.repository;

import com.jlpt.entity.StaffPasswordResetRequest;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StaffPasswordResetRequestRepository extends JpaRepository<StaffPasswordResetRequest, Long> {

    long countByStaffIdAndRequestedAtAfter(Long staffId, LocalDateTime requestedAfter);

    List<StaffPasswordResetRequest> findByStatusOrderByRequestedAtDesc(StaffPasswordResetRequest.ResetStatus status);
}
