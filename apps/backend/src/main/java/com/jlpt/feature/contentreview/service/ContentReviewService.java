/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.contentreview.service;

import com.jlpt.feature.contentreview.dto.RequestChangesRequest;
import com.jlpt.feature.contentreview.dto.ReviewActionRequest;
import com.jlpt.feature.contentreview.dto.ReviewQueueItemResponse;
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
import com.jlpt.feature.contentreview.model.ReviewAction;
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
 * UC-33 — Logic kiểm duyệt nội dung chờ duyệt cho StaffManager.
 *
 * <p>Thực thi: chỉ {@code staff_manager} (FR-33-01/02), nguyên tắc bốn mắt (FR-33-17/18),
 * guarded update chống đồng thời (FR-33-19), audit + đổi trạng thái cùng transaction (FR-33-22).
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ContentReviewService {

    private static final int MAX_PAGE_SIZE = 100;
    private static final String STATUS_PUBLISHED = "published";
    private static final String STATUS_REJECTED = "rejected";
    private static final String STATUS_DRAFT = "draft";

    private final ReviewableContentResolver resolver;
    private final StaffUserRepository staffUserRepository;
    private final ReviewAuditService reviewAuditService;

    /** FR-33-02..05 — Hàng đợi nội dung {@code pending_review}, sort updated_at ASC, phân trang. */
    @Transactional(readOnly = true)
    public ReviewQueueResponse getReviewQueue(
            String managerEmail, String typeStr, String levelStr, int page, int size) {
        requireManager(managerEmail);

        JlptLevel level = parseLevel(levelStr);
        int safeSize = size <= 0 ? 20 : Math.min(size, MAX_PAGE_SIZE);
        int safePage = Math.max(page, 0);

        List<ContentSnapshot> all = new ArrayList<>();
        if (StringUtils.hasText(typeStr)) {
            all.addAll(resolver.resolve(ContentType.fromValue(typeStr)).findPending(level));
        } else {
            for (ReviewableContentHandler handler : resolver.distinctHandlers()) {
                all.addAll(handler.findPending(level));
            }
        }

        all.sort(
                Comparator.comparing(ContentSnapshot::getSubmittedAt, Comparator.nullsLast(Comparator.naturalOrder())));

        long totalElements = all.size();
        int totalPages = (int) Math.ceil((double) totalElements / safeSize);
        int from = Math.min(safePage * safeSize, all.size());
        int endIndex = Math.min(from + safeSize, all.size());

        List<ReviewQueueItemResponse> content =
                all.subList(from, endIndex).stream().map(this::toQueueItem).toList();

        return ReviewQueueResponse.builder()
                .content(content)
                .totalElements(totalElements)
                .totalPages(totalPages)
                .build();
    }

    /** FR-33-06..08 — Chi tiết một nội dung (404 nếu không tồn tại/đã xóa). */
    @Transactional(readOnly = true)
    public ReviewableContentDetailResponse getContentDetail(String managerEmail, Long contentId, String typeStr) {
        requireManager(managerEmail);
        ContentType type = ContentType.fromValue(typeStr);
        ContentSnapshot snapshot = resolver.resolve(type)
                .findActiveById(contentId)
                .orElseThrow(() -> new ContentNotFoundException(type.getValue(), contentId));

        return ReviewableContentDetailResponse.builder()
                .contentId(snapshot.getContentId())
                .contentType(type.getValue())
                .titleOrText(snapshot.getTitleOrText())
                .jlptLevel(snapshot.getJlptLevel())
                .status(snapshot.getStatus())
                .submittedById(snapshot.getCreatedById())
                .submittedBy(snapshot.getCreatedByName())
                .submittedAt(snapshot.getSubmittedAt())
                .detail(snapshot.getDetail())
                .build();
    }

    /** FR-33-09..12, 14, 17, 19, 20 — Approve hoặc Reject. */
    @Transactional
    public ReviewResultResponse review(String managerEmail, ReviewActionRequest request) {
        StaffUser manager = requireManager(managerEmail);
        ContentType type = ContentType.fromValue(request.getContentType());
        ReviewAction action = ReviewAction.fromValue(request.getAction());

        ReviewableContentHandler handler = resolver.resolve(type);
        ContentSnapshot snapshot = handler.findActiveById(request.getContentId())
                .orElseThrow(() -> new ContentNotFoundException(type.getValue(), request.getContentId()));

        guardSelfReview(snapshot, manager);

        LocalDateTime now = LocalDateTime.now();
        if (action == ReviewAction.APPROVE) {
            int rows = handler.approve(request.getContentId(), manager, now);
            ensureUpdated(rows);
            reviewAuditService.log(
                    manager,
                    ReviewAuditService.ACTION_APPROVE,
                    type,
                    handler.tableName(),
                    request.getContentId(),
                    request.getFeedback());
            return ReviewResultResponse.builder()
                    .contentId(request.getContentId())
                    .contentType(type.getValue())
                    .status(STATUS_PUBLISHED)
                    .approvedAt(now)
                    .build();
        }

        // REJECT — feedback bắt buộc (FR-33-14)
        if (!StringUtils.hasText(request.getFeedback())) {
            throw new FeedbackRequiredException();
        }
        int rows = handler.transitionFromPending(request.getContentId(), STATUS_REJECTED, now);
        ensureUpdated(rows);
        reviewAuditService.log(
                manager,
                ReviewAuditService.ACTION_REJECT,
                type,
                handler.tableName(),
                request.getContentId(),
                request.getFeedback());
        return ReviewResultResponse.builder()
                .contentId(request.getContentId())
                .contentType(type.getValue())
                .status(STATUS_REJECTED)
                .build();
    }

    /** FR-33-13..16, 19 — Request Changes (→ draft mặc định, hoặc rejected). */
    @Transactional
    public ReviewResultResponse requestChanges(String managerEmail, RequestChangesRequest request) {
        StaffUser manager = requireManager(managerEmail);
        ContentType type = ContentType.fromValue(request.getContentType());

        if (!StringUtils.hasText(request.getFeedback())) {
            throw new FeedbackRequiredException();
        }
        String targetStatus = resolveRequestChangesTarget(request.getTargetStatus());

        ReviewableContentHandler handler = resolver.resolve(type);
        ContentSnapshot snapshot = handler.findActiveById(request.getContentId())
                .orElseThrow(() -> new ContentNotFoundException(type.getValue(), request.getContentId()));

        guardSelfReview(snapshot, manager);

        LocalDateTime now = LocalDateTime.now();
        int rows = handler.transitionFromPending(request.getContentId(), targetStatus, now);
        ensureUpdated(rows);
        reviewAuditService.log(
                manager,
                ReviewAuditService.ACTION_REQUEST_CHANGES,
                type,
                handler.tableName(),
                request.getContentId(),
                request.getFeedback());
        return ReviewResultResponse.builder()
                .contentId(request.getContentId())
                .contentType(type.getValue())
                .status(targetStatus)
                .build();
    }

    // ----------------------------------------------------------------------
    // Helpers
    // ----------------------------------------------------------------------

    /** FR-33-01/02, NFR-33-01 — Chỉ staff_manager đang active; sai → 403 FORBIDDEN (enforce ở Service). */
    private StaffUser requireManager(String email) {
        StaffUser staff = staffUserRepository
                .findByEmail(email)
                .orElseThrow(() -> new ForbiddenException("Tài khoản không có thẩm quyền kiểm duyệt"));
        if (staff.getStaffRole() != StaffUser.StaffRole.STAFF_MANAGER
                || staff.getStatus() != StaffUser.StaffStatus.ACTIVE) {
            throw new ForbiddenException("Tài khoản không có thẩm quyền kiểm duyệt");
        }
        return staff;
    }

    /** FR-33-17/18 — Không cho reviewer trùng người tạo. */
    private void guardSelfReview(ContentSnapshot snapshot, StaffUser manager) {
        if (snapshot.getCreatedById() != null && snapshot.getCreatedById().equals(manager.getId())) {
            throw new SelfReviewNotAllowedException();
        }
    }

    /** FR-33-10/19 — 0 dòng cập nhật nghĩa là item không còn pending_review (đã bị xử lý song song). */
    private void ensureUpdated(int affectedRows) {
        if (affectedRows == 0) {
            throw new ConcurrentReviewException();
        }
    }

    /** FR-33-13/15 — targetStatus ∈ {draft, rejected}, mặc định draft; khác → 400 VALIDATION_FAILED. */
    private String resolveRequestChangesTarget(String raw) {
        if (!StringUtils.hasText(raw)) {
            return STATUS_DRAFT;
        }
        String normalized = raw.trim().toLowerCase();
        if (!STATUS_DRAFT.equals(normalized) && !STATUS_REJECTED.equals(normalized)) {
            throw new BusinessException(400, "VALIDATION_FAILED", "targetStatus chỉ nhận 'draft' hoặc 'rejected'");
        }
        return normalized;
    }

    private JlptLevel parseLevel(String levelStr) {
        if (!StringUtils.hasText(levelStr)) {
            return null;
        }
        try {
            return JlptLevel.valueOf(levelStr.trim().toUpperCase());
        } catch (IllegalArgumentException invalidLevelError) {
            throw new BusinessException(400, "VALIDATION_FAILED", "jlptLevel không hợp lệ: " + levelStr);
        }
    }

    private ReviewQueueItemResponse toQueueItem(ContentSnapshot contentSnapshot) {
        return ReviewQueueItemResponse.builder()
                .contentId(contentSnapshot.getContentId())
                .contentType(contentSnapshot.getContentType().getValue())
                .titleOrText(contentSnapshot.getTitleOrText())
                .jlptLevel(contentSnapshot.getJlptLevel())
                .submittedBy(contentSnapshot.getCreatedByName())
                .submittedById(contentSnapshot.getCreatedById())
                .submittedAt(contentSnapshot.getSubmittedAt())
                .build();
    }
}
