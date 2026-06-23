# TASKS — Content Review (`feat-content-review`) — UC-33

> **UC:** UC-33 (Review Submitted Content) | **Nguồn:** `UC-33-review-submitted-content.md`, `PLAN.md`
> **Cập nhật:** 2026-06-12

## Phase 1: Database & Domain

- [ ] 1.1 Migration (Flyway/Liquibase) **chỉ thêm** index `IX_<table>_status (status, jlpt_level, updated_at)` cho 7 bảng: `courses`, `lessons`, `grammar_points`, `vocabulary`, `kanji`, `questions`, `assessments` (NFR-33-02). KHÔNG sửa cột có sẵn.
- [ ] 1.2 Tạo enum `ContentStatus` và `ContentType`; ánh xạ giá trị khớp ràng buộc `CHECK` của DB.
- [ ] 1.3 Rà soát Entity học liệu có cột `created_by`, `approved_by`, `published_at`, `status` đúng map; bổ sung nếu thiếu (chỉ ở tầng Entity, không đổi DB).

## Phase 2: Repository Layer

- [ ] 2.1 Mỗi repository học liệu: thêm `Page<T> findByStatus(String status, Pageable)` (lọc `pending_review`).
- [ ] 2.2 Mỗi repository: thêm guarded update `@Modifying @Query("UPDATE ... SET status=:to, approvedBy=:mgr, publishedAt=:now WHERE id=:id AND status='pending_review'")` trả về số dòng ảnh hưởng (chống đồng thời — FR-33-19).
- [ ] 2.3 `AdminAuditLogRepository` (nếu chưa có) — `save(...)`.

## Phase 3: DTO & Validation

- [ ] 3.1 `ReviewActionRequest` (`contentType`, `contentId`, `action` ∈ {APPROVE,REJECT}, `feedback`).
- [ ] 3.2 `RequestChangesRequest` (`contentType`, `contentId`, `targetStatus` ∈ {draft,rejected}, `feedback` `@NotBlank`).
- [ ] 3.3 Response DTOs: `ReviewQueueItemResponse`, `ReviewableContentDetailResponse`, `ReviewResultResponse`.
- [ ] 3.4 Custom validator/logic: `feedback` bắt buộc khi `action=REJECT` và mọi Request Changes (FR-33-14).

## Phase 4: Service Layer (Business Logic)

- [ ] 4.1 `ReviewableContentResolver`: map `contentType` → repository + chiến lược đọc/cập nhật (tránh God Class).
- [ ] 4.2 `ContentReviewService.getReviewQueue(type, jlptLevel, pageable)` — gộp `pending_review`, sort `updated_at` asc (FR-33-02..05).
- [ ] 4.3 `ContentReviewService.getContentDetail(contentType, contentId)` — map DTO; 404 nếu không tồn tại/deleted (FR-33-06..08).
- [ ] 4.4 `ContentReviewService.approve(req, managerId)` — guard self-review (FR-33-17) → guarded update → `published`, `approved_by`, `published_at` (FR-33-09..11); 409 nếu update 0 dòng (FR-33-19).
- [ ] 4.5 `ContentReviewService.reject(req, managerId)` — validate feedback → `rejected` (FR-33-12, 14, 16).
- [ ] 4.6 `ContentReviewService.requestChanges(req, managerId)` — validate feedback + targetStatus → `draft`/`rejected` (FR-33-13..15).
- [ ] 4.7 `ReviewAuditService.log(...)` — ghi `admin_audit_logs` (`staff_actor_id`, `action`, `target_table`, `target_id`, `description=feedback`) + SLF4J; cùng `@Transactional` với đổi status (FR-33-20..22).
- [ ] 4.8 Custom exceptions: `SelfReviewNotAllowedException`, `ConcurrentReviewException`, `ContentNotFoundException`, `FeedbackRequiredException`.

## Phase 5: Controller & Security

- [ ] 5.1 `ManagerReviewController`: `GET /api/manager/review-queue`, `GET /api/manager/contents/{contentId}`, `POST /api/manager/reviews`, `POST /api/manager/reviews/request-changes`.
- [ ] 5.2 `@PreAuthorize("hasAuthority('STAFF_MANAGER')")` + cấu hình SecurityFilterChain cho `/api/manager/**` (FR-33-01, NFR-33-03).
- [ ] 5.3 Cập nhật `GlobalExceptionHandler`: map 400/403/404/409/500 theo `{ status, message, data }` (UC-33 §7).

## Phase 6: Testing & QA (Backend)

- [ ] 6.1 Unit test `ContentReviewService` ≥ 80% (approve/reject/request-changes, self-review, concurrent, feedback bắt buộc) — xem `feat-testing/TC-UC-33`.
- [ ] 6.2 Integration test 4 endpoint (happy + error: 400/403/404/409) bằng MockMvc.
- [ ] 6.3 Test bất biến: response không lộ Entity/cột nhạy cảm; audit log được ghi đúng.

## Phase 7: Frontend

- [ ] 7.1 `services/managerReviewService` gom 4 API call.
- [ ] 7.2 `ReviewQueuePage` (bảng phân trang + lọc `type`/`jlptLevel`, loading/error).
- [ ] 7.3 `ReviewDetailDrawer` (Approve/Reject/Request Changes; feedback bắt buộc khi Reject/Request Changes — UX).
- [ ] 7.4 Tách UI StaffManager khỏi Staff (LESSON-001); ProtectedRoute theo quyền `staff_manager`.

## Phase 8: Final Review

- [ ] 8.1 Cross-check `UC-33` §3 (FR) ↔ code; xác nhận Rule 1–10 đều có test phủ.
- [ ] 8.2 Lint sạch (`mvn spotless:apply`, `npm run lint`); không TODO.
- [ ] 8.3 Code Review & Merge theo PR (≤ 400 dòng/PR; chia nhỏ nếu vượt).
