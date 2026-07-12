/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.support.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import com.jlpt.feature.staff.StaffUser;
import com.jlpt.feature.staff.StaffUserRepository;
import com.jlpt.feature.support.Ticket;
import com.jlpt.feature.support.repository.TicketRepository;
import com.jlpt.shared.exception.BusinessException;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** L3-R2 — assignTicket không được giao cho nhân viên đang không hoạt động. */
@ExtendWith(MockitoExtension.class)
class SupportTicketAssignTest {

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private StaffUserRepository staffUserRepository;

    @InjectMocks
    private SupportTicketService service;

    @Test
    void assignTicket_rejectsSuspendedTarget() {
        Ticket ticket = Ticket.builder().id(9L).status(Ticket.TicketStatus.OPEN).build();
        when(ticketRepository.findById(9L)).thenReturn(Optional.of(ticket));

        StaffUser target = StaffUser.builder()
                .id(2L)
                .status(StaffUser.StaffStatus.SUSPENDED)
                .build();
        when(staffUserRepository.findById(2L)).thenReturn(Optional.of(target));

        BusinessException ex =
                assertThrows(BusinessException.class, () -> service.assignTicket(9L, 2L, "admin@x.com", true));
        assertEquals("STAFF_NOT_ACTIVE", ex.getErrorCode());
    }
}
