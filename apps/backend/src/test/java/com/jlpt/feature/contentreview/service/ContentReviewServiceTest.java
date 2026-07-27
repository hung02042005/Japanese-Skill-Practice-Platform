/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.contentreview.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.jlpt.feature.contentreview.dto.RequestChangesRequest;
import com.jlpt.feature.contentreview.dto.ReviewActionRequest;
import com.jlpt.feature.contentreview.dto.ReviewQueueResponse;
import com.jlpt.feature.contentreview.dto.ReviewResultResponse;
import com.jlpt.feature.contentreview.dto.ReviewableContentDetailResponse;
import com.jlpt.feature.contentreview.exception.ConcurrentReviewException;
import com.jlpt.feature.contentreview.exception.ContentNotFoundException;
import com.jlpt.feature.contentreview.exception.FeedbackRequiredException;
import com.jlpt.feature.contentreview.exception.SelfReviewNotAllowedException;
import com.jlpt.feature.contentreview.handler.ReviewableContentHandler;
import com.jlpt.feature.contentreview.model.ContentSnapshot;
import com.jlpt.feature.contentreview.model.ContentType;
import com.jlpt.feature.staff.StaffUser;
import com.jlpt.feature.staff.StaffUserRepository;
import com.jlpt.shared.exception.BusinessException;
import com.jlpt.shared.exception.ForbiddenException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit tests cho ContentReviewService.
 */
@ExtendWith(MockitoExtension.class)
class ContentReviewServiceTest {

    @Mock
    private ReviewableContentResolver resolver;

    @Mock
    private StaffUserRepository staffUserRepository;

    @Mock
    private ReviewAuditService reviewAuditService;

    @Mock
    private ReviewableContentHandler handler;

    @InjectMocks
    private ContentReviewService contentReviewService;

    private StaffUser manager;
    private ContentSnapshot snapshot;

    @BeforeEach
    void setUp() {
        manager = StaffUser.builder()
                .id(1L)
                .email("manager@test.com")
                .staffRole(StaffUser.StaffRole.STAFF_MANAGER)
                .status(StaffUser.StaffStatus.ACTIVE)
                .build();

        snapshot = ContentSnapshot.builder()
                .contentId(100L)
                .contentType(ContentType.GRAMMAR)
                .titleOrText("Grammar Point")
                .jlptLevel("N3")
                .status("pending")
                .createdById(2L) // Different from manager.id (1L)
                .createdByName("Staff Author")
                .submittedAt(LocalDateTime.now())
                .build();
    }

    // ── getReviewQueue ────────────────────────────────────────────────────────

    @Test
    void getReviewQueue_notManager_throwsForbiddenException() {
        StaffUser regularStaff = StaffUser.builder()
                .id(3L)
                .email("staff@test.com")
                .staffRole(StaffUser.StaffRole.STAFF)
                .status(StaffUser.StaffStatus.ACTIVE)
                .build();
        when(staffUserRepository.findByEmail("staff@test.com")).thenReturn(Optional.of(regularStaff));

        assertThrows(
                ForbiddenException.class,
                () -> contentReviewService.getReviewQueue("staff@test.com", null, null, 0, 10));
    }

    @Test
    void getReviewQueue_withType_resolvesSpecificHandler() {
        when(staffUserRepository.findByEmail("manager@test.com")).thenReturn(Optional.of(manager));
        when(resolver.resolve(ContentType.GRAMMAR)).thenReturn(handler);
        when(handler.findPending(null)).thenReturn(List.of(snapshot));

        ReviewQueueResponse res = contentReviewService.getReviewQueue("manager@test.com", "grammar", null, 0, 10);

        assertNotNull(res);
        assertEquals(1, res.getTotalElements());
        assertEquals(1, res.getContent().size());
    }

    // ── getContentDetail ──────────────────────────────────────────────────────

    @Test
    void getContentDetail_notFound_throwsContentNotFoundException() {
        when(staffUserRepository.findByEmail("manager@test.com")).thenReturn(Optional.of(manager));
        when(resolver.resolve(ContentType.GRAMMAR)).thenReturn(handler);
        when(handler.findActiveById(100L)).thenReturn(Optional.empty());

        assertThrows(
                ContentNotFoundException.class,
                () -> contentReviewService.getContentDetail("manager@test.com", 100L, "grammar"));
    }

    @Test
    void getContentDetail_success_returnsDetailResponse() {
        when(staffUserRepository.findByEmail("manager@test.com")).thenReturn(Optional.of(manager));
        when(resolver.resolve(ContentType.GRAMMAR)).thenReturn(handler);
        when(handler.findActiveById(100L)).thenReturn(Optional.of(snapshot));

        ReviewableContentDetailResponse res =
                contentReviewService.getContentDetail("manager@test.com", 100L, "grammar");

        assertNotNull(res);
        assertEquals(100L, res.getContentId());
        assertEquals("Grammar Point", res.getTitleOrText());
    }

    // ── review (APPROVE / REJECT) ─────────────────────────────────────────────

    @Test
    void review_selfReview_throwsSelfReviewNotAllowedException() {
        ContentSnapshot selfSnapshot = ContentSnapshot.builder()
                .contentId(100L)
                .contentType(ContentType.GRAMMAR)
                .titleOrText("Grammar Point")
                .jlptLevel("N3")
                .status("pending")
                .createdById(1L) // Same as manager.id (1L)
                .createdByName("Manager")
                .submittedAt(LocalDateTime.now())
                .build();
        when(staffUserRepository.findByEmail("manager@test.com")).thenReturn(Optional.of(manager));
        when(resolver.resolve(ContentType.GRAMMAR)).thenReturn(handler);
        when(handler.findActiveById(100L)).thenReturn(Optional.of(selfSnapshot));

        ReviewActionRequest req = new ReviewActionRequest();
        req.setContentId(100L);
        req.setContentType("grammar");
        req.setAction("approve");

        assertThrows(SelfReviewNotAllowedException.class, () -> contentReviewService.review("manager@test.com", req));
    }

    @Test
    void review_approve_concurrentUpdate0Rows_throwsConcurrentReviewException() {
        when(staffUserRepository.findByEmail("manager@test.com")).thenReturn(Optional.of(manager));
        when(resolver.resolve(ContentType.GRAMMAR)).thenReturn(handler);
        when(handler.findActiveById(100L)).thenReturn(Optional.of(snapshot));
        when(handler.approve(eq(100L), eq(manager), any())).thenReturn(0);

        ReviewActionRequest req = new ReviewActionRequest();
        req.setContentId(100L);
        req.setContentType("grammar");
        req.setAction("approve");

        assertThrows(ConcurrentReviewException.class, () -> contentReviewService.review("manager@test.com", req));
    }

    @Test
    void review_approve_success_returnsPublishedResponse() {
        when(staffUserRepository.findByEmail("manager@test.com")).thenReturn(Optional.of(manager));
        when(resolver.resolve(ContentType.GRAMMAR)).thenReturn(handler);
        when(handler.findActiveById(100L)).thenReturn(Optional.of(snapshot));
        when(handler.approve(eq(100L), eq(manager), any())).thenReturn(1);
        when(handler.tableName()).thenReturn("grammar");

        ReviewActionRequest req = new ReviewActionRequest();
        req.setContentId(100L);
        req.setContentType("grammar");
        req.setAction("approve");
        req.setFeedback("Good work");

        ReviewResultResponse res = contentReviewService.review("manager@test.com", req);

        assertNotNull(res);
        assertEquals("published", res.getStatus());
        verify(reviewAuditService)
                .log(
                        eq(manager),
                        eq(ReviewAuditService.ACTION_APPROVE),
                        eq(ContentType.GRAMMAR),
                        eq("grammar"),
                        eq(100L),
                        eq("Good work"));
    }

    @Test
    void review_reject_noFeedback_throwsFeedbackRequiredException() {
        when(staffUserRepository.findByEmail("manager@test.com")).thenReturn(Optional.of(manager));
        when(resolver.resolve(ContentType.GRAMMAR)).thenReturn(handler);
        when(handler.findActiveById(100L)).thenReturn(Optional.of(snapshot));

        ReviewActionRequest req = new ReviewActionRequest();
        req.setContentId(100L);
        req.setContentType("grammar");
        req.setAction("reject");
        req.setFeedback("  "); // blank feedback

        assertThrows(FeedbackRequiredException.class, () -> contentReviewService.review("manager@test.com", req));
    }

    @Test
    void review_reject_success_returnsRejectedResponse() {
        when(staffUserRepository.findByEmail("manager@test.com")).thenReturn(Optional.of(manager));
        when(resolver.resolve(ContentType.GRAMMAR)).thenReturn(handler);
        when(handler.findActiveById(100L)).thenReturn(Optional.of(snapshot));
        when(handler.transitionFromPending(eq(100L), eq("rejected"), any())).thenReturn(1);
        when(handler.tableName()).thenReturn("grammar");

        ReviewActionRequest req = new ReviewActionRequest();
        req.setContentId(100L);
        req.setContentType("grammar");
        req.setAction("reject");
        req.setFeedback("Needs fix");

        ReviewResultResponse res = contentReviewService.review("manager@test.com", req);

        assertNotNull(res);
        assertEquals("rejected", res.getStatus());
        verify(reviewAuditService)
                .log(
                        eq(manager),
                        eq(ReviewAuditService.ACTION_REJECT),
                        eq(ContentType.GRAMMAR),
                        eq("grammar"),
                        eq(100L),
                        eq("Needs fix"));
    }

    // ── requestChanges ────────────────────────────────────────────────────────

    @Test
    void requestChanges_invalidTargetStatus_throwsBusinessException() {
        when(staffUserRepository.findByEmail("manager@test.com")).thenReturn(Optional.of(manager));

        RequestChangesRequest req = new RequestChangesRequest();
        req.setContentId(100L);
        req.setContentType("grammar");
        req.setFeedback("Fix typos");
        req.setTargetStatus("invalid_status");

        BusinessException ex = assertThrows(
                BusinessException.class, () -> contentReviewService.requestChanges("manager@test.com", req));
        assertEquals(400, ex.getStatus());
    }

    @Test
    void requestChanges_success_transitionsToDraft() {
        when(staffUserRepository.findByEmail("manager@test.com")).thenReturn(Optional.of(manager));
        when(resolver.resolve(ContentType.GRAMMAR)).thenReturn(handler);
        when(handler.findActiveById(100L)).thenReturn(Optional.of(snapshot));
        when(handler.transitionFromPending(eq(100L), eq("draft"), any())).thenReturn(1);
        when(handler.tableName()).thenReturn("grammar");

        RequestChangesRequest req = new RequestChangesRequest();
        req.setContentId(100L);
        req.setContentType("grammar");
        req.setFeedback("Please update example sentences");

        ReviewResultResponse res = contentReviewService.requestChanges("manager@test.com", req);

        assertNotNull(res);
        assertEquals("draft", res.getStatus());
        verify(reviewAuditService)
                .log(
                        eq(manager),
                        eq(ReviewAuditService.ACTION_REQUEST_CHANGES),
                        eq(ContentType.GRAMMAR),
                        eq("grammar"),
                        eq(100L),
                        eq("Please update example sentences"));
    }
}
