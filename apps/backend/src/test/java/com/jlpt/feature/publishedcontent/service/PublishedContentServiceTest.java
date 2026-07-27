/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.publishedcontent.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.jlpt.feature.contentreview.model.ContentType;
import com.jlpt.feature.contentreview.service.ReviewAuditService;
import com.jlpt.feature.publishedcontent.dto.ChangeStatusRequest;
import com.jlpt.feature.publishedcontent.dto.PublishedContentDetailResponse;
import com.jlpt.feature.publishedcontent.dto.PublishedContentListResponse;
import com.jlpt.feature.publishedcontent.dto.ReferenceItemResponse;
import com.jlpt.feature.publishedcontent.dto.RestoreContentRequest;
import com.jlpt.feature.publishedcontent.dto.StatusChangeResultResponse;
import com.jlpt.feature.publishedcontent.exception.ContentNotFoundException;
import com.jlpt.feature.publishedcontent.exception.ResourceInUseException;
import com.jlpt.feature.publishedcontent.exception.RestoreNotAllowedException;
import com.jlpt.feature.publishedcontent.handler.ManagedContentHandler;
import com.jlpt.feature.publishedcontent.model.ManagedContentSnapshot;
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
 * Unit tests cho PublishedContentService.
 */
@ExtendWith(MockitoExtension.class)
class PublishedContentServiceTest {

    @Mock
    private ManagedContentResolver resolver;

    @Mock
    private StaffUserRepository staffUserRepository;

    @Mock
    private ReviewAuditService auditService;

    @Mock
    private ManagedContentHandler handler;

    @InjectMocks
    private PublishedContentService publishedContentService;

    private StaffUser manager;
    private ManagedContentSnapshot snapshot;

    @BeforeEach
    void setUp() {
        manager = StaffUser.builder()
                .id(1L)
                .email("manager@test.com")
                .staffRole(StaffUser.StaffRole.STAFF_MANAGER)
                .status(StaffUser.StaffStatus.ACTIVE)
                .build();

        snapshot = ManagedContentSnapshot.builder()
                .contentId(10L)
                .contentType(ContentType.GRAMMAR)
                .titleOrText("Grammar Published")
                .jlptLevel("N3")
                .status("published")
                .publishedAt(LocalDateTime.now())
                .build();
    }

    // ── getPublishedContents ──────────────────────────────────────────────────

    @Test
    void getPublishedContents_notManager_throwsForbiddenException() {
        StaffUser staff = StaffUser.builder()
                .id(2L)
                .email("staff@test.com")
                .staffRole(StaffUser.StaffRole.STAFF)
                .status(StaffUser.StaffStatus.ACTIVE)
                .build();
        when(staffUserRepository.findByEmail("staff@test.com")).thenReturn(Optional.of(staff));

        assertThrows(
                ForbiddenException.class,
                () -> publishedContentService.getPublishedContents("staff@test.com", "grammar", null, 0, 10));
    }

    @Test
    void getPublishedContents_success_returnsListResponse() {
        when(staffUserRepository.findByEmail("manager@test.com")).thenReturn(Optional.of(manager));
        when(resolver.resolve(ContentType.GRAMMAR)).thenReturn(handler);
        when(handler.findPublished(null)).thenReturn(List.of(snapshot));

        PublishedContentListResponse res =
                publishedContentService.getPublishedContents("manager@test.com", "grammar", null, 0, 10);

        assertNotNull(res);
        assertEquals(1, res.getTotalElements());
        assertEquals(1, res.getContent().size());
    }

    // ── getContentDetail ──────────────────────────────────────────────────────

    @Test
    void getContentDetail_notFound_throwsContentNotFoundException() {
        when(staffUserRepository.findByEmail("manager@test.com")).thenReturn(Optional.of(manager));
        when(resolver.resolve(ContentType.GRAMMAR)).thenReturn(handler);
        when(handler.findById(99L)).thenReturn(Optional.empty());

        assertThrows(
                ContentNotFoundException.class,
                () -> publishedContentService.getContentDetail("manager@test.com", 99L, "grammar"));
    }

    @Test
    void getContentDetail_success_returnsDetailWithReferences() {
        when(staffUserRepository.findByEmail("manager@test.com")).thenReturn(Optional.of(manager));
        when(resolver.resolve(ContentType.GRAMMAR)).thenReturn(handler);
        when(handler.findById(10L)).thenReturn(Optional.of(snapshot));
        when(handler.findBlockingReferences(10L)).thenReturn(List.of());

        PublishedContentDetailResponse res =
                publishedContentService.getContentDetail("manager@test.com", 10L, "grammar");

        assertNotNull(res);
        assertEquals(10L, res.getContentId());
        assertTrue(res.getReferences().isEmpty());
    }

    // ── changeStatus ──────────────────────────────────────────────────────────

    @Test
    void changeStatus_shortReason_throwsBusinessException() {
        when(staffUserRepository.findByEmail("manager@test.com")).thenReturn(Optional.of(manager));

        ChangeStatusRequest req = new ChangeStatusRequest();
        req.setContentType("grammar");
        req.setStatus("archived");
        req.setReason("Short"); // < 10 chars

        BusinessException ex = assertThrows(
                BusinessException.class, () -> publishedContentService.changeStatus("manager@test.com", 10L, req));
        assertEquals(400, ex.getStatus());
    }

    @Test
    void changeStatus_blockingReferencesExist_throwsResourceInUseException() {
        when(staffUserRepository.findByEmail("manager@test.com")).thenReturn(Optional.of(manager));
        when(resolver.resolve(ContentType.GRAMMAR)).thenReturn(handler);
        when(handler.findById(10L)).thenReturn(Optional.of(snapshot));
        ReferenceItemResponse ref = ReferenceItemResponse.builder().build();
        when(handler.findBlockingReferences(10L)).thenReturn(List.of(ref));

        ChangeStatusRequest req = new ChangeStatusRequest();
        req.setContentType("grammar");
        req.setStatus("archived");
        req.setReason("Reason with more than ten chars");

        assertThrows(
                ResourceInUseException.class, () -> publishedContentService.changeStatus("manager@test.com", 10L, req));
    }

    @Test
    void changeStatus_success_updatesStatusAndLogsAudit() {
        when(staffUserRepository.findByEmail("manager@test.com")).thenReturn(Optional.of(manager));
        when(resolver.resolve(ContentType.GRAMMAR)).thenReturn(handler);
        when(handler.findById(10L)).thenReturn(Optional.of(snapshot));
        when(handler.findBlockingReferences(10L)).thenReturn(List.of());
        when(handler.changeStatus(eq(10L), any(), any())).thenReturn(1);
        when(handler.tableName()).thenReturn("grammar");

        ChangeStatusRequest req = new ChangeStatusRequest();
        req.setContentType("grammar");
        req.setStatus("archived");
        req.setReason("Reason with more than ten chars");

        StatusChangeResultResponse res = publishedContentService.changeStatus("manager@test.com", 10L, req);

        assertNotNull(res);
        assertEquals("archived", res.getStatus());
        verify(auditService)
                .log(eq(manager), anyString(), eq(ContentType.GRAMMAR), eq("grammar"), eq(10L), anyString());
    }

    // ── restore ───────────────────────────────────────────────────────────────

    @Test
    void restore_deletedContent_throwsRestoreNotAllowedException() {
        ManagedContentSnapshot deletedSnapshot = ManagedContentSnapshot.builder()
                .contentId(10L)
                .contentType(ContentType.GRAMMAR)
                .status("deleted")
                .build();

        when(staffUserRepository.findByEmail("manager@test.com")).thenReturn(Optional.of(manager));
        when(resolver.resolve(ContentType.GRAMMAR)).thenReturn(handler);
        when(handler.findById(10L)).thenReturn(Optional.of(deletedSnapshot));

        RestoreContentRequest req = new RestoreContentRequest();
        req.setContentType("grammar");

        assertThrows(
                RestoreNotAllowedException.class, () -> publishedContentService.restore("manager@test.com", 10L, req));
    }

    @Test
    void restore_archivedContent_success() {
        ManagedContentSnapshot archivedSnapshot = ManagedContentSnapshot.builder()
                .contentId(10L)
                .contentType(ContentType.GRAMMAR)
                .status("archived")
                .build();

        when(staffUserRepository.findByEmail("manager@test.com")).thenReturn(Optional.of(manager));
        when(resolver.resolve(ContentType.GRAMMAR)).thenReturn(handler);
        when(handler.findById(10L)).thenReturn(Optional.of(archivedSnapshot));
        when(handler.restore(eq(10L), any())).thenReturn(1);
        when(handler.tableName()).thenReturn("grammar");

        RestoreContentRequest req = new RestoreContentRequest();
        req.setContentType("grammar");

        StatusChangeResultResponse res = publishedContentService.restore("manager@test.com", 10L, req);

        assertNotNull(res);
        assertEquals("published", res.getStatus());
        verify(auditService)
                .log(eq(manager), eq("restore_content"), eq(ContentType.GRAMMAR), eq("grammar"), eq(10L), isNull());
    }
}
