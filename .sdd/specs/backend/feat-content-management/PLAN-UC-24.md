# PLAN — Manage Question Bank (`feat-content-management`) — UC-24

> **UC Coverage:** UC-24 (Manage Question Bank)
> **Trọng tâm:** Soạn thảo / tìm kiếm / sửa / gửi duyệt câu hỏi (`questions`) — vai trò Staff
> **Nguồn:** `SPEC.md` §3.1, `UC-24-manage-question-bank.md` | **Cập nhật:** 2026-06-12
> **Lưu ý:** Hạ tầng dùng chung (resolver submit-review, OwnershipGuard, enum, security) mô tả ở `PLAN.md`. Tài liệu này bổ sung phần riêng UC-24.

---

## 1. Mục tiêu (Goals)

Cho phép Staff **tạo, xem, tìm kiếm/lọc, cập nhật và gửi duyệt** câu hỏi trắc nghiệm theo `skill`/`jlpt_level`/`question_type`; thực thi nghiêm ngặt **khóa câu hỏi đã làm bài** (LESSON-005) và **cấm Staff tự publish**. Mọi câu hỏi mới ở `draft`, chỉ chuyển `pending_review` qua endpoint gửi duyệt.

## 2. Kiến trúc & Công nghệ

Như `PLAN.md` §2. Bảng trọng tâm: `questions`; bảng đọc thêm: `attempt_answers` (nguồn khóa), `staff_users` (chủ sở hữu).

## 3. Quyết định thiết kế chính (Design Decisions)

- **Khóa câu hỏi (DD-03):** `isLocked = existsByQuestionIdInAttemptAnswers(id)`. Kiểm tra **trong cùng transaction** với UPDATE; khóa ⇒ `QuestionLockedException` (409 `QUESTION_LOCKED`), KHÔNG sửa (FR-24-15/17, NFR-24-03). Versioning là Phase 2 — chỉ chặn.
- **Validate theo `question_type`:** multiple_choice cần `optionA..D` + `correctOption∈{A,B,C,D}`; true_false cần `correctAnswerText∈{true,false}` (bỏ qua options); fill_blank cần `correctAnswerText` (bỏ qua `correctOption`) (FR-24-06/07/08).
- **Guard state-machine:** update/submit chỉ khi `status∈{draft,rejected}`; khác ⇒ 409 `INVALID_STATUS_TRANSITION` (FR-24-18/22).
- **Ép draft + bỏ qua status client (DD-01):** create ép `status='draft'`, `created_by=caller`; bỏ qua `status/approved_by/published_at` (FR-24-01). Staff không có đường đặt `published` ⇒ 403 `PUBLISH_NOT_ALLOWED` (FR-24-23).
- **Tìm kiếm/lọc AND + loại deleted:** `q` LIKE không phân biệt hoa thường trên `question_text`; lọc `skill/jlptLevel/questionType/status` theo AND; loại `deleted` trừ khi `status=deleted` (FR-24-11/12/13). Sort `updated_at` desc, phân trang (FR-24-10).

## 4. Các thành phần Backend

### 4.1. Database Migration

- Chỉ thêm index `IX_questions_filter (status, jlpt_level, skill, question_type)` (NFR-24-01). KHÔNG sửa cột.

### 4.2. Entities

- `Question` (map đủ cột workflow), `AttemptAnswer` (read-only). Enum `QuestionType`, `Skill`, `JlptLevel`, `ContentStatus` (dùng chung).

### 4.3. Repositories

- `QuestionRepository.findWithFilters(q, skill, level, type, status, Pageable)`; `existsAttemptAnswerByQuestionId(id)`.

### 4.4. DTOs

- Request: `CreateQuestionRequest`, `UpdateQuestionRequest` (+ validator theo type), `SubmitReviewRequest` (dùng chung).
- Response: `QuestionSummaryResponse`, `QuestionDetailResponse` (kèm `isLocked`), `PageResponse<T>`.

### 4.5. Services

- `QuestionService` (`@Transactional`): `create`, `list`, `getDetail` (+`isLocked`), `update` (re-validate + `assertNotLocked` + guard status + `OwnershipGuard`). Submit-review đi qua `ContentSubmissionService` (PLAN.md §4.5) với `contentType=question`.
- Exceptions: `QuestionLockedException`(409), `InvalidStatusTransitionException`(409), `PublishNotAllowedException`(403), `OwnershipDeniedException`(403), `ContentNotFoundException`(404).

### 4.6. Controllers & Security

- `StaffQuestionController` (`/api/staff/questions`): `POST`, `GET`, `GET /{id}`, `PUT /{id}`.
- `@PreAuthorize("hasAnyAuthority('STAFF','STAFF_MANAGER')")`; `GlobalExceptionHandler` map theo UC-24 §7.

## 5. Các thành phần Frontend

- `QuestionBankPage` (list + search/filter, badge `isLocked`), `QuestionEditorPage` (form đổi field theo `questionType`); nút "Gửi duyệt" disabled khi câu đã khóa/không ở draft (UX).

## 6. Definition of Done

- Câu đã làm bài bị chặn sửa (`QUESTION_LOCKED`), kiểm trong transaction — có test.
- Validate theo type đúng; Staff không publish được (`PUBLISH_NOT_ALLOWED`).
- Ownership guard; update chỉ khi draft/rejected; search/filter loại deleted.
- Controller chỉ trả DTO; SLF4J log; soft delete; coverage Service ≥ 80% (`feat-testing/TC-UC-24`).
