/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.contentreview.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

import com.jlpt.feature.admin.AdminAuditLog;
import com.jlpt.feature.admin.AdminAuditLogRepository;
import com.jlpt.feature.contentreview.model.ContentType;
import com.jlpt.feature.staff.StaffUser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit tests cho ReviewAuditService.
 */
@ExtendWith(MockitoExtension.class)
class ReviewAuditServiceTest {

    @Mock
    private AdminAuditLogRepository adminAuditLogRepository;

    @InjectMocks
    private ReviewAuditService reviewAuditService;

    @Test
    void log_savesAuditRecordWithCorrectFields() {
        StaffUser manager = StaffUser.builder().id(1L).email("manager@test.com").build();

        reviewAuditService.log(
                manager, ReviewAuditService.ACTION_APPROVE, ContentType.GRAMMAR, "grammar", 100L, "Looks good");

        verify(adminAuditLogRepository).save(any(AdminAuditLog.class));
    }
}
