/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.publishedcontent.service;

import com.jlpt.feature.contentreview.model.ContentType;
import com.jlpt.feature.contentreview.service.ReviewAuditService;
import com.jlpt.feature.publishedcontent.dto.ChangeStatusRequest;
import com.jlpt.feature.publishedcontent.dto.PublishedContentDetailResponse;
import com.jlpt.feature.publishedcontent.dto.PublishedContentItemResponse;
import com.jlpt.feature.publishedcontent.dto.PublishedContentListResponse;
import com.jlpt.feature.publishedcontent.dto.ReferenceItemResponse;
import com.jlpt.feature.publishedcontent.dto.RestoreContentRequest;
import com.jlpt.feature.publishedcontent.dto.StatusChangeResultResponse;
import com.jlpt.feature.publishedcontent.exception.ContentNotFoundException;
import com.jlpt.feature.publishedcontent.exception.InvalidStateTransitionException;
import com.jlpt.feature.publishedcontent.exception.ResourceInUseException;
import com.jlpt.feature.publishedcontent.exception.RestoreNotAllowedException;
import com.jlpt.feature.publishedcontent.handler.ManagedContentHandler;
import com.jlpt.feature.publishedcontent.model.ManagedContentSnapshot;
import com.jlpt.feature.publishedcontent.model.TargetStatus;
import com.jlpt.feature.staff.StaffUser;
import com.jlpt.feature.staff.StaffUserRepository;
import com.jlpt.feature.student.StudentUser.JlptLevel;
import com.jlpt.shared.exception.BusinessException;
import com.jlpt.shared.exception.ForbiddenException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * UC-34 — Quản lý vòng đời sau xuất bản của nội dung cho StaffManager.
 *
 * <p>Thực thi: chỉ {@code staff_manager} đang active (FR-34-01/02); soft-delete chỉ đổi
 * cột {@code status} (FR-34-10); kiểm tra tham chiếu + đổi trạng thái + ghi audit trong CÙNG
 * transaction (FR-34-17/24).
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class PublishedContentService {

    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final int MAX_PAGE_SIZE = 100;
    private static final int MIN_REASON_LENGTH = 10;
    private static final String STATUS_PUBLISHED = "published";
    private static final String STATUS_ARCHIVED = "archived";
    private static final String STATUS_DELETED = "deleted";
    private static final String ACTION_RESTORE = "restore_content";

    private final ManagedContentResolver resolver;
    private final StaffUserRepository staffUserRepository;
    private final ReviewAuditService auditService;

    /** FR-34-03..06 — Danh sách nội dung {@code published}, lọc type/level, sort published_at DESC, phân trang. */
    @Transactional(readOnly = true)
    public PublishedContentListResponse getPublishedContents(
            String managerEmail, String contentTypeValue, String jlptLevelValue, int page, int size) {
        requireManager(managerEmail);

        JlptLevel jlptLevel = parseLevel(jlptLevelValue);
        int safeSize = size <= 0 ? DEFAULT_PAGE_SIZE : Math.min(size, MAX_PAGE_SIZE);
        int safePage = Math.max(page, 0);

        List<ManagedContentSnapshot> publishedContentSnapshots = new ArrayList<>();
        if (StringUtils.hasText(contentTypeValue)) {
            publishedContentSnapshots.addAll(
                    resolver.resolve(ContentType.fromValue(contentTypeValue)).findPublished(jlptLevel));
        } else {
            for (ManagedContentHandler handler : resolver.distinctHandlers()) {
                publishedContentSnapshots.addAll(handler.findPublished(jlptLevel));
            }
        }

        publishedContentSnapshots.sort(Comparator.comparing(
                ManagedContentSnapshot::getPublishedAt, Comparator.nullsLast(Comparator.reverseOrder())));

        long totalElements = publishedContentSnapshots.size();
        int totalPages = (int) Math.ceil((double) totalElements / safeSize);
        int startIndex = Math.min(safePage * safeSize, publishedContentSnapshots.size());
        int endIndex = Math.min(startIndex + safeSize, publishedContentSnapshots.size());

        List<PublishedContentItemResponse> contentItems =
                publishedContentSnapshots.subList(startIndex, endIndex).stream()
                        .map(this::toItem)
                        .toList();

        return PublishedContentListResponse.builder()
                .content(contentItems)
                .totalElements(totalElements)
                .totalPages(totalPages)
                .build();
    }

    /** FR-34-07..09 — Chi tiết một nội dung kèm danh sách tham chiếu (404 nếu không tồn tại). */
    @Transactional(readOnly = true)
    public PublishedContentDetailResponse getContentDetail(
            String managerEmail, Long contentId, String contentTypeValue) {
        requireManager(managerEmail);
        ContentType type = ContentType.fromValue(contentTypeValue);
        ManagedContentHandler handler = resolver.resolve(type);

        ManagedContentSnapshot snapshot =
                handler.findById(contentId).orElseThrow(() -> new ContentNotFoundException(type.getValue(), contentId));
        List<ReferenceItemResponse> references = handler.findBlockingReferences(contentId);

        return PublishedContentDetailResponse.builder()
                .contentId(snapshot.getContentId())
                .contentType(type.getValue())
                .titleOrText(snapshot.getTitleOrText())
                .jlptLevel(snapshot.getJlptLevel())
                .status(snapshot.getStatus())
                .publishedAt(snapshot.getPublishedAt())
                .references(references)
                .build();
    }

    /** FR-34-10..17, 22..24 — Unpublish / Archive / Delete (soft delete) với guard tham chiếu. */
    @Transactional
    public StatusChangeResultResponse changeStatus(String managerEmail, Long contentId, ChangeStatusRequest request) {
        StaffUser manager = requireManager(managerEmail);
        ContentType type = ContentType.fromValue(request.getContentType());
        TargetStatus target = TargetStatus.fromValue(request.getStatus());
        validateReason(request.getReason());

        ManagedContentHandler handler = resolver.resolve(type);
        ManagedContentSnapshot snapshot =
                handler.findById(contentId).orElseThrow(() -> new ContentNotFoundException(type.getValue(), contentId));

        // FR-34-10 — chỉ thao tác trên item đang published.
        if (!STATUS_PUBLISHED.equals(snapshot.getStatus())) {
            throw new InvalidStateTransitionException();
        }

        // FR-34-14..17 — kiểm tra tham chiếu trong cùng transaction trước khi ẩn.
        List<ReferenceItemResponse> references = handler.findBlockingReferences(contentId);
        if (!references.isEmpty()) {
            throw new ResourceInUseException(references);
        }

        LocalDateTime now = LocalDateTime.now();
        int rows = handler.changeStatus(contentId, target, now);
        if (rows == 0) {
            // Item vừa rời khỏi 'published' do thao tác đồng thời (FR-34-22).
            throw new InvalidStateTransitionException();
        }

        auditService.log(manager, target.getAuditAction(), type, handler.tableName(), contentId, request.getReason());

        return StatusChangeResultResponse.builder()
                .contentId(contentId)
                .contentType(type.getValue())
                .status(target.getResultingStatus())
                .build();
    }

    /** FR-34-18..20, 23..24 — Khôi phục nội dung {@code archived} → {@code published}. */
    @Transactional
    public StatusChangeResultResponse restore(String managerEmail, Long contentId, RestoreContentRequest request) {
        StaffUser manager = requireManager(managerEmail);
        ContentType type = ContentType.fromValue(request.getContentType());
        ManagedContentHandler handler = resolver.resolve(type);

        ManagedContentSnapshot snapshot =
                handler.findById(contentId).orElseThrow(() -> new ContentNotFoundException(type.getValue(), contentId));

        String currentStatus = snapshot.getStatus();
        if (STATUS_DELETED.equals(currentStatus)) {
            throw new RestoreNotAllowedException(); // FR-34-19 — deleted là trạng thái cuối.
        }
        if (!STATUS_ARCHIVED.equals(currentStatus)) {
            throw new InvalidStateTransitionException(); // FR-34-20 — chỉ archived mới restore được.
        }

        LocalDateTime now = LocalDateTime.now();
        int rows = handler.restore(contentId, now);
        if (rows == 0) {
            throw new InvalidStateTransitionException();
        }

        auditService.log(manager, ACTION_RESTORE, type, handler.tableName(), contentId, null);

        return StatusChangeResultResponse.builder()
                .contentId(contentId)
                .contentType(type.getValue())
                .status(STATUS_PUBLISHED)
                .build();
    }

    // ----------------------------------------------------------------------
    // Helpers
    // ----------------------------------------------------------------------

    /** FR-34-01/02 — Chỉ staff_manager đang active; sai → 403 FORBIDDEN (enforce ở Service Layer). */
    private StaffUser requireManager(String email) {
        StaffUser staff = staffUserRepository
                .findByEmail(email)
                .orElseThrow(() -> new ForbiddenException("Tài khoản không có thẩm quyền quản lý nội dung xuất bản"));
        if (staff.getStaffRole() != StaffUser.StaffRole.STAFF_MANAGER
                || staff.getStatus() != StaffUser.StaffStatus.ACTIVE) {
            throw new ForbiddenException("Tài khoản không có thẩm quyền quản lý nội dung xuất bản");
        }
        return staff;
    }

    /** FR-34-11 — reason bắt buộc khi Unpublish/Archive/Delete (validate backend, không tin client). */
    private void validateReason(String reason) {
        if (!StringUtils.hasText(reason)) {
            throw new BusinessException(
                    400, "REASON_REQUIRED", "Phải nhập lý do khi thu hồi, lưu trữ hoặc xóa nội dung");
        }
        if (reason.trim().length() < MIN_REASON_LENGTH) {
            throw new BusinessException(
                    400, "VALIDATION_FAILED", "Lý do phải có tối thiểu " + MIN_REASON_LENGTH + " ký tự");
        }
    }

    private JlptLevel parseLevel(String jlptLevelValue) {
        if (!StringUtils.hasText(jlptLevelValue)) {
            return null;
        }
        try {
            return JlptLevel.valueOf(jlptLevelValue.trim().toUpperCase());
        } catch (IllegalArgumentException invalidLevelError) {
            throw new BusinessException(400, "VALIDATION_FAILED", "jlptLevel không hợp lệ: " + jlptLevelValue);
        }
    }

    private PublishedContentItemResponse toItem(ManagedContentSnapshot contentSnapshot) {
        return PublishedContentItemResponse.builder()
                .contentId(contentSnapshot.getContentId())
                .contentType(contentSnapshot.getContentType().getValue())
                .titleOrText(contentSnapshot.getTitleOrText())
                .jlptLevel(contentSnapshot.getJlptLevel())
                .status(contentSnapshot.getStatus())
                .publishedAt(contentSnapshot.getPublishedAt())
                .build();
    }
}
