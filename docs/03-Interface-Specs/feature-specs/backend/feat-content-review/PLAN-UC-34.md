# PLAN — Manage Published Content Status (`feat-content-review`) — UC-34

> **UC Coverage:** UC-34 (Manage Published Content Status)
> **Trọng tâm tài liệu này:** UC-34 — Quản lý trạng thái nội dung đã xuất bản (StaffManager)
> **Nguồn:** `SPEC.md` §3.2, `UC-34-manage-published-content-status.md` | **Cập nhật:** 2026-06-12
> **Lưu ý:** PLAN cho UC-33 nằm ở `PLAN.md`. Tài liệu này bổ sung phần UC-34, tái sử dụng tối đa hạ tầng đã dựng ở UC-33.

---

## 1. Mục tiêu (Goals)

Triển khai luồng **quản lý vòng đời sau xuất bản** cho vai trò **StaffManager** theo đặc tả UC-34. Hệ thống phải cho phép xem danh sách nội dung `published` trên 7 bảng học liệu, xem chi tiết kèm danh sách tham chiếu, và thực hiện **Unpublish / Archive / Delete (soft delete)** + **Restore**; đồng thời thực thi nghiêm ngặt **kiểm tra toàn vẹn tham chiếu** (chặn ẩn câu hỏi đang trong đề thi `published` → 409 `RESOURCE_IN_USE`), chỉ cho **Restore** nội dung `archived` (không cho `deleted` — trạng thái cuối), loại nội dung đã ẩn khỏi mọi Student-facing API, và ghi audit log đầy đủ — tuân thủ `CONSTITUTION.md`, `AGENTS.md`, `CLAUDE.md`.

## 2. Kiến trúc & Công nghệ

- **Backend:** Java 21, Spring Boot 3.x, Spring Security (JWT), Spring Data JPA.
- **Database:** SQL Server; Migration bằng Flyway/Liquibase (chỉ thêm index nếu cần, KHÔNG đổi schema cột đã tồn tại).
- **Frontend:** React 18, Tailwind CSS, Context API (xem `frontend/feat-staff/SPEC-staff-content.md`).
- **Tuân thủ kiến trúc:** Controller → Service → Repository → Entity; DTO Pattern bắt buộc (ADR-005); `@Transactional` ở Service Layer; business logic 100% ở backend (Constitution §2.5); Soft Delete toàn hệ thống (ADR-004).

## 3. Quyết định thiết kế chính (Design Decisions)

- **Tái dùng `ReviewableContentResolver` (từ UC-33):** Tiếp tục dùng resolver `contentType` → repository cho 7 bảng học liệu; bổ sung chiến lược đọc danh sách `published` và guarded update theo trạng thái nguồn.
- **Quy ước chuyển trạng thái UC-34:** `Unpublish → 'draft'`, `Archive → 'archived'`, `Delete → 'deleted'`, `Restore (archived) → 'published'`. Giá trị nằm trong `CHECK` constraint hiện có nên **không cần migration đổi schema**.
- **Reference-integrity guard trong cùng transaction (NFR-34-02):** Trước khi UPDATE status cho `question`, chạy query đếm assignment vào assessment `published`:
  `SELECT a.assessment_id, a.title FROM question_assignments qa JOIN assessments a ON a.assessment_id = qa.parent_id WHERE qa.parent_type='assessment' AND qa.question_id=:id AND a.status='published'`.
  Với `lesson`/`assessment`: kiểm tra `assessments.lesson_id` + `question_assignments` active. Nếu có dòng chặn ⇒ ném `ResourceInUseException` (HTTP 409) kèm danh sách `references`.
- **Guarded Update theo trạng thái nguồn:** `UPDATE ... SET status=:to WHERE id=:id AND status=:expectedFrom`; nếu `affectedRows=0` ⇒ ném `InvalidStateTransitionException` (409) — chống race khi item đã đổi trạng thái.
- **Restore guard:** Chỉ cho `archived → published`; nếu nguồn là `deleted` ⇒ `RestoreNotAllowedException` (409 `RESTORE_NOT_ALLOWED`); nếu nguồn khác ⇒ `InvalidStateTransitionException` (409).
- **Audit + status change cùng transaction (FR-34-24):** Ghi `admin_audit_logs` và đổi `status` trong cùng `@Transactional`; SLF4J log, không `System.out.println`.
- **Student-facing exclusion (FR-34-21/22):** Bảo đảm mọi truy vấn Student-facing đã lọc `status='published'` ở Repository/Service; UC-34 không thêm code Student-facing, chỉ chịu trách nhiệm chuyển trạng thái để loại trừ tức thì.

## 4. Các thành phần Backend

### 4.1. Database Migration

- **Không đổi schema.** Các index phục vụ lọc `status` đã có (`IX_questions_public_bank`, `IX_assessments_public_list`, `IX_assign_parent`, …). Nếu thiếu index `status, published_at` cho danh sách published, có thể bổ sung index mới (chỉ thêm, không sửa cột).
- Bảng `question_assignments`, `assessments`, `admin_audit_logs` — chỉ sử dụng, không đổi.

### 4.2. Entities (chỉ đọc/cập nhật trạng thái)

- Tận dụng Entity có sẵn: `Course`, `Lesson`, `GrammarPoint`, `Vocabulary`, `Kanji`, `Question`, `Assessment`, `QuestionAssignment`, `StaffUser`, `AdminAuditLog`.
- Dùng lại enum `ContentStatus` và `ContentType` (đã tạo ở UC-33).

### 4.3. Repositories

- 7 repository học liệu: thêm `Page<T> findByStatus("published", Pageable)` (đã có từ UC-33, dùng lại với giá trị `published`) và guarded update `updateStatusFrom(id, expectedFrom, to)`.
- `QuestionAssignmentRepository`: `findPublishedAssessmentRefsByQuestionId(questionId)` → danh sách `(assessment_id, title)` đang `published`.
- `AssessmentRepository`: `findPublishedByLessonId(lessonId)` → kiểm tra lesson bị tham chiếu.
- `AdminAuditLogRepository.save(...)`.

### 4.4. DTOs

- Request: `UpdateContentStatusRequest` (`contentType`, `status` ∈ {unpublished,archived,deleted}, `reason` `@NotBlank` 10–500), `RestoreContentRequest` (`contentType`).
- Response: `PublishedContentItemResponse`, `PublishedContentDetailResponse` (kèm `references`), `ContentStatusResultResponse`, `ResourceReferenceResponse` (`referenceType`, `referenceId`, `referenceTitle`).
- Validation: `@NotNull`, `@NotBlank`, enum validator cho `contentType`/`status`.

### 4.5. Services (Business Logic)

- **`ReviewableContentResolver`** (dùng lại): map `contentType` → repository + chiến lược cập nhật.
- **`ContentReferenceChecker`** (mới): tập trung logic kiểm tra tham chiếu (FR-34-14/15), trả `List<ResourceReferenceResponse>` (rỗng = không bị chặn).
- **`PublishedContentService`** (`@Transactional`):
  - `listPublished(type, jlptLevel, pageable)` — gộp/lọc `published`, sort `published_at` desc (FR-34-03..06).
  - `getDetail(contentType, contentId)` — map DTO + `references` (FR-34-07..09).
  - `updateStatus(contentId, req, managerId)` — validate reason → reference check → guarded update → audit (FR-34-10..17, 23, 24). Ném `ResourceInUseException` nếu bị chặn.
  - `restore(contentId, req, managerId)` — guard `deleted`/`archived` → `published` + audit (FR-34-18..20).
- **`StatusChangeAuditService`** (hoặc dùng lại `ReviewAuditService`): ghi `admin_audit_logs` (`staff_actor_id`, `action` ∈ {unpublish_content, archive_content, delete_content, restore_content}, `target_table`, `target_id`, `description=reason`) + SLF4J.

### 4.6. Controllers & Security

- **`ManagerContentStatusController`** (`/api/manager/**`):
  - `GET /published-contents`, `GET /contents/{contentId}` (dùng chung detail với UC-33 nếu hợp lý), `PUT /contents/{contentId}/status`, `POST /contents/{contentId}/restore`.
- **Security:** `@PreAuthorize("hasAuthority('STAFF_MANAGER')")` trên toàn controller; route `/api/manager/**` yêu cầu JWT (FR-34-01, NFR-34-04).
- **`GlobalExceptionHandler`:** map `ResourceInUseException`→409 (kèm `references` trong `data`), `RestoreNotAllowedException`→409, `InvalidStateTransitionException`→409, `ContentNotFoundException`→404, `ReasonRequiredException`→400, validation→400 theo format `{ status, message, data }`.

## 5. Các thành phần Frontend

- Trang `PublishedContentPage` (StaffManager): bảng phân trang `published`, bộ lọc `type`/`jlptLevel`, loading/error.
- `ContentStatusActions`: nút Unpublish / Archive / Delete (mở modal nhập `reason` **bắt buộc** — UX validation, không thay backend) + nút Restore cho item `archived`.
- Hiển thị danh sách `references` khi backend trả 409 `RESOURCE_IN_USE` (modal "Đang được dùng trong các đề thi …").
- Tách riêng UI Staff vs StaffManager (LESSON-001); API call gom trong `services/` (tránh Direct API in Component).

## 6. Tiêu chuẩn đánh giá (Definition of Done)

- Soft delete đúng: Unpublish/Archive/Delete chỉ đổi `status`, không `DELETE FROM`; có test chứng minh bản ghi vẫn tồn tại.
- Reference check chặn đúng câu hỏi đang trong đề thi published → 409 `RESOURCE_IN_USE` kèm `references`; chạy cùng transaction.
- Restore: `archived → published` thành công; `deleted` bị chặn 409 `RESTORE_NOT_ALLOWED`.
- Nội dung đã ẩn không xuất hiện ở Student-facing (test loại trừ).
- Mọi action ghi `admin_audit_logs` + SLF4J, không `System.out.println`.
- Unit test Service ≥ 80% coverage; Integration test 4 endpoint (happy + error: 400/403/404/409).
- Controller chỉ trả DTO (không lộ Entity); error đúng `{ status, message, data }`.
- Không TODO comment; pass `mvn spotless:apply` & lint.
