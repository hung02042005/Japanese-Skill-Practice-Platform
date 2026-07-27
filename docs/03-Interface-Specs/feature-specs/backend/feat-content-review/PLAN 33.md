# PLAN — Content Review & Status Control (`feat-content-review`)

> **UC Coverage:** UC-33 (Review Submitted Content), UC-34 (Manage Published Content Status)
> **Trọng tâm tài liệu này:** UC-33 — Review Submitted Content (StaffManager)
> **Nguồn:** `SPEC.md`, `UC-33-review-submitted-content.md` | **Cập nhật:** 2026-06-12

---

## 1. Mục tiêu (Goals)

Triển khai luồng **kiểm duyệt nội dung chờ duyệt** cho vai trò **StaffManager** theo đặc tả UC-33. Hệ thống phải cho phép xem hàng đợi `pending_review` tập trung trên 7 bảng học liệu, xem chi tiết, và thực hiện Approve / Reject / Request Changes; đồng thời thực thi nghiêm ngặt nguyên tắc bốn mắt (cấm tự duyệt), chống xử lý đồng thời (409), và ghi audit log đầy đủ — tuân thủ `CONSTITUTION.md`, `AGENTS.md`, `CLAUDE.md`.

## 2. Kiến trúc & Công nghệ

- **Backend:** Java 21, Spring Boot 3.x, Spring Security (JWT), Spring Data JPA.
- **Database:** SQL Server; Migration bằng Flyway/Liquibase (chỉ thêm index, KHÔNG đổi schema cột nếu đã tồn tại).
- **Frontend:** React 18, Tailwind CSS, Context API (xem `frontend/feat-staff/SPEC-staff-review-queue.md`).
- **Tuân thủ kiến trúc:** Controller → Service → Repository → Entity; DTO Pattern bắt buộc (ADR-005); `@Transactional` ở Service Layer; business logic 100% ở backend (Constitution §2.5).

## 3. Quyết định thiết kế chính (Design Decisions)

- **Đa hình theo `contentType`:** 7 bảng (`courses`, `lessons`, `grammar_points`, `vocabulary`, `kanji`, `questions`, `assessments`) dùng chung bộ cột workflow (`status`, `created_by`, `approved_by`, `published_at`). Dùng **`ReviewableContentResolver`** ánh xạ `contentType` → repository tương ứng để tránh `if/else` rải rác (tránh God Class).
- **Guarded Update chống đồng thời:** Mọi chuyển trạng thái dùng `UPDATE ... SET status=? WHERE id=? AND status='pending_review'`; nếu `affectedRows = 0` ⇒ ném `ConcurrentReviewException` (HTTP 409). Bảo đảm chỉ một reviewer thắng (NFR-33-04).
- **Self-review guard ở Service Layer:** So sánh `content.created_by == currentManagerId` trước khi đổi trạng thái (FR-33-17/18), không phụ thuộc UI.
- **Audit + status change cùng transaction:** Ghi `admin_audit_logs` và đổi `status` trong cùng `@Transactional` (FR-33-22).

## 4. Các thành phần Backend

### 4.1. Database Migration

- File migration **chỉ bổ sung index** `IX_<table>_status (status, jlpt_level, updated_at)` cho 7 bảng học liệu phục vụ Review Queue (NFR-33-02). KHÔNG sửa cột có sẵn.
- Bảng `admin_audit_logs` đã tồn tại (Bảng 22) — chỉ sử dụng, không đổi.

### 4.2. Entities (chỉ đọc/cập nhật trạng thái)

- Tận dụng Entity có sẵn: `Course`, `Lesson`, `GrammarPoint`, `Vocabulary`, `Kanji`, `Question`, `Assessment`, `StaffUser`, `AdminAuditLog`.
- Bổ sung enum chung `ContentStatus { DRAFT, PENDING_REVIEW, REJECTED, PUBLISHED, ARCHIVED, DELETED }` và `ContentType`.

### 4.3. Repositories

- 7 repository học liệu cần truy vấn `findByStatus('pending_review', Pageable)` và guarded update `updateStatusIfPending(...)` (qua `@Modifying @Query`).
- `AdminAuditLogRepository.save(...)`.

### 4.4. DTOs

- Request: `ReviewActionRequest` (`contentType`, `contentId`, `action`, `feedback`), `RequestChangesRequest` (`contentType`, `contentId`, `targetStatus`, `feedback`).
- Response: `ReviewQueueItemResponse`, `ReviewableContentDetailResponse`, `ReviewResultResponse`.
- Validation: `@NotNull`, `@NotBlank` (feedback có điều kiện), enum validator cho `contentType`/`action`/`targetStatus`.

### 4.5. Services (Business Logic)

- **`ReviewableContentResolver`**: map `contentType` → repository + chiến lược đọc/cập nhật.
- **`ContentReviewService`** (`@Transactional`):
  - `getReviewQueue(type, jlptLevel, pageable)` — gộp/lọc `pending_review` (FR-33-02..05).
  - `getContentDetail(contentType, contentId)` — map DTO (FR-33-06..08).
  - `approve(req, managerId)` — guard self-review + guarded update → `published` + audit (FR-33-09..11, 17, 19, 20).
  - `reject(req, managerId)` — validate feedback → `rejected` + audit (FR-33-12, 14, 16).
  - `requestChanges(req, managerId)` — validate feedback + `targetStatus` → `draft`/`rejected` + audit (FR-33-13..16).
- **`ReviewAuditService`**: ghi `admin_audit_logs` (`staff_actor_id`, `action`, `target_table`, `target_id`, `description`) + SLF4J log (FR-33-20/21).

### 4.6. Controllers & Security

- **`ManagerReviewController`** (`/api/manager/**`):
  - `GET /review-queue`, `GET /contents/{contentId}`, `POST /reviews`, `POST /reviews/request-changes`.
- **Security:** `@PreAuthorize("hasAuthority('STAFF_MANAGER')")` trên toàn controller; cấu hình route `/api/manager/**` yêu cầu JWT (FR-33-01, NFR-33-03).
- **`GlobalExceptionHandler`:** map `SelfReviewNotAllowedException`→403, `ConcurrentReviewException`→409, `ContentNotFoundException`→404, `FeedbackRequiredException`→400 theo format `{ status, message, data }`.

## 5. Các thành phần Frontend

- Trang `ReviewQueuePage` (StaffManager): bảng phân trang `pending_review`, bộ lọc `type`/`jlptLevel`, trạng thái loading/error.
- `ReviewDetailDrawer`: hiển thị chi tiết + 3 nút Approve / Reject / Request Changes; ô feedback **bắt buộc** khi Reject/Request Changes (UX validation, không thay backend).
- Tách riêng UI Staff vs StaffManager (LESSON-001); API call gom trong `services/` (tránh Direct API in Component).

## 6. Tiêu chuẩn đánh giá (Definition of Done)

- Self-review bị chặn ở Service Layer; có test chứng minh không bypass được.
- Concurrent review trả 409 đúng (test 2 luồng).
- Mọi action ghi `admin_audit_logs` + SLF4J, không `System.out.println`.
- Unit test Service ≥ 80% coverage; Integration test 4 endpoint (happy + error).
- Controller chỉ trả DTO (không lộ Entity); error đúng `{ status, message, data }`.
- Không TODO comment; pass `mvn spotless:apply` & lint.
