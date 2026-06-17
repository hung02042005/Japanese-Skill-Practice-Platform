# PLAN — Manage Grammar Content (`feat-content-management`) — UC-25

> **UC Coverage:** UC-25 (Manage Grammar Content)
> **Trọng tâm:** Tạo / xem / sửa / liên kết lesson / gửi duyệt điểm ngữ pháp (`grammar_points`) — vai trò Staff
> **Nguồn:** `UC-25-manage-grammar-content.md` | **Cập nhật:** 2026-06-12
> **Lưu ý:** Hạ tầng dùng chung ở `PLAN.md`. UC-25 **không** có bảng `courses`; chỉ liên kết `lesson_id`. Tên cột DB khác đề bài (xem UC-25 §5).

---

## 1. Mục tiêu (Goals)
Cho phép Staff **tạo, xem (của mình), sửa khi `draft`/`rejected`, liên kết lesson và gửi duyệt** điểm ngữ pháp; bảo toàn cấp độ JLPT, cấm tự publish, soft delete.

## 2. Kiến trúc & Công nghệ
Như `PLAN.md` §2. Bảng trọng tâm: `grammar_points`; đọc thêm: `lessons` (khớp `jlpt_level`), `staff_users`.

## 3. Quyết định thiết kế chính (Design Decisions)
- **Trường bắt buộc enforce ở Service:** `usage_explanation` và `example_sentence_jp` là NULL-able ở DB nhưng **bắt buộc theo NV** (Rule #2) ⇒ validate ở Service, không sửa schema (FR-03). Cùng với `structure`, `meaning`, `jlpt_level` (FR-03/05).
- **Liên kết lesson khớp cấp độ:** WHERE có `lessonId` ⇒ lesson tồn tại & chưa `deleted` (else 404), và `lesson.jlpt_level == grammar.jlpt_level` (else 422 `ERR-LEVEL-MISMATCH-422`) — chống lẫn cấp độ (FR-06/07/08, AGENTS §5#5).
- **Guard state-machine:** sửa chỉ khi `status∈{draft,rejected}`; `published` ⇒ 422 (cần version mới); `pending_review`/`archived` ⇒ 422 (FR-13/14/15). PUT **bỏ qua** `status` client (FR-16).
- **Ownership (FR-17):** chỉ `created_by=caller` (hoặc quyền cao hơn) mới sửa, else 403.
- **List "của tôi" + loại deleted (FR-09/11):** chỉ trả grammar `created_by=caller`, loại `deleted`, phân trang + lọc `jlpt_level`/`status`.
- **Submit-review gate đầy đủ (FR-18/20):** chặn submit nếu thiếu trường bắt buộc ⇒ 422 `ERR-SUBMIT-INCOMPLETE-422`.
- **Audit:** chỉ ghi application log SLF4J (`GRAMMAR_CREATED/UPDATED/SUBMITTED`), KHÔNG ghi `admin_audit_logs` (FR-22).

## 4. Các thành phần Backend
### 4.1. Database Migration
- Tận dụng index `IX_grammar_public_level`; bổ sung `(created_by, status)` nếu thiếu cho list "của tôi". KHÔNG sửa cột.

### 4.2. Entities
- `GrammarPoint` (map `structure`, `formula`, `meaning`, `usage_explanation`, `jlpt_level`, `example_sentence_jp/vi`, `lesson_id`, cột workflow); `Lesson` (read-only).

### 4.3. Repositories
- `GrammarPointRepository.findByCreatedByWithFilters(staffId, jlptLevel, status, Pageable)`; `LessonRepository.findActiveById(id)`.

### 4.4. DTOs
- Request: `CreateGrammarRequest`, `UpdateGrammarRequest` (không `status`), `SubmitReviewRequest`.
- Response: `GrammarSummaryResponse`, `GrammarDetailResponse` (+ lesson rút gọn).

### 4.5. Services
- `GrammarService` (`@Transactional`): `create`, `listMine`, `getDetail`, `update` (guard status + ownership + liên kết lesson). Submit-review qua `ContentSubmissionService` (`contentType=grammar`).
- Exceptions: `LevelMismatchException`(422), `InvalidStatusTransitionException`(422), `ContentNotFoundException`(404), `LessonNotFoundException`(404), `OwnershipDeniedException`(403), `SubmitIncompleteException`(422).

### 4.6. Controllers & Security
- `StaffGrammarController` (`/api/staff/grammar`): `POST`, `GET`, `GET /{id}`, `PUT /{id}`.
- `@PreAuthorize("hasAnyAuthority('STAFF','STAFF_MANAGER')")`; map lỗi theo UC-25 §7.

## 5. Các thành phần Frontend
- `GrammarEditorPage` (form `structure`/`meaning`/`usageExplanation`/ví dụ + chọn lesson cùng cấp độ); list "của tôi" + lọc.

## 6. Definition of Done
- Lesson khác cấp độ bị chặn (422); thiếu trường bắt buộc chặn create & submit.
- Sửa theo state-machine; ownership; list chỉ của mình & loại deleted.
- Cấm publish; chỉ application log; DTO; coverage ≥ 80% (`feat-testing/TC-UC-25`).
