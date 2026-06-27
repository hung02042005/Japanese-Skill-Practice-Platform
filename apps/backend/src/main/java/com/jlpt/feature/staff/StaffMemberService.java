/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.staff;

import com.jlpt.feature.staff.dto.response.StaffMemberResponse;
import com.jlpt.feature.support.Ticket;
import com.jlpt.feature.support.repository.TicketRepository;
import com.jlpt.shared.exception.ForbiddenException;
import com.jlpt.shared.exception.ResourceNotFoundException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Danh sach nhan vien co the duoc giao ticket — chi Staff Manager truy cap (UC-29). */
@Service
@RequiredArgsConstructor
public class StaffMemberService {

    private static final List<Ticket.TicketStatus> CLOSED_STATUSES =
            List.of(Ticket.TicketStatus.RESOLVED, Ticket.TicketStatus.CLOSED);

    private final StaffUserRepository staffUserRepository;
    private final TicketRepository ticketRepository;

    @Transactional(readOnly = true)
    public List<StaffMemberResponse> listAssignableStaff(String actorEmail) {
        StaffUser actor = staffUserRepository
                .findByEmail(actorEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Khong tim thay nhan vien: " + actorEmail));
        if (actor.getStaffRole() != StaffUser.StaffRole.STAFF_MANAGER) {
            throw new ForbiddenException("Chi Staff Manager duoc xem danh sach phan cong");
        }

        return staffUserRepository.findAll().stream()
                .filter(s -> s.getStatus() == StaffUser.StaffStatus.ACTIVE)
                .map(s -> StaffMemberResponse.builder()
                        .staffId(s.getId())
                        .fullName(s.getFullName())
                        .email(s.getEmail())
                        .staffRole(s.getStaffRole().getValue())
                        .assignedOpenCount(
                                ticketRepository.countByAssignedToIdAndStatusNotIn(s.getId(), CLOSED_STATUSES))
                        .build())
                .toList();
    }
}
