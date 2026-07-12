/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.admin;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.jlpt.feature.staff.StaffUser;
import com.jlpt.feature.staff.StaffUserRepository;
import com.jlpt.feature.staff.dto.request.ChangeStaffRoleRequest;
import com.jlpt.shared.exception.BusinessRuleException;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** L3-R2 — không được đổi vai trò nhân viên đang bị đình chỉ. */
@ExtendWith(MockitoExtension.class)
class AdminUserServiceChangeRoleTest {

    @Mock
    private StaffUserRepository staffUserRepository;

    @Mock
    private AdminUserRepository adminUserRepository;

    @InjectMocks
    private AdminUserService service;

    @Test
    void changeStaffRole_rejectsSuspendedStaff() {
        when(adminUserRepository.findByEmail("a@x.com")).thenReturn(Optional.of(mock(AdminUser.class)));

        StaffUser staff = StaffUser.builder()
                .id(2L)
                .staffRole(StaffUser.StaffRole.STAFF)
                .status(StaffUser.StaffStatus.SUSPENDED)
                .build();
        when(staffUserRepository.findById(2L)).thenReturn(Optional.of(staff));

        ChangeStaffRoleRequest req = new ChangeStaffRoleRequest();
        req.setStaffRole("staff_manager");

        assertThrows(BusinessRuleException.class, () -> service.changeStaffRole("a@x.com", 2L, req));
    }
}
