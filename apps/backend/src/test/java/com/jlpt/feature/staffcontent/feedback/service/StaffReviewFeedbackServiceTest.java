/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.staffcontent.feedback.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.jlpt.feature.admin.AdminAuditLog;
import com.jlpt.feature.admin.AdminAuditLogRepository;
import com.jlpt.feature.contentreview.handler.ReviewableContentHandler;
import com.jlpt.feature.contentreview.model.ContentSnapshot;
import com.jlpt.feature.contentreview.model.ContentType;
import com.jlpt.feature.contentreview.service.ReviewableContentResolver;
import com.jlpt.feature.staff.StaffUser;
import com.jlpt.feature.staff.StaffUserRepository;
import com.jlpt.feature.staffcontent.feedback.dto.ReviewFeedbackResponse;
import com.jlpt.shared.exception.BusinessException;
import com.jlpt.shared.exception.ForbiddenException;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit tests cho StaffReviewFeedbackService.
 */
@ExtendWith(MockitoExtension.class)
class StaffReviewFeedbackServiceTest {

    @Mock
    private AdminAuditLogRepository auditLogRepository;

    @Mock
    private ReviewableContentResolver resolver;

    @Mock
    private StaffUserRepository staffUserRepository;

    @Mock
    private ReviewableContentHandler handler;

    @InjectMocks
    private StaffReviewFeedbackService staffReviewFeedbackService;

    private StaffUser staff;

    @BeforeEach
    void setUp() {
        staff = StaffUser.builder().id(1L).email("staff@test.com").build();
    }

    @Test
    void getContentFeedback_notOwner_throwsForbiddenException() {
        ContentSnapshot snapshot =
                ContentSnapshot.builder().contentId(10L).createdById(99L).build();
        when(resolver.resolve(ContentType.GRAMMAR)).thenReturn(handler);
        when(handler.findActiveById(10L)).thenReturn(Optional.of(snapshot));
        when(staffUserRepository.findByEmail("staff@test.com")).thenReturn(Optional.of(staff));

        assertThrows(
                ForbiddenException.class,
                () -> staffReviewFeedbackService.getContentFeedback(10L, "grammar", "staff@test.com"));
    }

    @Test
    void getContentFeedback_feedbackNotFound_throwsBusinessException() {
        ContentSnapshot snapshot =
                ContentSnapshot.builder().contentId(10L).createdById(1L).build();
        when(resolver.resolve(ContentType.GRAMMAR)).thenReturn(handler);
        when(handler.findActiveById(10L)).thenReturn(Optional.of(snapshot));
        when(staffUserRepository.findByEmail("staff@test.com")).thenReturn(Optional.of(staff));
        when(handler.tableName()).thenReturn("grammar");
        when(auditLogRepository.findFirstByTargetIdAndTargetTableAndActionInOrderByCreatedAtDesc(
                        eq(10L), eq("grammar"), any()))
                .thenReturn(Optional.empty());

        BusinessException ex = assertThrows(
                BusinessException.class,
                () -> staffReviewFeedbackService.getContentFeedback(10L, "grammar", "staff@test.com"));
        assertEquals(404, ex.getStatus());
        assertEquals("FEEDBACK_NOT_FOUND", ex.getErrorCode());
    }

    @Test
    void getContentFeedback_isOwner_returnsFeedbackResponse() {
        ContentSnapshot snapshot =
                ContentSnapshot.builder().contentId(10L).createdById(1L).build();
        when(resolver.resolve(ContentType.GRAMMAR)).thenReturn(handler);
        when(handler.findActiveById(10L)).thenReturn(Optional.of(snapshot));
        when(staffUserRepository.findByEmail("staff@test.com")).thenReturn(Optional.of(staff));
        when(handler.tableName()).thenReturn("grammar");

        AdminAuditLog auditLog = AdminAuditLog.builder()
                .action("reject")
                .description("Fix typo in section 2")
                .createdAt(LocalDateTime.now())
                .build();
        when(auditLogRepository.findFirstByTargetIdAndTargetTableAndActionInOrderByCreatedAtDesc(
                        eq(10L), eq("grammar"), any()))
                .thenReturn(Optional.of(auditLog));

        ReviewFeedbackResponse res = staffReviewFeedbackService.getContentFeedback(10L, "grammar", "staff@test.com");

        assertNotNull(res);
        assertEquals("Fix typo in section 2", res.getFeedback());
        assertEquals("reject", res.getActionType());
    }
}
