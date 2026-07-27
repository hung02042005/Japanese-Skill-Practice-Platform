/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.staff;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.jlpt.feature.staff.dto.response.StaffMemberResponse;
import com.jlpt.feature.support.repository.TicketRepository;
import com.jlpt.shared.exception.ForbiddenException;
import com.jlpt.shared.exception.ResourceNotFoundException;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit tests cho StaffMemberService.
 */
@ExtendWith(MockitoExtension.class)
class StaffMemberServiceTest {

    @Mock
    private StaffUserRepository staffUserRepository;

    @Mock
    private TicketRepository ticketRepository;

    @InjectMocks
    private StaffMemberService staffMemberService;

    @Test
    void listAssignableStaff_userNotFound_throwsResourceNotFoundException() {
        when(staffUserRepository.findByEmail("unknown@test.com")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> staffMemberService.listAssignableStaff("unknown@test.com"));
    }

    @Test
    void listAssignableStaff_notManager_throwsForbiddenException() {
        StaffUser regularStaff = StaffUser.builder()
                .id(1L)
                .email("staff@test.com")
                .staffRole(StaffUser.StaffRole.STAFF)
                .build();
        when(staffUserRepository.findByEmail("staff@test.com")).thenReturn(Optional.of(regularStaff));

        assertThrows(ForbiddenException.class, () -> staffMemberService.listAssignableStaff("staff@test.com"));
    }

    @Test
    void listAssignableStaff_isManager_returnsActiveStaffList() {
        StaffUser manager = StaffUser.builder()
                .id(1L)
                .email("manager@test.com")
                .staffRole(StaffUser.StaffRole.STAFF_MANAGER)
                .build();
        StaffUser activeStaff = StaffUser.builder()
                .id(2L)
                .email("staff2@test.com")
                .fullName("Staff Two")
                .staffRole(StaffUser.StaffRole.STAFF)
                .status(StaffUser.StaffStatus.ACTIVE)
                .build();
        StaffUser inactiveStaff = StaffUser.builder()
                .id(3L)
                .email("staff3@test.com")
                .status(StaffUser.StaffStatus.SUSPENDED)
                .build();

        when(staffUserRepository.findByEmail("manager@test.com")).thenReturn(Optional.of(manager));
        when(staffUserRepository.findAll()).thenReturn(List.of(manager, activeStaff, inactiveStaff));
        when(ticketRepository.countByAssignedToIdAndStatusNotIn(any(), any())).thenReturn(2L);

        List<StaffMemberResponse> res = staffMemberService.listAssignableStaff("manager@test.com");

        assertNotNull(res);
        // manager is also ACTIVE, so activeStaff + manager = 2
        assertEquals(2, res.size());
    }
}
